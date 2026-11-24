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

package app.krista.extensions.devsecops.development.jira.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.api.JiraAuditService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SetUpArea catalog requests.
 * Tests health check and configuration management functionality.
 */
@DisplayName("SetUpArea Tests")
class SetUpAreaTest {

    @Mock
    private RestClientFactory restClientFactory;

    @Mock
    private JiraAttributes jiraAttributes;

    @Mock
    private Invoker invoker;

    @Mock
    private RequestContext requestContext;

    @Mock
    private AuthorizationContext authorizationContext;

    @Mock
    private TelemetryMetrics telemetryMetrics;

    @Mock
    private IssueDao issueDao;

    @Mock
    private JiraAuditService jiraAuditService;

    @Mock
    private RestClient restClient;

    private SetUpArea setUpArea;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        setUpArea = new SetUpArea(
                restClientFactory,
                jiraAttributes,
                invoker,
                requestContext,
                authorizationContext,
                telemetryMetrics,
                issueDao,
                jiraAuditService
        );
    }

    // ========================================
    // Health Check Tests
    // ========================================

    @Test
    @DisplayName("Health check should return HEALTHY when all systems operational")
    void testCheckSystemHealth_AllSystemsOperational_ReturnsHealthy() throws Exception {
        // Arrange
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(restClientFactory.getInstance()).thenReturn(restClient);

        String mockResponse = "[]";
        when(restClient.fetch(eq("project"), any(), eq(String.class))).thenReturn(mockResponse);

        // Act
        ExtensionResponse result = setUpArea.checkSystemHealth();

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_TOTAL, 1);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_SUCCESS, 1);
    }

    @Test
    @DisplayName("Health check should return UNHEALTHY when host URL not configured")
    void testCheckSystemHealth_NoHostUrl_ReturnsUnhealthy() {
        // Arrange
        when(jiraAttributes.getHostUrl()).thenReturn(null);

        // Act
        ExtensionResponse result = setUpArea.checkSystemHealth();

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_TOTAL, 1);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Health check should return UNHEALTHY when API returns error")
    void testCheckSystemHealth_ApiError_ReturnsUnhealthy() throws Exception {
        // Arrange
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
        when(restClientFactory.getInstance()).thenReturn(restClient);

        when(restClient.fetch(eq("project"), any(), eq(String.class)))
            .thenThrow(new IllegalStateException("401 Unauthorized"));

        // Act
        ExtensionResponse result = setUpArea.checkSystemHealth();

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Health check should handle API connectivity exception")
    void testCheckSystemHealth_ApiException_ReturnsUnhealthy() throws Exception {
        // Arrange
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(restClientFactory.getInstance()).thenThrow(new RuntimeException("Network error"));

        // Act
        ExtensionResponse result = setUpArea.checkSystemHealth();

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    // ========================================
    // Save Configuration Tests
    // ========================================

    @Test
    @DisplayName("Save configuration should successfully update all fields")
    void testSaveConfiguration_AllFields_SavesSuccessfully() {
        // Arrange
        Map<String, Object> currentAttributes = new HashMap<>();
        when(invoker.getAttributes()).thenReturn(currentAttributes);
        
        doNothing().when(jiraAttributes).validateAttributes(any());
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

        // Act
        ExtensionResponse result = setUpArea.saveConfiguration(
                "https://new.atlassian.net",
                "test@example.com",
                "newToken123",
                "newClientId",
                "newClientSecret"
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(jiraAttributes).update(any());
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_TOTAL, 1);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_SUCCESS, 1);
    }

    @Test
    @DisplayName("Save configuration should update only provided fields")
    void testSaveConfiguration_PartialFields_UpdatesOnlyProvided() {
        // Arrange
        Map<String, Object> currentAttributes = new HashMap<>();
        currentAttributes.put(JiraAttributes.HOST_URL, "https://old.atlassian.net");
        currentAttributes.put(JiraAttributes.USER_NAME, "old@example.com");
        when(invoker.getAttributes()).thenReturn(currentAttributes);
        
        doNothing().when(jiraAttributes).validateAttributes(any());
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

        // Act
        ExtensionResponse result = setUpArea.saveConfiguration(
                "https://new.atlassian.net",
                null,
                null,
                null,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(jiraAttributes).update(any());
    }

    @Test
    @DisplayName("Save configuration should fail when validation fails")
    void testSaveConfiguration_ValidationFails_ReturnsFailure() {
        // Arrange
        Map<String, Object> currentAttributes = new HashMap<>();
        when(invoker.getAttributes()).thenReturn(currentAttributes);
        
        doThrow(new IllegalArgumentException("Invalid host URL"))
                .when(jiraAttributes).validateAttributes(any());

        // Act
        ExtensionResponse result = setUpArea.saveConfiguration(
                "invalid-url",
                null,
                null,
                null,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());

        verify(jiraAttributes, never()).update(any());
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_TOTAL, 1);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Save configuration should handle exception during save")
    void testSaveConfiguration_ExceptionDuringSave_ReturnsFailure() {
        // Arrange
        Map<String, Object> currentAttributes = new HashMap<>();
        when(invoker.getAttributes()).thenReturn(currentAttributes);
        
        doNothing().when(jiraAttributes).validateAttributes(any());
        doThrow(new RuntimeException("Save failed")).when(jiraAttributes).update(any());

        // Act
        ExtensionResponse result = setUpArea.saveConfiguration(
                "https://new.atlassian.net",
                null,
                null,
                null,
                null
        );

        // Assert
        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.FAILURE, result.getResult());

        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_TOTAL, 1);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_FAILURE, 1);
    }

    // ========================================
    // Additional Health Check Coverage Tests
    // ========================================

    @Test
    @DisplayName("Health check calls validateCredentials when invokeAsUser is true")
    void testCheckSystemHealth_InvokeAsUser_CallsValidateCredentials() throws Exception {
        when(requestContext.invokeAsUser()).thenReturn(true);
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(restClientFactory.getInstance()).thenReturn(restClient);
        when(restClient.fetch(eq("project"), any(), eq(String.class))).thenReturn("[]");

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(issueDao).validateCredentials();
    }

    @Test
    @DisplayName("Health check returns UNHEALTHY when host URL is blank (not null)")
    void testCheckSystemHealth_BlankHostUrl_ReturnsUnhealthy() {
        when(jiraAttributes.getHostUrl()).thenReturn("   ");

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Health check returns UNHEALTHY when credentials are not configured")
    void testCheckSystemHealth_NoCredentials_ReturnsUnhealthy() {
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);
        // getUserName() and getAuthToken() return null by default (Mockito default)

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
        verify(restClientFactory, never()).getInstance();
    }

    @Test
    @DisplayName("Health check with ISE containing 400 returns UNHEALTHY with bad-request message")
    void testCheckSystemHealth_ApiIllegalState_400_ReturnsUnhealthy() {
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(restClientFactory.getInstance()).thenReturn(restClient);
        when(restClient.fetch(eq("project"), any(), eq(String.class)))
                .thenThrow(new IllegalStateException("400 Bad Request"));

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Health check with ISE containing 404 returns UNHEALTHY with invalid-URL message")
    void testCheckSystemHealth_ApiIllegalState_404_ReturnsUnhealthy() {
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(restClientFactory.getInstance()).thenReturn(restClient);
        when(restClient.fetch(eq("project"), any(), eq(String.class)))
                .thenThrow(new IllegalStateException("404 Not Found"));

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Health check with ISE containing generic error message returns UNHEALTHY")
    void testCheckSystemHealth_ApiIllegalState_GenericError_ReturnsUnhealthy() {
        when(jiraAttributes.getHostUrl()).thenReturn("https://test.atlassian.net");
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);
        when(restClientFactory.getInstance()).thenReturn(restClient);
        when(restClient.fetch(eq("project"), any(), eq(String.class)))
                .thenThrow(new IllegalStateException("Connection refused"));

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
    }

    @Test
    @DisplayName("Health check triggers outer catch when telemetry throws")
    void testCheckSystemHealth_TelemetryThrows_OuterCatchHandled() {
        doThrow(new RuntimeException("Metrics unavailable"))
                .when(telemetryMetrics).incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_TOTAL, 1);

        ExtensionResponse result = setUpArea.checkSystemHealth();

        assertNotNull(result);
        verify(jiraAuditService).failure(isNull(), eq("Check System Health"), any(RuntimeException.class));
    }

    // ========================================
    // Additional Save Configuration Coverage Tests
    // ========================================

    @Test
    @DisplayName("Save configuration calls validateCredentials when invokeAsUser is true")
    void testSaveConfiguration_InvokeAsUser_CallsValidateCredentials() {
        when(requestContext.invokeAsUser()).thenReturn(true);
        Map<String, Object> currentAttributes = new HashMap<>();
        when(invoker.getAttributes()).thenReturn(currentAttributes);
        doNothing().when(jiraAttributes).validateAttributes(any());
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(true);

        ExtensionResponse result = setUpArea.saveConfiguration(
                "https://new.atlassian.net", null, null, null, null);

        assertNotNull(result);
        verify(issueDao).validateCredentials();
    }

    @Test
    @DisplayName("Save configuration with all null parameters still calls validation and update")
    void testSaveConfiguration_AllNullParams_ValidationAndUpdate() {
        Map<String, Object> currentAttributes = new HashMap<>();
        when(invoker.getAttributes()).thenReturn(currentAttributes);
        doNothing().when(jiraAttributes).validateAttributes(any());
        when(jiraAttributes.areOAuthCredentialsPresent()).thenReturn(false);

        ExtensionResponse result = setUpArea.saveConfiguration(null, null, null, null, null);

        assertNotNull(result);
        assertEquals(ExtensionResponse.Result.SUCCESS, result.getResult());
        verify(jiraAttributes).update(any());
        verify(jiraAttributes, never()).update(argThat(map ->
                map.containsKey(JiraAttributes.HOST_URL)
                        || map.containsKey(JiraAttributes.USER_NAME)));
    }

}