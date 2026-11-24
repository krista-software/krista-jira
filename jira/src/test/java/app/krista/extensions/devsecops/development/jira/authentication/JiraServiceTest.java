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
import app.krista.extension.request.RoutingInfo;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JiraService Tests")
class JiraServiceTest {

    @Mock private RequestContext requestContext;
    @Mock private SessionManager sessionManager;
    @Mock private Invoker invoker;
    @Mock private AuthorizationContext authorizationContext;
    @Mock private JiraAttributes jiraAttributes;
    @Mock private RoutingInfo routingInfo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(invoker.getRoutingInfo()).thenReturn(routingInfo);
        when(routingInfo.getRoutingURL(anyString(), any())).thenReturn("http://test.local/jira/");
    }

    @Test
    @DisplayName("scopes constant contains all 5 required scopes")
    void testScopes_ContainsAllRequiredScopes() {
        List<String> scopes = JiraService.scopes;
        assertEquals(5, scopes.size());
        assertTrue(scopes.contains("read:jira-user"));
        assertTrue(scopes.contains("read:jira-work"));
        assertTrue(scopes.contains("write:jira-work"));
        assertTrue(scopes.contains("manage:jira-data-provider"));
        assertTrue(scopes.contains("manage:jira-project"));
    }

    @Test
    @DisplayName("getScopes returns the scopes constant")
    void testGetScopes_ReturnsExpectedScopes() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        assertEquals(JiraService.scopes, service.getScopes());
    }

    @Test
    @DisplayName("getJaxrsId returns JAXRS_ID from JiraConstants")
    void testGetJaxrsId_ReturnsJaxrsId() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        assertEquals(JiraConstants.JAXRS_ID, service.getJaxrsId());
    }

    @Test
    @DisplayName("getAuthorizationPrompt returns AUTHORIZATION_PROMPT from JiraConstants")
    void testGetAuthorizationPrompt_ReturnsPrompt() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        assertEquals(JiraConstants.AUTHORIZATION_PROMPT, service.getAuthorizationPrompt());
    }

    @Test
    @DisplayName("getJiraToken delegates to getToken()")
    void testGetJiraToken_DelegatesToGetToken() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        JiraService spy = spy(service);
        doReturn("test_token").when(spy).getToken();

        String result = spy.getJiraToken();
        assertEquals("test_token", result);
        verify(spy).getToken();
    }

    @Test
    @DisplayName("getJiraTokenForIssues delegates to getToken(accountId, invokerId)")
    void testGetJiraTokenForIssues_DelegatesToGetToken() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        JiraService spy = spy(service);
        doReturn("user_token").when(spy).getToken("acc-123", "inv-456");

        String result = spy.getJiraTokenForIssues("acc-123", "inv-456");
        assertEquals("user_token", result);
        verify(spy).getToken("acc-123", "inv-456");
    }

    @Test
    @DisplayName("getJiraAccessToken(accountId, invokerId) delegates to getAccessToken")
    void testGetJiraAccessToken_WithArgs_DelegatesToGetAccessToken() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        JiraService spy = spy(service);
        OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
        doReturn(mockToken).when(spy).getAccessToken("acc-123", "inv-456");

        OAuth2AccessToken result = spy.getJiraAccessToken("acc-123", "inv-456");
        assertSame(mockToken, result);
        verify(spy).getAccessToken("acc-123", "inv-456");
    }

    @Test
    @DisplayName("getJiraAccessToken() delegates to getAccessToken()")
    void testGetJiraAccessToken_NoArgs_DelegatesToGetAccessToken() {
        JiraService service = new JiraService(requestContext, sessionManager, invoker, authorizationContext, jiraAttributes);
        JiraService spy = spy(service);
        OAuth2AccessToken mockToken = mock(OAuth2AccessToken.class);
        doReturn(mockToken).when(spy).getAccessToken();

        OAuth2AccessToken result = spy.getJiraAccessToken();
        assertSame(mockToken, result);
        verify(spy).getAccessToken();
    }

    @Test
    @DisplayName("JiraService has @Service annotation")
    void testHasServiceAnnotation() {
        assertTrue(JiraService.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("JiraService has @Inject on constructor")
    void testHasInjectAnnotation() throws NoSuchMethodException {
        var constructor = JiraService.class.getConstructor(
                RequestContext.class, SessionManager.class, Invoker.class,
                AuthorizationContext.class, JiraAttributes.class);
        assertTrue(constructor.isAnnotationPresent(javax.inject.Inject.class));
    }
}
