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

package app.krista.extensions.devsecops.development.jira.api;

import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.JiraService;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 100% coverage tests for {@link JiraAuthUiResource}.
 *
 * <p>The class delegates almost all HTTP work to {@code AbstractAtlassianAuthResource};
 * this test suite focuses on the four override methods this class provides:
 * {@code loadAttributes}, {@code saveAttributes}, {@code onOAuthCallbackSuccess},
 * and {@code removeTokens}.</p>
 */
@DisplayName("JiraAuthUiResource Tests")
class JiraAuthUiResourceTest {

    @Mock
    private JiraAttributes jiraAttributes;

    @Mock
    private JiraService jiraService;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private RequestContext requestContext;

    @Mock
    private KeyValueStore keyValueStore;

    @Mock
    private AuthorizationListener authorizationListener;

    @Mock
    private JiraAuditService jiraAuditService;

    @Mock
    private KeyValueAuditStore keyValueAuditStore;

    private JiraAuthUiResource resource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(requestContext.getInvokerId()).thenReturn("invoker-456");
        when(jiraAuditService.getAuditStore()).thenReturn(keyValueAuditStore);
        resource = new JiraAuthUiResource(
                jiraAttributes, jiraService, sessionManager,
                requestContext, keyValueStore, authorizationListener, jiraAuditService);
    }

    // ── Annotations ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should have @Path('/sdk') annotation")
    void testHasPathAnnotation() {
        javax.ws.rs.Path path = JiraAuthUiResource.class.getAnnotation(javax.ws.rs.Path.class);
        assertNotNull(path);
        assertEquals("/sdk", path.value());
    }

    @Test
    @DisplayName("Should have @Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        var ctor = JiraAuthUiResource.class.getConstructor(
                JiraAttributes.class, JiraService.class, SessionManager.class,
                RequestContext.class, KeyValueStore.class,
                AuthorizationListener.class, JiraAuditService.class);
        assertTrue(ctor.isAnnotationPresent(javax.inject.Inject.class));
    }

    // ── loadAttributes ──────────────────────────────────────────────────────

    @Test
    @DisplayName("loadAttributes returns the injected JiraAttributes instance")
    void testLoadAttributes_ReturnsInjectedAttributes() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod("loadAttributes", String.class);
        m.setAccessible(true);

        JiraAttributes result = (JiraAttributes) m.invoke(resource, "any-key");

        assertSame(jiraAttributes, result);
    }

    @Test
    @DisplayName("loadAttributes with null key still returns the injected JiraAttributes")
    void testLoadAttributes_NullKey_ReturnsInjectedAttributes() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod("loadAttributes", String.class);
        m.setAccessible(true);

        JiraAttributes result = (JiraAttributes) m.invoke(resource, (Object) null);

        assertSame(jiraAttributes, result);
    }

    // ── saveAttributes ──────────────────────────────────────────────────────

    @Test
    @DisplayName("saveAttributes always returns true (Krista attribute store is system of record)")
    void testSaveAttributes_AlwaysReturnsTrue() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "saveAttributes", JiraAttributes.class, String.class);
        m.setAccessible(true);

        boolean result = (boolean) m.invoke(resource, jiraAttributes, "some-key");

        assertTrue(result);
    }

    @Test
    @DisplayName("saveAttributes with null attributes still returns true")
    void testSaveAttributes_NullAttributes_ReturnsTrue() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "saveAttributes", JiraAttributes.class, String.class);
        m.setAccessible(true);

        boolean result = (boolean) m.invoke(resource, null, "some-key");

        assertTrue(result);
    }

    @Test
    @DisplayName("saveAttributes with null key still returns true")
    void testSaveAttributes_NullKey_ReturnsTrue() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "saveAttributes", JiraAttributes.class, String.class);
        m.setAccessible(true);

        boolean result = (boolean) m.invoke(resource, jiraAttributes, null);

        assertTrue(result);
    }

    // ── onOAuthCallbackSuccess ──────────────────────────────────────────────

    @Test
    @DisplayName("onOAuthCallbackSuccess marks the SPECIFIC account authorized")
    void testOnOAuthCallbackSuccess_CallsAuthorizationListener() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "onOAuthCallbackSuccess", String.class, String.class);
        m.setAccessible(true);

        // second arg is the account id from the OAuth state
        m.invoke(resource, "refresh-token-key", "oauth-state");

        verify(authorizationListener, times(1)).authorized("oauth-state");
    }

    @Test
    @DisplayName("onOAuthCallbackSuccess with null key still notifies listener for the account")
    void testOnOAuthCallbackSuccess_NullKey_StillNotifiesListener() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "onOAuthCallbackSuccess", String.class, String.class);
        m.setAccessible(true);

        m.invoke(resource, null, "state");

        verify(authorizationListener, times(1)).authorized("state");
    }

    @Test
    @DisplayName("onOAuthCallbackSuccess with null state still notifies listener")
    void testOnOAuthCallbackSuccess_NullState_StillNotifiesListener() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "onOAuthCallbackSuccess", String.class, String.class);
        m.setAccessible(true);

        m.invoke(resource, "refresh-token-key", null);

        verify(authorizationListener, times(1)).authorized();
    }

    @Test
    @DisplayName("onOAuthCallbackSuccess with blank account id falls back to no-arg authorized()")
    void testOnOAuthCallbackSuccess_BlankAccountId_UsesNoArg() throws Exception {
        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "onOAuthCallbackSuccess", String.class, String.class);
        m.setAccessible(true);

        m.invoke(resource, "refresh-token-key", "   ");   // blank (non-null) -> else branch

        verify(authorizationListener, times(1)).authorized();
        verify(authorizationListener, never()).authorized(anyString());
    }

    @Test
    @DisplayName("onOAuthCallbackSuccess falls back to no-arg when authorized(accountId) throws")
    void testOnOAuthCallbackSuccess_AuthorizedAccountThrows_FallsBackToNoArg() throws Exception {
        doThrow(new RuntimeException("boom")).when(authorizationListener).authorized("acc-1");

        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "onOAuthCallbackSuccess", String.class, String.class);
        m.setAccessible(true);

        m.invoke(resource, "refresh-token-key", "acc-1");   // must not propagate

        verify(authorizationListener, times(1)).authorized("acc-1");
        verify(authorizationListener, times(1)).authorized();   // fallback
    }

    @Test
    @DisplayName("onOAuthCallbackSuccess swallows exception when BOTH authorized calls throw")
    void testOnOAuthCallbackSuccess_BothAuthorizedCallsThrow_Swallowed() throws Exception {
        doThrow(new RuntimeException("boom")).when(authorizationListener).authorized("acc-2");
        doThrow(new RuntimeException("boom-again")).when(authorizationListener).authorized();

        Method m = JiraAuthUiResource.class.getDeclaredMethod(
                "onOAuthCallbackSuccess", String.class, String.class);
        m.setAccessible(true);

        // Inner catch swallows the fallback failure — the reflective call must complete normally.
        m.invoke(resource, "refresh-token-key", "acc-2");

        verify(authorizationListener, times(1)).authorized("acc-2");
        verify(authorizationListener, times(1)).authorized();
    }

    // ── removeTokens ────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeTokens — when OAuth credentials present, removes token from keyValueStore")
    void testRemoveTokens_OAuthPresent_RemovesToken() throws Exception {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        // appWideTokenKey is computed from clientId + clientSecret + extensionType; just verify
        // that keyValueStore.remove is called with a non-null key.
        when(jiraAttributes.getClientId()).thenReturn("client-id");
        when(jiraAttributes.getClientSecret()).thenReturn("client-secret");

        Method m = JiraAuthUiResource.class.getDeclaredMethod("removeTokens", JiraAttributes.class);
        m.setAccessible(true);
        m.invoke(resource, jiraAttributes);

        verify(keyValueStore, times(1)).remove(anyString());
    }

    @Test
    @DisplayName("removeTokens — when OAuth credentials absent, does NOT remove from keyValueStore")
    void testRemoveTokens_OAuthAbsent_DoesNotRemove() throws Exception {
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

        Method m = JiraAuthUiResource.class.getDeclaredMethod("removeTokens", JiraAttributes.class);
        m.setAccessible(true);
        m.invoke(resource, jiraAttributes);

        verify(keyValueStore, never()).remove(anyString());
    }
}
