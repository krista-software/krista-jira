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
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.authentication.AuthorizationListener;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.mockStatic;

@DisplayName("AuthenticationResource Tests")
class AuthenticationResourceTest {

    @Mock private SessionManager sessionManager;
    @Mock private JiraService jiraService;
    @Mock private RequestContext requestContext;
    @Mock private AuthorizationContext authorizationContext;
    @Mock private AuthorizationListener authorizationListener;
    @Mock private EventHandler eventHandler;
    @Mock private JiraAttributes jiraAttributes;
    @Mock private OAuth20Service oAuth20Service;
    @Mock private OAuth2AccessToken oAuth2AccessToken;

    private AuthenticationResource resource;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        resource = new AuthenticationResource(sessionManager, jiraService, requestContext,
                authorizationContext, authorizationListener, eventHandler, jiraAttributes);
    }

    // ── getCallBack ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCallBack returns error HTML when code is null")
    void testGetCallBack_NullCode_ReturnsErrorHtml() {
        String result = resource.getCallBack(null, null);
        assertTrue(result.contains("Authentication Failed") || result.contains("No authorization code"),
                "Null code should return error HTML: " + result);
    }

    @Test
    @DisplayName("getCallBack returns error HTML when code is blank")
    void testGetCallBack_BlankCode_ReturnsErrorHtml() {
        String result = resource.getCallBack("  ", null);
        assertTrue(result.contains("Authentication Failed") || result.contains("No authorization code"),
                "Blank code should return error HTML: " + result);
    }

    @Test
    @DisplayName("getCallBack stores tokens and notifies listener when state is null (no CSRF)")
    void testGetCallBack_NullState_ExchangesCodeAndStoresToken() throws Exception {
        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getAccessToken("auth_code")).thenReturn(oAuth2AccessToken);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getApiSecret()).thenReturn("secret456");
        when(oAuth2AccessToken.getRefreshToken()).thenReturn("rt_xyz");
        when(requestContext.getInvokerId()).thenReturn("inv-001");

        String result = resource.getCallBack("auth_code", null);

        assertTrue(result.contains("Authentication Successful"),
                "Valid callback with null state should succeed: " + result);
        // App-wide key stored (no user-specific key since state is null)
        verify(sessionManager).store("client123#secret456#inv-001", "rt_xyz");
        verify(authorizationListener).authorized();
    }

    @Test
    @DisplayName("getCallBack returns error HTML when OAuth service is null")
    void testGetCallBack_NullOAuthService_ReturnsErrorHtml() {
        when(jiraService.getOauthService()).thenReturn(null);
        when(jiraAttributes.isManagedCredentialsEnabled()).thenReturn(false);
        when(jiraAttributes.getEffectiveClientId()).thenReturn(null);
        when(requestContext.getInvokerId()).thenReturn("inv-001");

        String result = resource.getCallBack("auth_code", null);

        assertTrue(result.contains("not configured") || result.contains("Authentication Failed"),
                "Null OAuth service should return error HTML: " + result);
        verify(authorizationListener, never()).authorized();
    }

    @Test
    @DisplayName("getCallBack returns error HTML when token exchange throws exception")
    void testGetCallBack_ExceptionDuringTokenExchange_ReturnsErrorHtml() throws Exception {
        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getAccessToken("auth_code"))
                .thenThrow(new RuntimeException("OAuth service failure"));

        String result = resource.getCallBack("auth_code", null);

        assertTrue(result.contains("Authentication Failed") || result.contains("failed"),
                "Exception should return error HTML: " + result);
    }

    @Test
    @DisplayName("getCallBack returns failure HTML when refresh token is null")
    void testGetCallBack_NullRefreshToken_ReturnsFailureHtml() throws Exception {
        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getAccessToken("auth_code")).thenReturn(oAuth2AccessToken);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getApiSecret()).thenReturn("secret456");
        when(oAuth2AccessToken.getRefreshToken()).thenReturn(null);
        when(requestContext.getInvokerId()).thenReturn("inv-001");

        String result = resource.getCallBack("auth_code", null);

        assertTrue(result.contains("unsuccessful") || result.contains("Authentication Failed"),
                "Null refresh token should return failure: " + result);
        verify(authorizationListener, never()).authorized();
    }

    @Test
    @DisplayName("getCallBack returns failure HTML when refresh token is blank")
    void testGetCallBack_BlankRefreshToken_ReturnsFailureHtml() throws Exception {
        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getAccessToken("auth_code")).thenReturn(oAuth2AccessToken);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getApiSecret()).thenReturn("secret456");
        when(oAuth2AccessToken.getRefreshToken()).thenReturn("");
        when(requestContext.getInvokerId()).thenReturn("inv-001");

        String result = resource.getCallBack("auth_code", null);

        assertTrue(result.contains("unsuccessful") || result.contains("Authentication Failed"),
                "Blank refresh token should return failure: " + result);
        verify(authorizationListener, never()).authorized();
    }

    @Test
    @DisplayName("getCallBack with no CSRF nonce falls back to using raw state as accountId")
    void testGetCallBack_NoCsrfNonce_FallsBackToRawState() throws Exception {
        // State has a value but no matching nonce → falls back to raw state as accountId
        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getAccessToken("auth_code")).thenReturn(oAuth2AccessToken);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getApiSecret()).thenReturn("secret456");
        when(oAuth2AccessToken.getRefreshToken()).thenReturn("rt_xyz");
        when(requestContext.getInvokerId()).thenReturn("inv-001");

        String result = resource.getCallBack("auth_code", "someAccountId");

        assertTrue(result.contains("Authentication Successful"),
                "Raw state fallback should succeed: " + result);
        // Per-user key stored using raw state as accountId
        verify(sessionManager).store("someAccountId#inv-001", "rt_xyz");
    }

    @Test
    @DisplayName("getCallBack with valid CSRF state stores under user-specific key")
    void testGetCallBack_ValidCsrfState_StoresUserKey() throws Exception {
        // Set up a valid CSRF nonce in the store
        String nonce = "abc123def456";
        long expiry = System.currentTimeMillis() + 600_000; // 10 min from now
        when(sessionManager.get("oauth-state#" + nonce)).thenReturn(String.valueOf(expiry));

        when(jiraService.getOauthService()).thenReturn(oAuth20Service);
        when(oAuth20Service.getAccessToken("auth_code")).thenReturn(oAuth2AccessToken);
        when(oAuth20Service.getApiKey()).thenReturn("client123");
        when(oAuth20Service.getApiSecret()).thenReturn("secret456");
        when(oAuth2AccessToken.getRefreshToken()).thenReturn("rt_xyz");
        when(requestContext.getInvokerId()).thenReturn("inv-001");

        String state = "myAccountId." + nonce;
        String result = resource.getCallBack("auth_code", state);

        assertTrue(result.contains("Authentication Successful"),
                "Valid CSRF state should succeed: " + result);
        // Per-user key stored (accountId#invokerId)
        verify(sessionManager).store("myAccountId#inv-001", "rt_xyz");
        verify(authorizationListener).authorized();
    }

    // ── getJiraWebhookResponse ───────────────────────────────────────────────

    @Test
    @DisplayName("getJiraWebhookResponse invokes eventHandler before building Response for non-empty data")
    void testGetJiraWebhookResponse_NonEmpty_HandlesEvent() {
        String webhookData = "{\"event\":\"jira:issue_updated\"}";

        // The event is dispatched before Response.status() is called.
        // Response.status() throws a LinkageError in unit-test context due to the
        // jersey-2.x / jersey-3.x classpath conflict; we catch it here so the
        // eventHandler verification can still be asserted.
        try {
            resource.getJiraWebhookResponse(webhookData);
        } catch (LinkageError ignored) {
            // Expected in test environment — JAX-RS RuntimeDelegate conflict between
            // jersey-media-multipart:2.35 (javax.*) and jersey-hk2:3.0.3 (jakarta.*).
        }

        verify(eventHandler).handleEvent(eq("Jira Updates"), any());
    }

    @Test
    @DisplayName("getJiraWebhookResponse does not invoke eventHandler for empty data")
    void testGetJiraWebhookResponse_Empty_NoEventDispatched() {
        try {
            resource.getJiraWebhookResponse("");
        } catch (LinkageError ignored) {
            // Expected in test environment — same JAX-RS RuntimeDelegate conflict.
        }

        verify(eventHandler, never()).handleEvent(anyString(), any());
    }

    @Test
    @DisplayName("getJiraWebhookResponse returns Response.OK after handling non-empty data — uses mocked Response")
    void testGetJiraWebhookResponse_NonEmpty_ReturnsOkResponse() {
        String webhookData = "{\"event\":\"jira:issue_updated\"}";

        javax.ws.rs.core.Response.ResponseBuilder mockBuilder =
                mock(javax.ws.rs.core.Response.ResponseBuilder.class);
        javax.ws.rs.core.Response mockResp = mock(javax.ws.rs.core.Response.class);

        try (MockedStatic<javax.ws.rs.core.Response> mockedResponse =
                     mockStatic(javax.ws.rs.core.Response.class)) {
            mockedResponse.when(() -> javax.ws.rs.core.Response.status(
                    any(javax.ws.rs.core.Response.Status.class))).thenReturn(mockBuilder);
            when(mockBuilder.entity(any())).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(mockResp);

            javax.ws.rs.core.Response result = resource.getJiraWebhookResponse(webhookData);

            assertNotNull(result);
            assertSame(mockResp, result);
            verify(eventHandler).handleEvent(eq("Jira Updates"), any());
        }
    }

}
