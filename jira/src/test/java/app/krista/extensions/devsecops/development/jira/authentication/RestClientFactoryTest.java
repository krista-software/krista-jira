/*
 * JIRA Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.devsecops.development.jira.authentication;

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.oauth.ClientCredentialsTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("RestClientFactory Tests")
class RestClientFactoryTest {

    @Mock private JiraAttributes jiraAttributes;
    @Mock private OAuth2RestClient oAuth2RestClient;
    @Mock private RequestContext requestContext;
    @Mock private Invoker invoker;

    private RestClientFactory factory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        factory = new RestClientFactory(jiraAttributes, oAuth2RestClient, requestContext, invoker);
    }

    @Test
    @DisplayName("invokeAsUser() delegates to requestContext.invokeAsUser()")
    void testInvokeAsUser_DelegatesToRequestContext() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        assertTrue(factory.invokeAsUser());

        when(requestContext.invokeAsUser()).thenReturn(false);
        assertFalse(factory.invokeAsUser());
    }

    @Test
    @DisplayName("getInstance(true) returns oAuth2RestClient when OAuth creds present and invokeAsUser")
    void testGetInstance_InvokeAsUserAndOAuthPresent_ReturnsOAuth2Client() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

        RestClient result = factory.getInstance(true);

        assertSame(oAuth2RestClient, result);
    }

    @Test
    @DisplayName("getInstance(false) does not return oAuth2RestClient unconditionally — goes through selector")
    void testGetInstance_NotInvokeAsUser_GoesToSelector() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        when(invoker.getAttributes()).thenReturn(new HashMap<>());

        try (var mocked = mockConstruction(AuthRestClient.class)) {
            RestClient result = factory.getInstance(false);
            // With basic creds and no service account mode, selector picks AuthRestClient
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("getInstance(true) goes through selector when OAuth creds NOT present")
    void testGetInstance_InvokeAsUserButNoOAuthCreds_GoesToSelector() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        when(invoker.getAttributes()).thenReturn(new HashMap<>());

        try (var mocked = mockConstruction(AuthRestClient.class)) {
            RestClient result = factory.getInstance(true);
            assertNotNull(result);
        }
    }

    @Test
    @DisplayName("getInstance() delegates to getInstance(requestContext.invokeAsUser())")
    void testGetInstance_NoArgs_DelegatesToRequestContext() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

        RestClient result = factory.getInstance();

        assertSame(oAuth2RestClient, result);
        verify(requestContext, atLeastOnce()).invokeAsUser();
    }

    @Test
    @DisplayName("serviceAccountClient is created lazily and cached on second call")
    void testServiceAccountClient_LazilyCreatedAndCached() {
        // RestClientSelector routes to clientCredentialsSupplier when:
        //   !areBasicCredentialsPresent() && areOAuthCredentialsPresent() && serviceAccountMode
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);

        try (var mockedProvider = mockConstruction(ClientCredentialsTokenProvider.class);
             var mockedOAuth = mockConstruction(OAuth2RestClient.class)) {

            // First call — creates the service account client
            RestClient first = factory.getInstance(false);
            assertNotNull(first);
            assertEquals(1, mockedOAuth.constructed().size());

            // Second call — reuses the cached client (double-checked locking)
            RestClient second = factory.getInstance(false);
            assertNotNull(second);
            assertEquals(1, mockedOAuth.constructed().size()); // Still only 1 constructed

            // Both calls return the same instance
            assertSame(first, second);
        }
    }

    @Test
    @DisplayName("RestClientFactory has @Service annotation")
    void testHasServiceAnnotation() {
        assertTrue(RestClientFactory.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("serviceAccountClient inner null-check false branch: simulate concurrent pre-initialisation")
    void testServiceAccountClient_ConcurrentPreInit_InnerFalseBranch() throws Exception {
        Field field = RestClientFactory.class.getDeclaredField("serviceAccountClient");
        field.setAccessible(true);
        Method method = RestClientFactory.class.getDeclaredMethod("serviceAccountClient");
        method.setAccessible(true);

        // Pre-built mock to simulate what a concurrent thread would have stored.
        OAuth2RestClient prebuilt = mock(OAuth2RestClient.class);
        AtomicReference<Object> callerResult = new AtomicReference<>();

        CountDownLatch lockAcquired = new CountDownLatch(1);
        CountDownLatch releaseLock = new CountDownLatch(1);

        // Hold factory's monitor so the caller thread blocks inside synchronized(this).
        Thread lockHolder = new Thread(() -> {
            synchronized (factory) {
                lockAcquired.countDown();
                try { releaseLock.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        lockHolder.start();
        lockAcquired.await();   // wait until the lock is held

        // At this point: serviceAccountClient == null, factory's lock is held.
        // Launch caller: reads null (outer check true), then blocks on synchronized(factory).
        Thread caller = new Thread(() -> {
            try { callerResult.set(method.invoke(factory)); }
            catch (Exception e) { callerResult.set(e); }
        });
        caller.start();

        // Wait until caller is BLOCKED (i.e. it has passed the outer null-check and is
        // waiting to acquire factory's monitor).  Thread.State.BLOCKED is the precise
        // state a thread enters when it tries to enter a synchronized block it can't acquire.
        while (caller.getState() != Thread.State.BLOCKED) {
            Thread.sleep(1);
        }

        // While the caller is stuck, install a non-null client — simulating the race where
        // another thread has completed initialisation between the outer read and the
        // synchronized block entry.
        field.set(factory, prebuilt);

        // Release the lock.  Caller enters synchronized, finds serviceAccountClient != null
        // (inner check → false branch covered), assigns client = serviceAccountClient, returns.
        releaseLock.countDown();
        caller.join(5_000);

        assertSame(prebuilt, callerResult.get());
    }

    @Test
    @DisplayName("getInstance(false) with OAuth creds but no basic creds and service account disabled returns oAuth2RestClient")
    void testGetInstance_NotInvokeAsUser_OAuthCredsNoBasic_NoServiceAccount_ReturnsOAuth2Client() {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);

        RestClient result = factory.getInstance(false);

        assertSame(oAuth2RestClient, result);
    }
}
