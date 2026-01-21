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
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.JiraAttributes;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SetUpArea provides system-level catalog requests for the JIRA extension.
 * This includes health checks and configuration management capabilities.
 *
 * @author Krista Development Team
 * @version 2.0.22
 */

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "29193690-a37b-4579-a950-d6bf65b623ed")
public class SetUpArea {

    private static final Logger logger = LoggerFactory.getLogger(SetUpArea.class);

    private final RestClientFactory restClientFactory;
    private final JiraAttributes jiraAttributes;
    private final Invoker invoker;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final TelemetryMetrics telemetryMetrics;

    /**
     * Constructs a new SetUpArea with required dependencies.
     * <p>
     * All dependencies are injected via constructor using JSR-330 {@code @Inject} annotation.
     * This ensures proper dependency management and testability.
     * </p>
     *
     * @param restClientFactory Factory for creating authenticated REST clients
     * @param jiraAttributes    JIRA connection configuration and credentials
     * @param invoker          Extension attribute accessor and updater
     * @param requestContext   Current request context and metadata
     * @param authorizationContext Authorization and permission context
     * @param telemetryMetrics Telemetry metrics collector
     * @throws NullPointerException if any parameter is null
     */
    @Inject
    public SetUpArea(RestClientFactory restClientFactory,
                     JiraAttributes jiraAttributes,
                     Invoker invoker,
                     RequestContext requestContext,
                     AuthorizationContext authorizationContext,
                     TelemetryMetrics telemetryMetrics) {
        this.restClientFactory = restClientFactory;
        this.jiraAttributes = jiraAttributes;
        this.invoker = invoker;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.telemetryMetrics = telemetryMetrics;
    }

    /**
     * Performs a comprehensive health check of the JIRA extension.
     * Verifies connectivity, authentication, and basic API functionality.
     *
     * @return ExtensionResponse containing health status and diagnostic information
     */
    @CatalogRequest(
            id = "localDomainRequest_jira_healthcheck",
            name = "Check System Health",
            description = "Verify JIRA connectivity, authentication status, and extension health. Returns comprehensive diagnostic information including connection status, API availability, and configuration validity.",
            area = "SetUp",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Boolean(name = "Is Healthy", required = false, attributes = {@Attribute(name = "toolTip", value = "'Overall health status of the JIRA extension. True indicates all systems operational.'")}, options = {})
    @Field.Text(name = "Status", required = false, attributes = {@Attribute(name = "toolTip", value = "'Current health status: HEALTHY, DEGRADED, or UNHEALTHY.'")}, options = {})
    @Field.Text(name = "JIRA Instance URL", required = false, attributes = {@Attribute(name = "toolTip", value = "'The configured JIRA instance URL being monitored.'")}, options = {})
    @Field.Text(name = "Authentication Method", required = false, attributes = {@Attribute(name = "toolTip", value = "'Authentication method in use: OAuth 2.0 or API Token.'")}, options = {})
    @Field.Boolean(name = "API Accessible", required = false, attributes = {@Attribute(name = "toolTip", value = "'Indicates whether the JIRA API is accessible and responding.'")}, options = {})
    @Field.Text(name = "Response Time (ms)", required = false, attributes = {@Attribute(name = "toolTip", value = "'API response time in milliseconds for health check request.'")}, options = {})
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "toolTip", value = "'Detailed health check message or error information.'")}, options = {})
    @Field.Text(name = "Timestamp", required = false, attributes = {@Attribute(name = "toolTip", value = "'Timestamp when the health check was performed.'")}, options = {})
    public ExtensionResponse checkSystemHealth() {
        logger.info("Initiating JIRA health check");
        long startTime = System.currentTimeMillis();

        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_TOTAL, 1);

            Map<String, Object> healthStatus = new LinkedHashMap<>();
            boolean isHealthy = true;
            String status = "HEALTHY";
            String message = "All systems operational";

            String hostUrl = jiraAttributes.getHostUrl();

            if (hostUrl == null || hostUrl.isBlank()) {
                isHealthy = false;
                status = "UNHEALTHY";
                message = "JIRA Host URL not configured. Please configure the extension settings.";

                healthStatus.put("JIRA Instance URL", "Not configured");
                healthStatus.put("Authentication Method", "Not configured");
                healthStatus.put("API Accessible", false);
                healthStatus.put("Response Time (ms)", "N/A");

                logger.warn("Health check failed: Host URL not configured");
            } else {
                healthStatus.put("JIRA Instance URL", hostUrl);

                String authMethod = jiraAttributes.areOAuthCredentialsPresent() ? "OAuth 2.0" : "API Token";
                healthStatus.put("Authentication Method", authMethod);

                // Verify authentication credentials are present
                boolean hasCredentials = (jiraAttributes.getUserName() != null && !jiraAttributes.getUserName().isBlank() &&
                                         jiraAttributes.getAuthToken() != null && !jiraAttributes.getAuthToken().isBlank()) ||
                                        jiraAttributes.areOAuthCredentialsPresent();

                if (!hasCredentials) {
                    isHealthy = false;
                    status = "UNHEALTHY";
                    message = "Authentication credentials not properly configured";
                    healthStatus.put("API Accessible", false);
                    healthStatus.put("Response Time (ms)", "N/A");
                    logger.warn("Health check failed: Incomplete authentication configuration");
                } else {
                    // Test API connectivity
                    try {
                        RestClient restClient = restClientFactory.getInstance();
                        long apiStartTime = System.currentTimeMillis();

                        Map<String, Object> queryParams = new HashMap<>();
                        queryParams.put("maxResults", 1);

                        try {
                            String jsonResponse = restClient.fetch("project", queryParams, String.class);
                            long apiDuration = System.currentTimeMillis() - apiStartTime;

                            healthStatus.put("Response Time (ms)", String.valueOf(apiDuration));
                            healthStatus.put("API Accessible", true);

                            if (apiDuration > 5000) {
                                status = "DEGRADED";
                                message = "API responding slowly (>5s response time)";
                                isHealthy = false;
                                logger.warn("Performance degradation detected: {}ms response time", apiDuration);
                            } else {
                                logger.info("Health check successful: API responded in {}ms", apiDuration);
                            }
                        } catch (IllegalStateException e) {
                            long apiDuration = System.currentTimeMillis() - apiStartTime;
                            healthStatus.put("Response Time (ms)", String.valueOf(apiDuration));
                            isHealthy = false;
                            status = "UNHEALTHY";
                            healthStatus.put("API Accessible", false);

                            String errorMessage = e.getMessage();
                            logger.error("API health check failed: {}", errorMessage);

                            if (errorMessage != null && errorMessage.contains("401")) {
                                message = "Authentication failed: Invalid credentials. Please verify your User Name and Auth Token.";
                                logger.error("Authentication failure detected (401)");
                            } else if (errorMessage != null && errorMessage.contains("400")) {
                                message = "Bad Request: Invalid API endpoint or malformed request. Please check your configuration.";
                                logger.error("Bad request error detected (400)");
                            } else if (errorMessage != null && errorMessage.contains("404")) {
                                message = "Invalid Host URL: Please verify your JIRA instance URL.";
                                logger.error("Host URL error detected (404)");
                            } else {
                                message = "API error: " + errorMessage;
                            }
                        }

                    } catch (Exception apiException) {
                        isHealthy = false;
                        status = "UNHEALTHY";
                        message = "API connectivity failed: " + apiException.getMessage();
                        healthStatus.put("API Accessible", false);
                        healthStatus.put("Response Time (ms)", "N/A");
                        logger.error("API connectivity check failed: {}", apiException.getMessage(), apiException);
                    }
                }
            }

            healthStatus.put("Is Healthy", isHealthy);
            healthStatus.put("Status", status);
            healthStatus.put("Message", message);
            healthStatus.put("Timestamp", java.time.Instant.now().toString());

            long totalDuration = System.currentTimeMillis() - startTime;
            logger.info("Health check completed: status={}, isHealthy={}, duration={}ms",
                    status, isHealthy, totalDuration);

            if (isHealthy) {
                telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_SUCCESS, 1);
            } else {
                telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);
            }

            return ExtensionResponseFactory.create(healthStatus);

        } catch (Exception cause) {
            long totalDuration = System.currentTimeMillis() - startTime;
            logger.error("Health check failed with exception after {}ms: {}", totalDuration, cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.HEALTHCHECK_REQUEST_FAILURE, 1);

            return ExtensionResponseFactory.create(
                    Map.of(
                            "Is Healthy", false,
                            "Status", "UNHEALTHY",
                            "Message", "Health check failed: " + cause.getMessage(),
                            "Timestamp", java.time.Instant.now().toString()
                    )
            );
        }
    }

    /**
     * Saves or updates the JIRA extension configuration.
     * Allows users to modify connection settings and authentication credentials.
     *
     * @param hostUrl      JIRA instance URL (optional)
     * @param userName     Admin user name/email for API token auth (optional)
     * @param authToken    API authentication token (optional, secured)
     * @param clientId     OAuth 2.0 client ID (optional)
     * @param clientSecret OAuth 2.0 client secret (optional, secured)
     * @return ExtensionResponse indicating success or failure of configuration save
     */
    @CatalogRequest(
            id = "localDomainRequest_jira_save_configuration",
            name = "Save Extension Configuration",
            description = "Save or update JIRA extension configuration settings including host URL, authentication credentials, and OAuth parameters. At least one authentication method (API Token or OAuth 2.0) must be provided.",
            area = "SetUp",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Configuration Saved", required = false, attributes = {@Attribute(name = "toolTip", value = "'Indicates whether the configuration was successfully saved and validated.'")}, options = {})
    @Field.Text(name = "Message", required = false, attributes = {@Attribute(name = "toolTip", value = "'Detailed message about the configuration save operation.'")}, options = {})
    @Field.Text(name = "Authentication Method", required = false, attributes = {@Attribute(name = "toolTip", value = "'The authentication method configured: OAuth 2.0 or API Token.'")}, options = {})
    public ExtensionResponse saveConfiguration(
            @Field.Text(name = "Host URL", required = false, attributes = {@Attribute(name = "toolTip", value = "'JIRA instance URL (e.g., https://your-domain.atlassian.net). Leave blank to keep existing value.'")}, options = {}) String hostUrl,
            @Field.Text(name = "User Name", required = false, attributes = {@Attribute(name = "toolTip", value = "'Admin user email for API token authentication. Leave blank to keep existing value.'")}, options = {}) String userName,
            @Field.Text(name = "Auth Token", required = false, isSecured = true, attributes = {@Attribute(name = "toolTip", value = "'API authentication token. Leave blank to keep existing value.'")}, options = {}) String authToken,
            @Field.Text(name = "Client ID", required = false, attributes = {@Attribute(name = "toolTip", value = "'OAuth 2.0 client ID from JIRA application. Leave blank to keep existing value.'")}, options = {}) String clientId,
            @Field.Text(name = "Client Secret", required = false, isSecured = true, attributes = {@Attribute(name = "toolTip", value = "'OAuth 2.0 client secret from JIRA application. Leave blank to keep existing value.'")}, options = {}) String clientSecret) {

        logger.info("saveConfiguration: hostUrl={}, userName={}, hasAuthToken={}, hasClientId={}, hasClientSecret={}",
                hostUrl != null ? hostUrl : "not provided",
                userName != null ? userName : "not provided",
                authToken != null && !authToken.isBlank(),
                clientId != null && !clientId.isBlank(),
                clientSecret != null && !clientSecret.isBlank());

        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_TOTAL, 1);
            logger.debug("saveConfiguration: Recorded total request telemetry");

            // Get current attributes
            Map<String, Object> currentAttributes = invoker.getAttributes();
            Map<String, Object> newAttributes = new HashMap<>(currentAttributes);
            logger.debug("saveConfiguration: Retrieved current attributes, size={}", currentAttributes.size());

            int updatedFieldsCount = 0;

            // Update only provided values
            if (hostUrl != null && !hostUrl.isBlank()) {
                newAttributes.put(JiraAttributes.HOST_URL, hostUrl);
                logger.debug("saveConfiguration: Updating Host URL to: {}", hostUrl);
                updatedFieldsCount++;
            }

            if (userName != null && !userName.isBlank()) {
                newAttributes.put(JiraAttributes.USER_NAME, userName);
                logger.debug("saveConfiguration: Updating User Name to: {}", userName);
                updatedFieldsCount++;
            }

            if (authToken != null && !authToken.isBlank()) {
                newAttributes.put(JiraAttributes.AUTH_TOKEN, authToken);
                logger.debug("saveConfiguration: Updating Auth Token (value hidden for security)");
                updatedFieldsCount++;
            }

            if (clientId != null && !clientId.isBlank()) {
                newAttributes.put(JiraAttributes.CLIENT_ID, clientId);
                logger.debug("saveConfiguration: Updating Client ID to: {}", clientId);
                updatedFieldsCount++;
            }

            if (clientSecret != null && !clientSecret.isBlank()) {
                newAttributes.put(JiraAttributes.CLIENT_SECRET, clientSecret);
                logger.debug("saveConfiguration: Updating Client Secret (value hidden for security)");
                updatedFieldsCount++;
            }

            logger.info("saveConfiguration: Total fields to update: {}", updatedFieldsCount);

            // Validate new configuration
            logger.debug("saveConfiguration: Starting configuration validation");
            try {
                jiraAttributes.validateAttributes(newAttributes);
                logger.info("saveConfiguration: Configuration validation successful");
            } catch (Exception validationException) {
                logger.error("saveConfiguration: Configuration validation failed - {}", validationException.getMessage(), validationException);
                telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_FAILURE, 1);
                logger.debug("saveConfiguration: Recorded failure telemetry for validation error");

                return ExtensionResponseFactory.create(
                        Map.of(
                                "Configuration Saved", false,
                                "Message", "Configuration validation failed: " + validationException.getMessage()
                        )
                );
            }

            // Update configuration in JiraAttributes
            logger.debug("saveConfiguration: Applying configuration updates");
            jiraAttributes.update(newAttributes);
            logger.info("saveConfiguration: Configuration updated successfully in JiraAttributes");

            // Determine authentication method
            String authMethod = jiraAttributes.areOAuthCredentialsPresent() ? "OAuth 2.0" : "API Token";
            logger.debug("saveConfiguration: Detected authentication method: {}", authMethod);

            telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_SUCCESS, 1);
            logger.debug("saveConfiguration: Recorded success telemetry");
            logger.info("saveConfiguration: Configuration save completed successfully - authMethod={}", authMethod);

            return ExtensionResponseFactory.create(
                    Map.of(
                            "Configuration Saved", true,
                            "Message", "Configuration saved and validated successfully",
                            "Authentication Method", authMethod
                    )
            );

        } catch (Exception cause) {
            logger.error("saveConfiguration: Failed to save configuration - {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.SetUpArea.SAVE_CONFIGURATION_REQUEST_FAILURE, 1);
            logger.debug("saveConfiguration: Recorded failure telemetry for exception");

            return ExtensionResponseFactory.create(cause,
                    "Failed to save configuration: " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }
}

