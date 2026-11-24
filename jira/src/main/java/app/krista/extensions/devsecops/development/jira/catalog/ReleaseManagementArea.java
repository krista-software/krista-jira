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
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.extensions.devsecops.development.jira.api.JiraAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "89b2e544-be19-4d1a-a335-dd483d8b77ba")
public final class ReleaseManagementArea {

    private final ReleaseDao releaseDao;
    private final IssueDao issueDao;
    private final TelemetryMetrics telemetryMetrics;
    private final ValidationOrchestrator validationOrchestrator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    private final RequestContext requestContext;
    private final JiraAuditService jiraAuditService;
    private final Logger logger = LoggerFactory.getLogger(ReleaseManagementArea.class);

    @Inject
    public ReleaseManagementArea(ReleaseDao releaseDao, IssueDao issueDao, TelemetryMetrics telemetryMetrics,
                                 ValidationOrchestrator validationOrchestrator,
                                 ErrorHandlingStateManager internalStateManager,
                                 ExtensionResponseGenerator responseGenerator,
                                 RequestContext requestContext, JiraAuditService jiraAuditService) {
        this.releaseDao = releaseDao;
        this.issueDao = issueDao;
        this.telemetryMetrics = telemetryMetrics;
        this.validationOrchestrator = validationOrchestrator;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.requestContext = requestContext;
        this.jiraAuditService = jiraAuditService;
    }

    @CatalogRequest(
            id = "localDomainRequest_dc6e0837-e404-4ef3-8123-172e07c4df75",
            name = "Get release summary",
            description = "Retrieves a summary of releases filtered by various issue fields.",
            area = "Release Management",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field(name = "Total user stories", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Total count of user stories in the release.'")}, options = {})
    @Field(name = "Open user stories", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Number of user stories that are still open and need completion.'")}, options = {})
    @Field(name = "Total epics", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Total count of epics included in the release.'")}, options = {})
    @Field(name = "Total Tasks", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Total count of tasks in the release.'")}, options = {})
    @Field(name = "Open Tasks", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Number of tasks that are still open and pending completion.'")}, options = {})
    @Field(name = "Total Bugs", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Total count of bugs identified in the release.'")}, options = {})
    @Field(name = "Open Bugs", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Number of bugs that are still open and need to be fixed.'")}, options = {})
    @Field.Date(name = "Release day", attributes = {@Attribute(name = "toolTip", value = "'Scheduled date for the release deployment.'")}, options = {})
    @Field(name = "Days remaining", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Number of days remaining until the release date.'")}, options = {})
    @Field.Desc(name = "User stories", type = "[ Entity(Issue) ]",required = false)
    @Field.Desc(name = "Epics", type = "[ Entity(Issue) ]",required = false)
    @Field.Desc(name = "Bugs", type = "[ Entity(Issue) ]",required = false)
    @Field.Desc(name = "Tasks", type = "[ Entity(Issue) ]",required = false)
    public ExtensionResponse getReleaseSummary(
            @Field.Text(name = "Status", attributes = {@Attribute(name = "toolTip", value = "'Filter issues by status (e.g., Open, In Progress, Done, Closed).'")}, options = {}) String status,
            @Field.Text(name = "Assignee", attributes = {@Attribute(name = "toolTip", value = "'Filter issues by assignee name or email to see work assigned to specific team members.'")}, options = {}) String assignee,
            @Field.Text(name = "Priority", attributes = {@Attribute(name = "toolTip", value = "'Filter issues by priority level (e.g., High, Medium, Low, Critical).'")}, options = {}) String priority,
            @Field.Text(name = "Issue type", attributes = {@Attribute(name = "toolTip", value = "'Filter by issue type (e.g., Bug, Story, Task, Epic).'")}, options = {}) String issueType,
            @Field(name = "Page Size", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Number of results to display per page (default: 20).'")}, options = {}) Double pageSize,
            @Field(name = "Page Number", type = "Number", attributes = {@Attribute(name = "toolTip", value = "'Page number to retrieve (default: 1).'")}, options = {}) Double pageNumber,
            @Field.Text(name = "Fix version", attributes = {@Attribute(name = "toolTip", value = "'Filter issues by fix version to see what will be included in a specific release.'")}, options = {}) String fixVersion) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get release summary");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            // Build validation map for provided parameters
            Map<Validator.ValidationResource, String> validationMap = new HashMap<>();
            if (status != null && !status.isEmpty()) {
                validationMap.put(Validator.ValidationResource.STATUS_NAME, status);
            }
            if (assignee != null && !assignee.isEmpty()) {
                validationMap.put(Validator.ValidationResource.USER_ID, assignee);
            }
            if (issueType != null && !issueType.isEmpty()) {
                validationMap.put(Validator.ValidationResource.ISSUE_TYPE, issueType);
            }

            // Perform validation
            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(validationMap);

            if (validationResults.isEmpty()) {
                // Success path - no validation errors
                long pSize = (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20;
                long pNumber = (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1;
                ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, pSize, pNumber, fixVersion);

                telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(response);
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed for getReleaseSummary, errorCount={}", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                Map<String, Object> stateData = new HashMap<>();
                stateData.put(JiraConstants.VALIDATION_RESULT, validationResults);
                stateData.put("Status", status);
                stateData.put("Assignee", assignee);
                stateData.put("Priority", priority);
                stateData.put("Issue type", issueType);
                stateData.put("Page Size", pageSize);
                stateData.put("Page Number", pageNumber);
                stateData.put("Fix version", fixVersion);

                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(stateData));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterForGetReleaseSummary", Map.of(JiraConstants.STATE_ID, stateId,
                                "Status", status != null ? status : "",
                                "Assignee", assignee != null ? assignee : "",
                                "Priority", priority != null ? priority : "",
                                "Issue type", issueType != null ? issueType : "",
                                "Page Size", pageSize != null ? pageSize : 20.0,
                                "Page Number", pageNumber != null ? pageNumber : 1.0,
                                "Fix version", fixVersion != null ? fixVersion : ""));
                telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get release summary", cause);
            logger.error("Error occurred while getting release summary :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting release summary.",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get release summary");
            }
        }
    }

}