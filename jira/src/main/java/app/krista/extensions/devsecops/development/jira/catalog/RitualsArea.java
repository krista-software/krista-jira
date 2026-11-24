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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.api.JiraAuditService;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.sdk.atlassian.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.sdk.atlassian.jql.JQLBuilder;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.impl.WebhookDao;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import app.krista.extensions.devsecops.development.jira.util.FieldValidator;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.base.FreeForm;
import com.kristasoft.common.holders.ThreadLocalProxy;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.*;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "87b4a4a9-1870-4c82-abea-bc71b22d3081")
public final class RitualsArea {

    public static final String ERROR_MESSAGE = "ErrorMessage";
    public static final String IS_ERROR = "IsError";
    public static final String LIST_OF_ISSUES = "List of Issues";
    private final IssueDao issueDao;
    private final UserDao userDao;
    private final RequestContext requestContext;
    private final AuthorizationContext authorizationContext;
    private final EventHandler eventHandler;
    private final ValidationOrchestrator validationOrchestrator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    private final TelemetryMetrics telemetryMetrics;
    private final JiraAuditService jiraAuditService;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final Logger LOGGER = LoggerFactory.getLogger(RitualsArea.class);
    private final ProjectDao projectDao;

    @Inject
    public RitualsArea(IssueDao issueDao, UserDao userDao, RequestContext requestContext, AuthorizationContext authorizationContext,
                       EventHandler eventHandler, ValidationOrchestrator validationOrchestrator,
                       ErrorHandlingStateManager internalStateManager, ExtensionResponseGenerator responseGenerator,
                       ProjectDao projectDao, TelemetryMetrics telemetryMetrics, JiraAuditService jiraAuditService) {
        this.issueDao = issueDao;
        this.userDao = userDao;
        this.requestContext = requestContext;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.validationOrchestrator = validationOrchestrator;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.projectDao = projectDao;
        this.telemetryMetrics = telemetryMetrics;
        this.jiraAuditService = jiraAuditService;
    }

    @CatalogRequest(
            id = "localDomainRequest_30916ab2-7bbf-4c1d-902f-ce12adeb2d09",
            name = "Update status of multiple issues",
            description = "Update the status for multiple Jira issues simultaneously to streamline workflow management and bulk operations.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "Updated issues", type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse updateStatusOfMultipleIssues(
            @Field.Text(name = "Status Name", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the new status to apply to all selected issues (e.g., In Progress, Done, Closed).'")}, options = {}) String statusName,
            @Field.Desc(name = "Issue Key", type = "[ Text ]") List<String> issueKey) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Update status of multiple issues");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            // Validate using enhanced validation with field extraction
            String issueKeysJson = GSON_BUILDER.toJson(issueKey);
            Map<Validator.ValidationResource, String> validationParams = Map.of(
                Validator.ValidationResource.ISSUE_KEYS, issueKeysJson,
                Validator.ValidationResource.STATUS_FOR_MULTIPLE_ISSUES, statusName
            );

            List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(validationParams, RitualsArea.class, "updateStatusOfMultipleIssues");

            if (validationResults.isEmpty()) {
                // Validation passed, execute business logic
                List<Issue> updatedIssue = new ArrayList<>();
                issueKey.forEach(key -> {
                    issueDao.updateStatus(key, statusName);
                    updatedIssue.add(issueDao.getIssueFinal(key));
                });

                ExtensionResponse response = ExtensionResponseFactory.create(Map.of("Updated issues", updatedIssue));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS, 1);
                return response;
            } else {
                // Validation failure - create confirmation flow
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_RETRY_ATTEMPT, 1);

                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
                filedMap.put("Status Name", statusName);  // Include ALL original parameters
                filedMap.put("Issue Key", issueKey);
                filedMap.put(JiraConstants.RE_ENTER, false);
                internalStateManager.put(stateId, GSON_BUILDER.toJson(filedMap));

                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterUpdateStatusOfMultipleIssues",  // SubCatalogRequest name
                        Map.of(JiraConstants.STATE_ID, stateId, "Status Name", statusName, "Issue Key", issueKey, JiraConstants.RE_ENTER, false));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Update status of multiple issues", cause);
            LOGGER.error("Authorization failed for updateStatusOfMultipleIssues: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update status of multiple issues", cause);
            LOGGER.error("Illegal caller for updateStatusOfMultipleIssues: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE, 1);
            throw new RuntimeException("Invalid caller for update status operation", cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update status of multiple issues", cause);
            LOGGER.error("Unexpected error updating multiple issues: {}", cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating multiple issues",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Update status of multiple issues");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_f39c66d5-0862-4b3f-a6bb-badadf5ce7d0",
            name = "Add a comment",
            description = "Add a comment to a specific Jira issue or task to provide updates, feedback, or additional information for team collaboration.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Boolean(name = "Is successful", required = false, attributes = {@Attribute(name = "toolTip", value = "'Confirmation indicating whether the comment was successfully added to the issue.'")}, options = {})
    public ExtensionResponse addAComment(
            @Field(name = "Comment", type = "Paragraph",required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter your comment text. You can provide updates, feedback, or any relevant information about the issue.'")}, options = {}) String comment,
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the issue key where you want to add the comment (e.g., PROJ-123).'")}, options = {}) String issueKey,
            @Field.PickOne(name = "Comment Visibility", values = {"Public", "Internal"}, required = false,
                    attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Choose comment visibility: Public (visible to all users) or Internal (visible only to team members).'")}, options = {}) String commentVisibility) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Add a comment");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                try {
                    issueDao.addComment(issueKey, comment, commentVisibility);
                    telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_SUCCESS, 1);
                    return ExtensionResponseFactory.create(Map.of("Is successful", true));
                } catch (MustAuthorizeException cause) {
                    telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_FAILURE, 1);
                    throw new MustAuthorizeException("Please authenticate yourself." + cause.getMessage());
                } catch (Exception cause) {
                    telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_FAILURE, 1);
                    return ExtensionResponseFactory.create(cause, "Failed to add comment to the issue",
                            ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
                }
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_RETRY_ATTEMPT, 1);
                LOGGER.debug("Validation failed for addComment, issueKey={}, errorCount={}", issueKey, validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForAddAComment", Map.of(JiraConstants.STATE_ID, stateId,
                            JiraResources.TICKET_ID, issueKey,
                            JiraConstants.COMMENT, comment,
                            COMMENT_VISIBILITY, commentVisibility));
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException | IllegalCallerException cause) {
            auditRecorded = true;
            if (cause instanceof MustAuthorizeException) {
                jiraAuditService.authFailure(invokerId, "Add a comment", cause);
            } else {
                jiraAuditService.failure(invokerId, "Add a comment", cause);
            }
            LOGGER.error("Failed to add comment to issue {}: {}", issueKey, cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_FAILURE, 1);
            throw cause instanceof MustAuthorizeException ? (MustAuthorizeException) cause : new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Add a comment", cause);
            LOGGER.error("Unexpected error adding comment to issue {}: {}", issueKey, cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while adding comment in issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Add a comment");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_21823408-60d2-4cec-b39a-b98f806c0638",
            name = "Get user issues",
            description = "Retrieve all issues associated with a specific user account to track their workload and assigned tasks.",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "User Issues", type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse getUserIssues(
            @Field.Text(name = "User Account Id", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the unique account ID of the user whose issues you want to retrieve.'")}, options = {}) String userAccountId) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get user issues");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, userAccountId));
            if (validationResults.isEmpty()) {
                // Execute business logic first
                List<Issue> userIssues = issueDao.searchByJQL(" assignee in (" + userAccountId + ") order by priority DESC, duedate DESC", 30, 1);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("User Issues", userIssues));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_RETRY_ATTEMPT, 1);
                LOGGER.debug("Validation failed for getUserIssues, userAccountId={}, errorCount={}", userAccountId, validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForGetUserIssues", Map.of(JiraConstants.STATE_ID, stateId,
                            JiraResources.ACCOUNT_ID, userAccountId));
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get user issues", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get user issues", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get user issues", cause);
            LOGGER.error("Error occurred while get issues by user account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while get issues by user account Id",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get user issues");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_c7a4f0e2-9b3d-4e51-8a6c-2f7d1b9e0a34",
            name = "Get issues by user names",
            description = "Look up the Jira issues currently ASSIGNED to one or more people, identified by name rather than by Jira account ID. "
                    + "Use this when the request mentions people by their display name, full name, or username (for example \"show me the open tickets for Alice and Bob\", "
                    + "\"what is John working on\", or \"list issues assigned to these team members\") and you do NOT already have their Jira account IDs. "
                    + "Provide the list of names in the 'User Names' input; this request resolves each name to its Jira account(s) for you, so no prior account-ID lookup is needed. "
                    + "It searches on ASSIGNEE only (not reporter, watcher, or creator). "
                    + "Behavior: a name that matches more than one Jira user includes the issues of ALL matching users; names that match no user are skipped; if none of the names match any user, an empty list is returned instead of an error. "
                    + "Returns a single combined list of issues across everyone named, ordered by priority (highest first) then due date, capped at 30 issues. "
                    + "If you already have a Jira account ID and want a single user's issues, use \"Get user issues\" instead; to find someone's account ID, use \"Get Users Account Id\".",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]", required = false,
            description = "The combined list of Jira issues assigned to the named users. Each entry is a full Issue entity (key, summary, status, priority, assignee, due date, etc.). Empty if none of the given names match a Jira user or the matched users have no assigned issues.")
    public ExtensionResponse getIssuesByUserNames(
            @Field.Desc(name = "User Names", type = "[ Text ]", required = true,
                    description = "The people whose assigned Jira issues you want, given as a list of names — display name, full name, or username (email or account ID also work, since they are matched the same way). "
                            + "Provide one entry per person, e.g. [\"Alice Smith\", \"Bob\", \"jdoe\"]. Partial names are allowed and may match several users, in which case all matching users' issues are included. "
                            + "Do not pass a single comma-separated string; use a proper list with one name per element. At least one name is required.")
            List<String> userNames) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get issues by user names");
        boolean auditRecorded = false;
        try {
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_BY_USER_NAMES_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            // Resolve every name to its Jira account id(s). An ambiguous name (multiple
            // matches) contributes all of its account ids; a name matching nobody contributes
            // nothing. Order is preserved and duplicates across names are collapsed.
            Set<String> accountIds = new LinkedHashSet<>();
            if (userNames != null) {
                for (String name : userNames) {
                    if (name == null || name.isBlank()) {
                        continue;
                    }
                    for (JiraUser user : userDao.searchUser(name.trim())) {
                        if (user.accountId != null && !user.accountId.isBlank()) {
                            accountIds.add(user.accountId);
                        }
                    }
                }
            }

            if (accountIds.isEmpty()) {
                // No name resolved to a user — return an empty list rather than an error.
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_BY_USER_NAMES_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Issues", new ArrayList<Issue>()));
            }

            String jql = " assignee in (" + String.join(", ", accountIds) + ") order by priority DESC, duedate DESC";
            List<Issue> issues = issueDao.searchByJQL(jql, 30, 1);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_BY_USER_NAMES_REQUEST_SUCCESS, 1);
            return ExtensionResponseFactory.create(Map.of("Issues", issues));
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get issues by user names", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_BY_USER_NAMES_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get issues by user names", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_BY_USER_NAMES_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get issues by user names", cause);
            LOGGER.error("Error occurred while getting issues by user names :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_BY_USER_NAMES_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting issues by user names",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get issues by user names");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_054845ea-7fc1-4ad6-b1f3-5eb96a784daa",
            name = "Update status of issue",
            description = "Change the status of a specific Jira issue to reflect its current progress in the workflow (e.g., from 'To Do' to 'In Progress').",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)", required = false)
    public ExtensionResponse updateStatusOfIssue(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the issue whose status you want to update (e.g., PROJ-123).'")}, options = {}) String issueKey,
            @Field.Text(name = "Status Name", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the new status name (e.g., In Progress, Done, Closed, Testing).'")}, options = {}) String statusName) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Update status of issue");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.TICKET_ID, issueKey,
                            Validator.ValidationResource.STATUS_NAME, statusName));
            if (validationResults.isEmpty()) {
                // Success path
                issueDao.updateStatus(issueKey, statusName);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of(
                        "Updated Issue", issueDao.getIssueFinal(issueKey)));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForUpdateStatusOfIssue", Map.of(JiraConstants.STATE_ID, stateId,
                            JiraResources.TICKET_ID, issueKey,
                            JiraResources.STATUS_NAME, statusName));
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Update status of issue", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update status of issue", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update status of issue", cause);
            LOGGER.error("Error occurred while updating status of issue :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating status of issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Update status of issue");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_33949891-0ba1-43dd-8d2f-170596a74a00",
            name = "Assign a task to user",
            description = "Assign a Jira task or issue to a specific user to delegate work and ensure clear ownership of responsibilities.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "Updated issue details", type = "Entity(Issue)")
    public ExtensionResponse assignATaskToUser(
            @Field.Text(name = "To assign user account id", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the account ID of the user you want to assign this task to.'")}, options = {}) String toAssignUserAccountId,
            @Field.Text(name = "Issue key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the issue/task you want to assign (e.g., PROJ-123).'")}, options = {}) String issueKey) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Assign a task to user");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey, Validator.ValidationResource.USER_ID, toAssignUserAccountId));
            if (validationResults.isEmpty()) {
                // Success path
                issueDao.assignTask(issueKey, toAssignUserAccountId);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of(
                        "Updated issue details", issueDao.getIssueFinal(issueKey)));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterAssignATaskToUser", Map.of(
                            JiraConstants.STATE_ID, stateId,
                            JiraResources.TICKET_ID, issueKey, JiraResources.ACCOUNT_ID, toAssignUserAccountId));
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Assign a task to user", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Assign a task to user", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Assign a task to user", cause);
            LOGGER.error("Error occurred while assigning task to user :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while assigning task to user",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Assign a task to user");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_ac7c50a8-f72f-4b71-870b-d3db381a7b05",
            name = "Fetch by JQL query",
            description = "Initiate a search operation in Jira using JQL (Jira Query Language) syntax to find issues based on complex criteria and filters.",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM)
    @Field.Text(name = "Task ID", required = false, attributes = {@Attribute(name = "toolTip", value = "'Unique reference ID for this query execution. Use this ID to retrieve the results later.'")}, options = {})
    public ExtensionResponse fetchByJQLQuery(@Field(name = "JQL Query", type = "Paragraph", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter your JQL query (e.g., project = PROJ AND status = \"In Progress\" AND assignee = currentUser()).'")}, options = {}) String jQLQuery) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Fetch by JQL query");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_COUNT, 1);

            boolean invokeAsUser = requestContext.invokeAsUser();
            if (invokeAsUser) {
                issueDao.validateCredentials();
            }
            String accountID = authorizationContext.getAuthorizedAccount().getAccountId();
            final String taskId = UUID.randomUUID().toString();
            Map<Class<?>, Object> threadLocals = ThreadLocalProxy.getAll();
            executorService.submit(() -> {
                ThreadLocalProxy.setAll(threadLocals);
                FreeForm freeForm = new FreeForm();
                try {
                    List<Issue> issues = issueDao.searchByJqlQuery(invokeAsUser, accountID, invokerId, jQLQuery, JiraConstants.DEFAULT_PAGE_SIZE);
                    LOGGER.info("fetchByJQLQuery taskId: {} issues size: {}", taskId, issues.size());
                    freeForm.put(IS_ERROR, "Switch", false);
                    freeForm.put("Data", "[ Entity(Issue) ]", issues);
                    telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_SUCCESS, 1);
                } catch (Exception cause) {
                    LOGGER.error("JQL query failed taskId={} jql=[{}] error={}", taskId, jQLQuery, cause.getMessage(), cause);
                    freeForm.put(IS_ERROR, "Switch", false);
                    freeForm.put("Data", "[ Entity(Issue) ]", Collections.emptyList());
                    telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_FAILURE, 1);
                }
                eventHandler.handleEvent(taskId, freeForm);
            });
            LOGGER.info("Initiated async JQL query execution, taskId={}", taskId);
            return ExtensionResponseFactory.create(Map.of("Task ID", taskId));
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Fetch by JQL query", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Fetch by JQL query", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Fetch by JQL query", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause.getMessage(), ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants(cause.getMessage(), List.of())),
                    null, Map.of());
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Fetch by JQL query");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_51d3362e-bce5-47b5-b2f0-c458522dcfcb",
            name = "Get Results for Fetch Issues by JQL Query",
            description = "Retrieve the results of a previously initiated JQL query using its Task ID to get the list of matching Jira issues.",
            area = "Rituals",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "List of Issues", type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse getResultsForFetchIssuesByJQLQuery(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData,
            @Field.Text(name = "Task ID", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the Task ID returned from the initial JQL query execution.'")}, options = {}) String taskID) {
        LOGGER.info(" Started getResultsForFetchIssuesByJQLQuery taskID : {} & eventName : {}", taskID, eventName);
        if (eventName.equals(taskID)) {
            if (Boolean.TRUE.equals(eventData.get(IS_ERROR))) {
                LOGGER.error("Invalid JQL query :{}", eventData.get(ERROR_MESSAGE));
                return ExtensionResponseFactory.create(eventData.get(ERROR_MESSAGE).toString(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants(eventData.get(ERROR_MESSAGE).toString(), List.of())),
                        null, Map.of());
            } else {
                try {
                    List<Issue> issueList = (List<Issue>) eventData.get("Data");
                    LOGGER.info(" issueList size : {}", issueList.size());
                    if (issueList.isEmpty()) {
                        return ExtensionResponseFactory.create(Map.of(
                                LIST_OF_ISSUES, Collections.emptyList()));
                    }
                    return ExtensionResponseFactory.create(Map.of(LIST_OF_ISSUES, issueList));
                } catch (MustAuthorizeException cause) {
                    LOGGER.error(cause.getMessage(), cause);
                    throw cause;
                } catch (ClassCastException cause) {
                    LOGGER.debug("Unable to cast eventData to List<Issue> : {}", cause.getMessage(), cause);
                    return ExtensionResponseFactory.create(Map.of(
                            LIST_OF_ISSUES, Collections.emptyList()));
                }
            }
        }
        throw new IllegalStateException("Invalid task ID");
    }

    @CatalogRequest(
            id = "localDomainRequest_9f1e46ff-b952-454e-b19f-ea6c79c3c2e0",
            name = "Get my issues by given statuses",
            description = "Fetch issues assigned to the current user filtered by specific status values to track your personal workload and progress.",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getMyIssuesByGivenStatuses(
            @Field.Desc(name = "Status Names", type = "[ Text ]") List<String> statusNames) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get my issues by given statuses");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            // Validate status names using enhanced validation with field extraction
            String statusNamesJson = GSON_BUILDER.toJson(statusNames);
            Map<Validator.ValidationResource, String> validationParams = Map.of(
                Validator.ValidationResource.STATUS_NAMES, statusNamesJson
            );

            List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(validationParams, RitualsArea.class, "getMyIssuesByGivenStatuses");

            if (validationResults.isEmpty()) {
                // Execute business logic first
                String statusCommaSeparated = statusNames.stream()
                        .map(status -> "'" + status + "'")
                        .collect(Collectors.joining(","));
                List<Issue> issues = issueDao.searchByJQL("status in (" + statusCommaSeparated + ") AND assignee in (currentUser()) ORDER BY priority DESC, updated DESC", 30, 1);

                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Issues", issues));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_RETRY_ATTEMPT, 1);

                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
                filedMap.put("Status Names", statusNames);
                filedMap.put(JiraConstants.RE_ENTER, false);
                internalStateManager.put(stateId, GSON_BUILDER.toJson(filedMap));

                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetMyIssuesByGivenStatuses",
                        Map.of(JiraConstants.STATE_ID, stateId, "Status Names", statusNames, JiraConstants.RE_ENTER, false));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get my issues by given statuses", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get my issues by given statuses", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get my issues by given statuses", cause);
            LOGGER.error("Invalid query provided, Please check :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create("Invalid query provided, Please check.", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Invalid query provided, Please check.", List.of())),
                    null, null);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get my issues by given statuses");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_554940f4-188c-47eb-b0b6-768fc99b1399",
            name = "Get issues",
            description = "Retrieve issues filtered by various criteria such as summary, status, priority, assignee, and project to find specific tickets efficiently.",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "List of Filtered Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getIssues(
            @Field.Text(name = "Summary", required = false, attributes = {@Attribute(name = "toolTip", value = "'Filter issues by text in the summary/title (partial matches supported).'")}, options = {}) String summary,
            @Field.Text(name = "Priority", required = false, attributes = {@Attribute(name = "toolTip", value = "'Filter by priority level (e.g., High, Medium, Low, Critical).'")}, options = {}) String priority,
            @Field.Text(name = "Issue Type", required = false, attributes = {@Attribute(name = "toolTip", value = "'Filter by issue type (e.g., Bug, Story, Task, Epic).'")}, options = {}) String issueType,
            @Field.Text(name = "Status", required = false, attributes = {@Attribute(name = "toolTip", value = "'Filter by current status (e.g., Open, In Progress, Done, Closed).'")}, options = {}) String status,
            @Field.Text(name = "Assignee", required = false, attributes = {@Attribute(name = "toolTip", value = "'Filter by assignee name or email address.'")}, options = {}) String assignee,
            @Field.Text(name = "Project Key", required = false, attributes = {@Attribute(name = "toolTip", value = "'Filter by project key (e.g., PROJ, DEV, TEST).'")}, options = {}) String projectKey,
            @Field(name = "Page Size", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Number of results per page (default: 20).'")}, options = {}) Double pageSize,
            @Field(name = "Page Number", type = "Number", required = false, attributes = {@Attribute(name = "toolTip", value = "'Page number to retrieve (default: 1).'")}, options = {}) Double pageNumber) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get Issues");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
            if (projectKey != null && !projectKey.isBlank()) {
                validationResults.addAll(validationOrchestrator.validate(
                        Map.of(Validator.ValidationResource.PROJECT_KEY, FieldValidator.getNullOrEmptyDefault(projectKey))));
            }
            if (issueType != null && !issueType.isBlank() && (projectKey != null && !projectKey.isBlank() && projectDao.isProjectExist(FieldValidator.getNullOrEmptyDefault(projectKey)))) {
                validationResults = validationOrchestrator.validate(
                        Map.of(Validator.ValidationResource.ISSUE_TYPE, FieldValidator.getNullOrEmptyDefault(issueType)));
            }
            if (validationResults.isEmpty()) {
                // Success path
                String jql = new JQLBuilder().status(status)
                        .assignee(assignee)
                        .priority(priority)
                        .summary(summary)
                        .projectKey(projectKey)
                        .issueType(issueType)
                        .orderBy("priority DESC, updated", "desc")
                        .build();
                List<Issue> issues = issueDao.searchByJQL(jql, (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20, (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("List of Filtered Issues", issues));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
                filedMap.put(JiraConstants.STATUS, status);
                filedMap.put(JiraConstants.ASSIGNEE, assignee);
                filedMap.put(JiraConstants.PRIORITY, priority);
                filedMap.put(JiraConstants.SUMMARY, summary);
                filedMap.put("Page Size", pageSize);
                filedMap.put("Page Number", pageNumber);
                internalStateManager.put(stateId, GSON_BUILDER.toJson(filedMap));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetIssues",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get Issues", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get Issues", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get Issues", cause);
            LOGGER.error("Error occurred while getting issues by using filters :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting issues by using filters " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get Issues");
            }
        }

    }

    @CatalogRequest(
            id = "localDomainRequest_f3f1f7b6-9131-462d-bd1c-b80979b905cc",
            name = "Get user issue by issue type",
            description = "Fetch issues assigned to a specific user filtered by issue type to analyze workload distribution and specialization.",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]")
    public ExtensionResponse getUserIssueByIssueType(
            @Field.Text(name = "User account Id", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the account ID of the user whose issues you want to retrieve.'")}, options = {}) String userAccountId,
            @Field.Text(name = "Issue Type", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the issue type to filter by (e.g., Bug, Story, Task, Epic).'")}, options = {}) String issueType) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get user issue by issue type");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            // Validate using enhanced validation with field extraction
            Map<Validator.ValidationResource, String> validationParams = Map.of(
                Validator.ValidationResource.USER_ID, userAccountId,
                Validator.ValidationResource.GLOBAL_ISSUE_TYPE, issueType
            );

            List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(validationParams, RitualsArea.class, "getUserIssueByIssueType");

            if (validationResults.isEmpty()) {
                // Validation passed, execute business logic
                List<Issue> issues = issueDao.searchByJQL("issuetype = \"" + issueType + "\"  AND assignee in (" + userAccountId + ") order by priority DESC, duedate DESC", 30, 1);

                ExtensionResponse response = ExtensionResponseFactory.create(Map.of("Issues", issues));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_SUCCESS, 1);
                return response;
            } else {
                // Validation failure - create confirmation flow
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_RETRY_ATTEMPT, 1);

                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
                filedMap.put("User account Id", userAccountId);  // Include ALL original parameters
                filedMap.put("Issue Type", issueType);
                filedMap.put(JiraConstants.RE_ENTER, false);
                internalStateManager.put(stateId, GSON_BUILDER.toJson(filedMap));

                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetUserIssueByIssueType",  // SubCatalogRequest name
                        Map.of(JiraConstants.STATE_ID, stateId, "User account Id", userAccountId, "Issue Type", issueType, JiraConstants.RE_ENTER, false));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get user issue by issue type", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get user issue by issue type", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get user issue by issue type", cause);
            LOGGER.error("Invalid query provided, Please check :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create("Invalid query provided, Please check.", ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Invalid query provided, Please check.", List.of())),
                    null, null);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get user issue by issue type");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_2087c0b0-dff9-4001-bbd8-cf9933aa06d5",
            name = "Update an issue",
            description = "Update various properties of a Jira issue including status, assignee, summary, description, priority, and custom fields in a single operation.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    public ExtensionResponse updateAnIssue(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the issue you want to update (e.g., PROJ-123).'")}, options = {}) String issueKey,
            @Field.Text(name = "New Status", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter new status if you want to change it (e.g., In Progress, Done, Closed).'")}, options = {}) String newStatus,
            @Field.Text(name = "New priority", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter new priority level if you want to change it (e.g., High, Medium, Low).'")}, options = {}) String newPriority,
            @Field.Text(name = "New Summary", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter new summary/title if you want to update it.'")}, options = {}) String newSummary,
            @Field(name = "New Description", type = "RichText", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter new description content if you want to update it.'")}, options = {}) String newDescription,
            @Field.Text(name = "New Assignee Name", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter the name of the new assignee if you want to reassign the issue.'")}, options = {}) String newAssigneeName,
            @Field.Text(name = "Work log", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter time spent on this issue (e.g., 2h 30m, 1d 4h).'")}, options = {}) String workLog,
            @Field.Desc(name = "Labels", type = "[ Text ]", required = false) List<String> labels,
            @Field.Desc(name = "Components", type = "[ Text ]", required = false) List<String> components,
            @Field(name = "Custom Fields", type = "FreeForm", required = false, attributes = {@Attribute(name = "toolTip", value = "'JSON object containing custom field updates specific to your Jira configuration.'")}, options = {}) FreeForm customFields) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Update an issue");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (newStatus != null && !newStatus.isEmpty()) {
                if (validationResults.isEmpty()) validationResults = validationOrchestrator.validate(
                        Map.of(Validator.ValidationResource.TICKET_ID, issueKey,
                                Validator.ValidationResource.STATUS_NAME, newStatus));
            }
            if (validationResults.isEmpty()) {
                // Success path
                LOGGER.debug("Assignee status re-entry condition matched");
                Issue oldIssue = issueDao.getIssueFinal(issueKey);
                Issue issue = new Issue();
                issue.key = issueKey;
                issue.status = (newStatus == null) ? oldIssue.status : newStatus;
                issue.priority = newPriority;
                issue.summary = (newSummary == null || newSummary.isBlank()) ? oldIssue.summary : newSummary;
                issue.description = newDescription;
                issue.assigneeName = newAssigneeName;
                issue.labels = labels;
                issue.components = components;
                issueDao.updateIssue(issue, customFields);
                if (workLog != null && !workLog.isBlank()) {
                    addWorklog(issueKey, new Date().getTime(), workLog);
                }
                Issue updatedIssue = issueDao.getIssueFinal(issueKey);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Updated Issue", updatedIssue));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());
                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put("Validation Results", validationResults);
                filedMap.put("New priority", newPriority);
                filedMap.put("New Summary", newSummary);
                filedMap.put("New Description", newDescription);
                filedMap.put("New Assignee Name", newAssigneeName);
                filedMap.put("Work log", workLog);
                filedMap.put("Labels", labels);
                filedMap.put("Components", components);
                filedMap.put("Custom Fields", customFields);
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(filedMap));
                Map<String, Object> subState = new LinkedHashMap<>();
                subState.put(JiraConstants.STATE_ID, stateId);
                subState.put(JiraResources.TICKET_ID, issueKey);
                subState.put(JiraResources.STATUS_NAME, newStatus);

                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterForUpdateAnIssue", subState);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Update an issue", cause);
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update an issue", cause);
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update an issue", cause);
            LOGGER.error("Error occurred while updating issue :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Update an issue");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_3712bbb2-daaa-42d9-9a36-fe0e55738236",
            name = "Update priority of issue",
            description = "Change the priority level of a specific Jira issue to reflect its importance and urgency in the project workflow.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    public ExtensionResponse updatePriorityOfIssue(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the issue whose priority you want to update (e.g., PROJ-123).'")}, options = {}) String issueKey,
            @Field.Text(name = "Priority Name", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the new priority level (e.g., High, Medium, Low, Critical, Blocker).'")}, options = {}) String priorityName) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Update priority of issue");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                // Success path
                TicketPriority priority = new TicketPriority();
                priority.name = priorityName;
                ExtensionResponse response = ExtensionResponseFactory.create(Map.of(
                        "Updated Issue", issueDao.updatePriority(priority, issueKey)));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_REQUEST_SUCCESS, 1);
                return response;
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());
                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults, "Priority Name", priorityName)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterUpdatePriorityOfIssue", Map.of(
                                "stateId", stateId,
                                JiraResources.TICKET_ID, issueKey));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_REQUEST_SUCCESS, 1);
                return response;
            }

        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Update priority of issue", cause);
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update priority of issue", cause);
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update priority of issue", cause);
            LOGGER.error("Error occurred while updating priority of issue :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating priority of issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Update priority of issue");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_ba64194f-f531-453e-b83d-597f646c923f",
            name = "Update the priority of multiple issues",
            description = "Change the priority level for multiple Jira issues simultaneously to efficiently manage workload and project priorities.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Desc(name = LIST_OF_ISSUES, type = "[ Entity(Issue) ]", required = false)
    public ExtensionResponse updateThePriorityOfMultipleIssues(
            @Field.Desc(name = "Issue Keys", type = "[ Text ]") List<String> issueKeys,
            @Field.Text(name = "New Priority", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the new priority level to apply to all selected issues (e.g., High, Medium, Low).'")}, options = {}) String newPriority) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Update the priority of multiple issues");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<Issue> updatedIssue = new ArrayList<>();
            issueKeys.forEach(key -> {
                issueDao.updatePriority(new TicketPriority(newPriority), key);
                updatedIssue.add(issueDao.getIssueFinal(key));
            });
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS, 1);
            return ExtensionResponseFactory.create(Map.of(LIST_OF_ISSUES, updatedIssue));
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Update the priority of multiple issues", cause);
            LOGGER.error(cause.getMessage(), cause);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update the priority of multiple issues", cause);
            LOGGER.error(cause.getMessage(), cause);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update the priority of multiple issues", cause);
            LOGGER.error("Error occurred while updating priority of multiple issues: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating priority of multiple issue",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Update the priority of multiple issues");
            }
        }

    }

    @CatalogRequest(
            id = "localDomainRequest_1e62b36a-609b-4156-acba-8f6f8109c1d0",
            name = "Add worklog",
            description = "Add a time log entry to a Jira issue to track work hours and maintain accurate project time records.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Boolean(name = "Is successful", required = false, attributes = {@Attribute(name = "toolTip", value = "'Confirmation indicating whether the worklog entry was successfully added to the issue.'")}, options = {})
    public ExtensionResponse addWorklog(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the issue to log work against (e.g., PROJ-123).'")}, options = {}) String issueKey,
            @Field.Date(name = "Start Date time", required = true, attributes = {@Attribute(name = "toolTip", value = "'Select the date and time when the work started.'")}, options = {}) Long startDateTime,
            @Field.Text(name = "Time Spent", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the duration of work (e.g., 2h 30m, 1d 4h, 45m).'")}, options = {}) String timeSpent) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Add worklog");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                issueDao.addWorklog(timeSpent, startDateTime, issueKey, "");
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of(
                        "Is successful", true));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults, "Start Date time", startDateTime, "Time Spent", timeSpent)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterAddWorkLog", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Add worklog", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Add worklog", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Add worklog", cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred in work log",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Add worklog");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_3f532311-cd58-4a79-a727-5d12a9409319",
            name = "Get Issue By Key",
            description = "Fetch detailed information about a specific Jira issue using its unique key to view all properties and current status.",
            area = "Rituals",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "Issue", type = "Entity(Issue)")
    public ExtensionResponse getIssueByKey(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the unique key of the issue you want to retrieve (e.g., PROJ-123).'")}, options = {}) String issueKey) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get issue by key");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey));
            if (validationResults.isEmpty()) {
                ExtensionResponse response = ExtensionResponseFactory.create(Map.of(
                        "Issue", issueDao.getIssueFinal(issueKey)));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_SUCCESS, 1);
                return response;
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetIssueByKey", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get issue by key", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get issue by key", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get issue by key", cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting issue by issue key.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get issue by key");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_42220d05-f6f1-447c-918a-b3efbf6e17e2",
            name = "Assign Child Issue to Epic",
            description = "Assign a subtask or child issue to a specified epic to organize work hierarchically and maintain project structure.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Boolean(name = "Is successful", required = false, attributes = {@Attribute(name = "toolTip", value = "'Confirmation indicating whether the child issue was successfully assigned to the epic.'")}, options = {})
    public ExtensionResponse assignChildIssueToEpic(
            @Field.Text(name = "Child Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the subtask/child issue to assign (e.g., PROJ-124).'")}, options = {}) String childIssueKey,
            @Field.Text(name = "Parent Epic Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the parent epic (e.g., PROJ-100).'")}, options = {}) String parentEpicKey) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Assign child issue to epic");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(Validator.ValidationResource.CHILD_ISSUE_KEY, childIssueKey, Validator.ValidationResource.PARENT_EPIC_KEY, parentEpicKey));
            if (validationResults.isEmpty()) {
                // Success path
                issueDao.assignChildIssueToEpic(childIssueKey, parentEpicKey);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Is successful", true));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, initiating SubCatalogRequest flow", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterAssignChildIssueToEpic", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.CHILD_ISSUE_KEY, childIssueKey, JiraResources.PARENT_EPIC_KEY, parentEpicKey));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Assign child issue to epic", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Assign child issue to epic", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Assign child issue to epic", cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while assigning child issue to parent epic.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Assign child issue to epic");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_0e9b3e9b-3e15-43e9-8530-de879cf03e69",
            name = "Assign Issue to Epic",
            description = "Assign a given issue to a specified epic to organize related work items and maintain project hierarchy.",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Boolean(name = "Is successful", required = false, attributes = {@Attribute(name = "toolTip", value = "'Confirmation indicating whether the issue was successfully assigned to the epic.'")}, options = {})
    public ExtensionResponse assignIssueToEpic(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the issue to assign to the epic (e.g., PROJ-123).'")}, options = {}) String issueKey,
            @Field.Text(name = "Epic Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the key of the epic to assign the issue to (e.g., PROJ-100).'")}, options = {}) String epicKey) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Assign issue to epic");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, issueKey, Validator.ValidationResource.EPIC_KEY, epicKey));
            if (validationResults.isEmpty()) {
                // Success path
                issueDao.assignIssueToEpic(issueKey, epicKey);
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Is successful", true));
            } else {
                // Validation failure - retry attempt (Note: No SubCatalogRequest support for this method)
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_RETRY_ATTEMPT, 1);
                LOGGER.info("Validation failed with {} errors, using direct confirmation response", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, GSON_BUILDER.toJson(Map.of(VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterAssignIssueToEpic", Map.of(
                                JiraConstants.STATE_ID, stateId,
                                JiraResources.TICKET_ID, issueKey, JiraResources.EPIC_KEY, epicKey));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Assign issue to epic", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Assign issue to epic", cause);
            LOGGER.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Assign issue to epic", cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_FAILURE, 1);
            JSONObject jsonObject = new JSONObject(cause.getMessage());
            JSONArray errorMessages = (JSONArray) jsonObject.get("errorMessages");
            return ExtensionResponseFactory.create(cause, "Error occurred while assigning issue to epic." + errorMessages.get(0).toString(),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Assign issue to epic");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_a6b3b600-16d6-4ddb-93ba-487d06d1baa8",
            name = "Get Notification",
            description = "Receive webhook-based notifications from Jira with real-time updates about issue changes, status transitions, and other events.",
            area = "Rituals",
            type = CatalogRequest.Type.WAIT_FOR_EVENT)
    @Field.Desc(name = "Update", type = "[ Composite ]", required = false)
    @Field.Text(name = "Issue Key", required = false, attributes = {@Attribute(name = "visualWidth", value = "S"), @Attribute(name = "toolTip", value = "'Key of the issue that triggered the notification event.'")}, options = {})
    public Map<String, Object> getNotification(
            @Field(name = "eventName", type = "Text") String eventName,
            @Field(name = "eventData", type = "FreeForm") FreeForm eventData) {
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_COUNT, 1);

            if (eventName.equals(JIRA_UPDATES)) {
                WebhookDao webhookDao = new WebhookDao();
                Map<String, Object> result = webhookDao.parseWebhookResponse((String) eventData.get(UPDATES_ON_JIRA));
                telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_SUCCESS, 1);
                return result;
            }

            LOGGER.info("No matching event name, returning empty map");
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_SUCCESS, 1);
            return Map.of();
        } catch (Exception cause) {
            LOGGER.error("Error occurred while processing notification: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_FAILURE, 1);
            // Note: This method returns Map<String, Object>, not ExtensionResponse, so we can't return error response
            return Map.of("error", "Error occurred while processing notification: " + cause.getMessage());
        }
    }


    @CatalogRequest(
            id = "localDomainRequest_0a5052cd-bd83-4a0a-82c3-861ed7090a19",
            name = "Get Custom Field Value",
            description = "Get data of custom field",
            area = "Rituals",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "Response", type = "[ Text ]", required = false)
    public List<String> getCustomFieldValue(
            @Field.Text(name = "Issue Key", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String issueKey,
            @Field.Text(name = "Custom Field", required = true, attributes = {@Attribute(name = "visualWidth", value = "S")}, options = {}) String customField) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get custom field value");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<String> result = issueDao.getCustomIssue(issueKey, customField);
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_SUCCESS, 1);
            return result;
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get custom field value", cause);
            LOGGER.error("Error occurred while getting custom field value: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_FAILURE, 1);
            throw cause;
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get custom field value");
            }
        }
    }

}