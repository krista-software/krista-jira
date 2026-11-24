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
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.extensions.util.KeyValueStore;
import app.krista.ksdk.context.RequestContext;
import app.krista.sdk.atlassian.audit.AuditData;
import app.krista.sdk.atlassian.audit.KeyValueAuditStore;
import app.krista.sdk.atlassian.config.AuthMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 100% coverage tests for {@link JiraAuditResource}.
 *
 * <p>Strategy: construct the resource with mocked collaborators and invoke
 * {@code buildHealthResponse()} via reflection (it is protected).
 * The {@code getAuditData}, {@code clearAuditRecords}, and {@code getGlobalCounter}
 * methods are thin delegators; they are tested by verifying delegation to the
 * underlying {@link KeyValueAuditStore} mock.</p>
 */
@DisplayName("JiraAuditResource Tests")
class JiraAuditResourceTest {

    @Mock
    private JiraAuditService jiraAuditService;

    @Mock
    private KeyValueAuditStore keyValueAuditStore;

    @Mock
    private RequestContext requestContext;

    @Mock
    private JiraAttributes jiraAttributes;

    @Mock
    private SessionManager sessionManager;

    private JiraAuditResource resource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(requestContext.getInvokerId()).thenReturn("invoker-123");
        when(jiraAuditService.getAuditStore()).thenReturn(keyValueAuditStore);
        resource = new JiraAuditResource(jiraAuditService, requestContext, jiraAttributes, sessionManager);
    }

    // ── Annotations ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Should have @Path('/sdk/audit') annotation")
    void testHasPathAnnotation() {
        javax.ws.rs.Path path = JiraAuditResource.class.getAnnotation(javax.ws.rs.Path.class);
        assertNotNull(path);
        assertEquals("/sdk/audit", path.value());
    }

    @Test
    @DisplayName("Should have @Inject annotation on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        var ctor = JiraAuditResource.class.getConstructor(
                JiraAuditService.class, RequestContext.class, JiraAttributes.class, SessionManager.class);
        assertTrue(ctor.isAnnotationPresent(javax.inject.Inject.class));
    }

    // ── getAuditData ────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAuditData delegates to auditStore.getData")
    void testGetAuditData_DelegatesToStore() throws Exception {
        AuditData expected = mock(AuditData.class);
        when(keyValueAuditStore.getData("inv", 0L, 100L, "type", "status")).thenReturn(expected);

        Method m = JiraAuditResource.class.getDeclaredMethod(
                "getAuditData", String.class, Long.class, Long.class, String.class, String.class);
        m.setAccessible(true);
        AuditData result = (AuditData) m.invoke(resource, "inv", 0L, 100L, "type", "status");

        assertSame(expected, result);
        verify(keyValueAuditStore).getData("inv", 0L, 100L, "type", "status");
    }

    @Test
    @DisplayName("getAuditData with null parameters delegates correctly")
    void testGetAuditData_NullParams_DelegatesToStore() throws Exception {
        when(keyValueAuditStore.getData(null, null, null, null, null)).thenReturn(null);

        Method m = JiraAuditResource.class.getDeclaredMethod(
                "getAuditData", String.class, Long.class, Long.class, String.class, String.class);
        m.setAccessible(true);
        Object result = m.invoke(resource, null, null, null, null, null);

        assertNull(result);
    }

    // ── clearAuditRecords ───────────────────────────────────────────────────

    @Test
    @DisplayName("clearAuditRecords delegates to auditStore.clearRecords")
    void testClearAuditRecords_DelegatesToStore() throws Exception {
        Method m = JiraAuditResource.class.getDeclaredMethod(
                "clearAuditRecords", String.class, Long.class, Long.class);
        m.setAccessible(true);
        m.invoke(resource, "inv", 0L, 100L);

        verify(keyValueAuditStore).clearRecords("inv", 0L, 100L);
    }

    @Test
    @DisplayName("clearAuditRecords with null timestamps delegates correctly")
    void testClearAuditRecords_NullTimestamps() throws Exception {
        Method m = JiraAuditResource.class.getDeclaredMethod(
                "clearAuditRecords", String.class, Long.class, Long.class);
        m.setAccessible(true);
        m.invoke(resource, "inv", null, null);

        verify(keyValueAuditStore).clearRecords("inv", null, null);
    }

    // ── getGlobalCounter ────────────────────────────────────────────────────

    @Test
    @DisplayName("getGlobalCounter delegates to auditStore.getGlobalCounter")
    void testGetGlobalCounter_DelegatesToStore() throws Exception {
        Map<String, Object> expected = Map.of("total", 42);
        when(keyValueAuditStore.getGlobalCounter()).thenReturn(expected);

        Method m = JiraAuditResource.class.getDeclaredMethod("getGlobalCounter");
        m.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) m.invoke(resource);

        assertSame(expected, result);
        verify(keyValueAuditStore).getGlobalCounter();
    }

    // ── buildHealthResponse — BASIC ─────────────────────────────────────────

    @Test
    @DisplayName("buildHealthResponse — basic credentials: returns BASIC auth method")
    void testBuildHealthResponse_BasicCredentials_ReturnsBasicAuthMethod() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.getUserName()).thenReturn("user@example.com");

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals(AuthMethod.BASIC.name(), result.get("authMethod"));
        assertEquals("https://test.atlassian.net", result.get("hostUrl"));
        assertEquals("user@example.com", result.get("userName"));
        assertEquals("N/A", result.get("hasRefreshToken"));
    }

    // ── buildHealthResponse — OAUTH2 (3LO) ─────────────────────────────────

    @Test
    @DisplayName("buildHealthResponse — OAuth, not service account, has refresh token")
    void testBuildHealthResponse_OAuth_NotServiceAccount_HasRefreshToken() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");
        // invokerId is "invoker-123" (set in setUp)
        String expectedAppKey = "client-id#client-secret#invoker-123";
        when(sessionManager.get(expectedAppKey)).thenReturn("some-refresh-token");

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals(AuthMethod.OAUTH2.name(), result.get("authMethod"));
        assertEquals(true, result.get("hasRefreshToken"));
    }

    @Test
    @DisplayName("buildHealthResponse — OAuth, not service account, refresh token absent")
    void testBuildHealthResponse_OAuth_NotServiceAccount_NoRefreshToken() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");
        when(sessionManager.get(anyString())).thenReturn(null);

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals(AuthMethod.OAUTH2.name(), result.get("authMethod"));
        assertEquals(false, result.get("hasRefreshToken"));
    }

    @Test
    @DisplayName("buildHealthResponse — OAuth, not service account, refresh token is empty string")
    void testBuildHealthResponse_OAuth_NotServiceAccount_EmptyRefreshToken() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");
        when(sessionManager.get(anyString())).thenReturn("");

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals(AuthMethod.OAUTH2.name(), result.get("authMethod"));
        assertEquals(false, result.get("hasRefreshToken"));
    }

    // ── buildHealthResponse — OAUTH2_CLIENT_CREDENTIALS (2LO) ──────────────

    @Test
    @DisplayName("buildHealthResponse — OAuth, service account: returns OAUTH2_CLIENT_CREDENTIALS")
    void testBuildHealthResponse_OAuth_ServiceAccount_ReturnsClientCredentials() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(true);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals(AuthMethod.OAUTH2_CLIENT_CREDENTIALS.name(), result.get("authMethod"));
        assertEquals("N/A", result.get("hasRefreshToken"));
        // Should NOT call sessionManager for service-account mode
        verify(sessionManager, never()).get(anyString());
    }

    // ── buildHealthResponse — not configured ───────────────────────────────

    @Test
    @DisplayName("buildHealthResponse — no credentials: returns 'not configured'")
    void testBuildHealthResponse_NoCredentials_NotConfigured() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals("not configured", result.get("authMethod"));
        assertEquals(false, result.get("hasRefreshToken"));
    }

    // ── buildHealthResponse — exception branch ─────────────────────────────

    @Test
    @DisplayName("buildHealthResponse — exception from areBasicCredentialsPresent: returns error state")
    void testBuildHealthResponse_ExceptionFromAttributes_ReturnsError() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent())
                .thenThrow(new RuntimeException("internal error"));

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals("error", result.get("authMethod"));
        assertEquals(false, result.get("hasRefreshToken"));
    }

    @Test
    @DisplayName("buildHealthResponse — exception from sessionManager.get: returns error state")
    void testBuildHealthResponse_ExceptionFromSessionManager_ReturnsError() throws Exception {
        when(jiraAttributes.areBasicCredentialsPresent()).thenReturn(false);
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(jiraAttributes.isServiceAccountModeEnabled()).thenReturn(false);
        when(jiraAttributes.getEffectiveClientId()).thenReturn("client-id");
        when(jiraAttributes.getEffectiveClientSecret()).thenReturn("client-secret");
        when(sessionManager.get(anyString())).thenThrow(new RuntimeException("kv failure"));

        Map<String, Object> result = invokeBuildHealthResponse();

        assertEquals("error", result.get("authMethod"));
        assertEquals(false, result.get("hasRefreshToken"));
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeBuildHealthResponse() throws Exception {
        Method m = JiraAuditResource.class.getDeclaredMethod("buildHealthResponse");
        m.setAccessible(true);
        return (Map<String, Object>) m.invoke(resource);
    }
}
