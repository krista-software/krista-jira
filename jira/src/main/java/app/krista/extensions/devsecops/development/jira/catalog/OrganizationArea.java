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
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.Attribute;
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Field;
import app.krista.model.base.FreeForm;
import app.krista.extensions.devsecops.development.jira.api.JiraAuditService;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Bug;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.services.UserService;
import app.krista.extensions.devsecops.development.jira.util.FieldValidator;
import app.krista.extensions.devsecops.development.jira.util.IssueMapper;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.base.File;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

import static app.krista.extensions.devsecops.development.jira.util.JiraConstants.GSON;

@Domain(id = "catEntryDomain_7edc1712-4403-4a82-ad44-7eb22bbf2812",
        name = "Development",
        ecosystemId = "catEntryEcosystem_98c365b2-ab5a-463a-ac13-6da05a67c417",
        ecosystemName = "Dev Sec Ops",
        ecosystemVersion = "87b4a4a9-1870-4c82-abea-bc71b22d3081")
public final class OrganizationArea {
    private final Invoker invoker;
    private final IssueDao issueDao;
    private final UserDao userDao;
    private final UserService userService;
    private final KristaMediaClient kristaMediaClient;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final ValidationOrchestrator validationOrchestrator;
    private final TelemetryMetrics telemetryMetrics;
    private final RequestContext requestContext;
    private final JiraAuditService jiraAuditService;
    private final Logger logger = LoggerFactory.getLogger(OrganizationArea.class);

    @Inject
    public OrganizationArea(Invoker invoker, IssueDao issueDao, UserDao userDao, UserService userService,
                            KristaMediaClient kristaMediaClient,
                            ExtensionResponseGenerator responseGenerator,
                            ErrorHandlingStateManager internalStateManager, ValidationOrchestrator validationOrchestrator,
                            TelemetryMetrics telemetryMetrics, RequestContext requestContext,
                            JiraAuditService jiraAuditService) {
        this.invoker = invoker;
        this.issueDao = issueDao;
        this.userDao = userDao;
        this.userService = userService;
        this.kristaMediaClient = kristaMediaClient;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
        this.validationOrchestrator = validationOrchestrator;
        this.telemetryMetrics = telemetryMetrics;
        this.requestContext = requestContext;
        this.jiraAuditService = jiraAuditService;
    }

    /**
     * Converts the structured "Custom Fields" input — a list of
     * {@code { "Key": <name>, "Value": <value> }} entries — into a {@code name -> value}
     * map, the shape the DAO's custom-field resolver expects. Uses a plain map (NOT FreeForm:
     * FreeForm's 2-arg put(key,value) is unsupported and throws). Null/blank names are
     * skipped; a null/empty list yields an empty map (field stays optional).
     */
    private static Map<String, Object> toCustomFieldMap(List<Map<String, Object>> rows) {
        Map<String, Object> customFields = new LinkedHashMap<>();
        if (rows != null) {
            for (Map<String, Object> row : rows) {
                if (row == null) {
                    continue;
                }
                Object name = row.get("Key");
                if (name != null && !name.toString().isBlank()) {
                    customFields.put(name.toString(), row.get("Value"));
                }
            }
        }
        return customFields;
    }

    @CatalogRequest(
            id = "localDomainRequest_6f69c3d3-601b-4ca6-ace2-b0cd242ab26e",
            name = "Attach File to Ticket",
            description = "Uploads a file to a specified Jira ticket.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Upload Status", attributes = {@Attribute(name = "toolTip", value = "'Status indicating whether the file upload was successful.'")})
    public ExtensionResponse attachFileToTicket(
            @Field(name = "Ticket Id", type = "Identifier", attributes = {@Attribute(name = "toolTip", value = "'Unique identifier of the Jira ticket to which the file will be attached.'")}) String ticketId,
            @Field(name = "File", type = "File", attributes = {@Attribute(name = "toolTip", value = "'The file to upload to the Jira ticket.'")}) List<File> file) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Attach File to Ticket");
        boolean auditRecorded = false;
        try {
            logger.info("attachFileToTicket: ticketId={}, fileCount={}",
                    ticketId, file != null ? file.size() : 0);

            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
            List<java.io.File> ticketFiles = new ArrayList<>();
            Type fileListType = new TypeToken<ArrayList<File>>() {
            }.getType();
            file = GSON.fromJson(GSON.toJson(file), fileListType);
            for (File currentFile : file) {
                try {
                    ticketFiles.add(kristaMediaClient.toJavaFile(currentFile));
                } catch (IOException cause) {
                    logger.debug("Unable to process the file {}", currentFile.getFileName());
                }
            }
            if (validationResults.isEmpty()) {
                // Success path
                boolean uploadStatus = issueDao.attachFileToTicket(ticketFiles, ticketId);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Upload Status", uploadStatus));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraResources.TICKET_ID, ticketId,
                        "File", file,
                        JiraConstants.VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterTicketId", Map.of(JiraConstants.STATE_ID, stateId));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Attach File to Ticket", cause);
            logger.error("Authorization failed for attachFileToTicket, ticketId={}: {}", ticketId, cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Attach File to Ticket", cause);
            logger.error("Illegal caller for attachFileToTicket, ticketId={}: {}", ticketId, cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE, 1);
            throw new RuntimeException("Invalid caller for file attachment operation", cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Attach File to Ticket", cause);
            logger.error("Unexpected error attaching file to ticket {}: {}", ticketId, cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while attaching file to ticket",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Attach File to Ticket");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_7f825af5-5dbc-4803-96f1-d221e3838f7c",
            name = "Create an issue",
            description = "Creates a new Jira issue with the provided details.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Desc(name = "New Issue", type = "Entity(Issue)", required = false)
    public ExtensionResponse createAnIssue(
            @Field.Text(name = "Project Key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Key of the project where the issue should be created.'")}, options = {}) String projectKey,
            @Field.Text(name = "Summary", required = true, attributes = {@Attribute(name = "toolTip", value = "'Short summary or title for the issue.'")}, options = {}) String summary,
            @Field.Text(name = "Issue Type", required = true, attributes = {@Attribute(name = "toolTip", value = "'Type of issue to be created (e.g., Bug, Story).'")}, options = {}) String issueType,
            @Field.Text(name = "Description", required = true, attributes = {@Attribute(name = "toolTip", value = "'Description for the Issue'")}, options = {}) String description,
            @Field.Text(name = "Reporter Account Id", required = false, attributes = {@Attribute(name = "toolTip", value = "'Atlassian account ID of the reporter. If not provided, the authenticated user will be the reporter.'")}, options = {}) String reporterAccountId,
            @Field.Desc(name = "Custom Field", type = "[ { Key: Text, Value: Text } ]", required = false, description = "Custom and project-required Jira fields to set on the new issue, as a list with one entry per field. Each entry has Key = the field's exact display name as it appears on the Jira create screen (case-insensitive), and Value = the value to set. Value formats by field type: plain text for text fields; the exact option label for single- or multi-select fields (e.g. Category = R&D Task); a date in YYYY-MM-DD for date fields (e.g. Due date = 2026-09-01); an account/user identifier for people fields. Different projects and issue types require different fields, so include one entry for each field the target project/issue type needs — for example DIR/Task requires 'Category'; KE/Task and KE/Bug require 'Customer' and 'Due date'. Do not use this for the standard Summary, Description, Issue Type, Project or Reporter (those have their own inputs). If the issue type needs no extra fields, pass an empty list.") List<Map<String, Object>> customFields) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Create an issue");
        boolean auditRecorded = false;
        try {
            // Log exactly what the Custom Fields input delivered and how it mapped — so a
            // "requires X" surprise is easy to diagnose (wrong key names → empty mapping).
            logger.info("createAnIssue: project={} issueType={} rawCustomFields={} mappedKeys={}",
                    projectKey, issueType, customFields, toCustomFieldMap(customFields).keySet());

            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            // Validate reporter account ID if provided
            if (reporterAccountId != null && !reporterAccountId.isBlank() && !userDao.isUserExist(reporterAccountId)) {
                logger.warn("Invalid Reporter Account Id provided: {}", reporterAccountId);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
                String errorMsg = "The provided Reporter Account Id is invalid or does not exist: " + reporterAccountId
                        + ". Please provide a valid Atlassian account ID. You can use the 'Get Users Account Id' request to look up an account ID by email.";
                return ExtensionResponseFactory.create(new IllegalArgumentException(errorMsg), errorMsg,
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR);
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey,
                            Validator.ValidationResource.ISSUE_TYPE, issueType, Validator.ValidationResource.SUMMARY, summary));
            if (validationResults.isEmpty()) {
                // Success path. No pre-emptive custom-field validation: create-meta's
                // "required"/"allowedValues" flags don't reliably match Jira's actual create
                // enforcement (fields with defaults show required=true but aren't enforced;
                // allowedValues can be incomplete). That produced false blocks. Jira is the
                // authority — build the payload and let Jira reject with its own clear error
                // (surfaced by IssueDao.createIssue / extractJiraErrors).
                Map<String, Object> customFieldMap = toCustomFieldMap(customFields);
                logger.info("createAnIssue: project={} issueType={} customFieldKeys={}",
                        projectKey, issueType, customFieldMap.keySet());
                Issue newIssue = issueDao.createIssue(projectKey, summary, description, null, issueType, reporterAccountId, customFieldMap);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("New Issue", newIssue));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
                Map<String, Object> stateMap = new HashMap<>(Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey,
                        JiraResources.DESCRIPTION, description, JiraResources.ISSUE_TYPE, issueType, JiraResources.SUMMARY, summary));
                if (reporterAccountId != null) {
                    stateMap.put(JiraResources.REPORTER_ACCOUNT_ID, reporterAccountId);
                }
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterForCreateIssue", stateMap);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Create an issue", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Create an issue", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Create an issue", cause);
            // Log the full exception (type + stack trace), not just the message — a
            // null-message exception previously hid the real cause ("...: null").
            logger.error("Error occurred while creating issue: {}: {}",
                    cause.getClass().getName(), cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while creating issue"
                    + (cause.getMessage() != null ? ": " + cause.getMessage() : " (" + cause.getClass().getSimpleName() + ")"),
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Create an issue");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_23c5c35c-2c99-4e38-b34a-f911c589f02f",
            name = "Report a bug",
            description = "Create a bug report in Jira under the specified project to track and resolve issues efficiently.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Boolean(name = "Upload Status", required = false, attributes = {@Attribute(name = "toolTip", value = "'Status indicating whether the file upload was successful.'")}, options = {})

    @Field.Desc(name = "New Bug", type = "Entity(Bug)", required = false)
    public ExtensionResponse reportABug(
            @Field.Text(name = "Project key", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the project key where you want to report the bug (e.g., PROJ, DEV, TEST).'")}, options = {}) String projectKey,
            @Field.Text(name = "Summary", required = true, attributes = {@Attribute(name = "toolTip", value = "'Provide a clear and concise summary describing the bug you encountered.'")}, options = {}) String summary) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Report a bug");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey, Validator.ValidationResource.SUMMARY, summary)
            );
            if (validationResults.isEmpty()) {
                // Success path
                Bug newBug = IssueMapper.getBug(issueDao.createIssue(projectKey, summary, summary, null, JiraConstants.BUG));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("New Bug", newBug));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForReportABug",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.PROJECT_KEY, projectKey, JiraResources.SUMMARY, summary));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Report a bug", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Report a bug", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Report a bug", cause);
            logger.error("Error occurred while reporting bug: {}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while reporting bug",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Report a bug");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_10d947e5-ccbe-457c-99f2-d50dd5ede35f",
            name = "Create User",
            description = "Create a new Jira user by sending an invitation email to join your Jira workspace.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Text(name = "Self", required = false, attributes = {@Attribute(name = "toolTip", value = "'URL reference to the created user profile in Jira.'")}, options = {})
    @Field.Text(name = "Account Id", required = false, attributes = {@Attribute(name = "toolTip", value = "'Unique account identifier assigned to the newly created user.'")}, options = {})
    @Field.Text(name = "Email Address", required = false, attributes = {@Attribute(name = "toolTip", value = "'Email address of the newly created user account.'")}, options = {})
    @Field.Text(name = "Display Name", required = false, attributes = {@Attribute(name = "toolTip", value = "'Display name that will appear for this user in Jira.'")}, options = {})
    public ExtensionResponse createUser(
            @Field.Text(name = "Email", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the email address of the person you want to invite to Jira.'")}, options = {}) String email) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Create User");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EMAIL, email)
            );
            if (validationResults.isEmpty()) {
                // Success path
                Map<String, Object> response = issueDao.createUser(email);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of(
                        "Self", response.get("Self")
                        , "Account Id", response.get("Account Id")
                        , "Email Address", response.get("Email Address")
                        , "Display Name", response.get("Display Name")));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterCreateUser",
                        Map.of(JiraConstants.STATE_ID, stateId, JiraResources.EMAIL, email));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Create User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Create User", cause);
            logger.error("Error occurred while creating user using Account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while creating user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Create User");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_5c2d6fe7-2e9e-4947-bc9a-e9f41536810a",
            name = "Update User",
            description = "Add a Jira user to specified groups to grant them appropriate permissions and access levels.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Boolean(name = "User Added", required = false, attributes = {@Attribute(name = "toolTip", value = "'Confirmation indicating whether the user was successfully added to the specified groups.'")}, options = {})
    public ExtensionResponse updateUser(
            @Field.Text(name = "Account Id", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the unique account ID of the user you want to add to groups.'")}, options = {}) String accountId,
            @Field.Desc(name = "Group Names", type = "[ Text ]", required = true) List<String> groupNames) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Update User");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
            if (validationResults.isEmpty()) {
                // Success path
                boolean userAdded = issueDao.updateUser(accountId, groupNames);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("User Added", userAdded));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForUpdateUser",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, accountId, "Group Names", groupNames));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Update User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Update User", cause);
            logger.error("Error occurred while updating user using Account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while updating user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Update User");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_8431e961-69db-4d9c-a380-2d34b89a1fda",
            name = "Delete User",
            description = "Permanently remove a Jira user from your workspace. This action requires admin privileges and cannot be undone.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM)
    @Field.Boolean(name = "Deleted", required = false, attributes = {@Attribute(name = "toolTip", value = "'Confirmation indicating whether the user was successfully deleted from Jira.'")}, options = {})
    public ExtensionResponse deleteUser(
            @Field.Text(name = "Account Id", required = true, attributes = {@Attribute(name = "toolTip", value = "'Enter the unique account ID of the user you want to permanently delete from Jira.'")}, options = {}) String accountId) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Delete User");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults =
                    validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
            if (validationResults.isEmpty()) {
                // Success path
                boolean deleted = issueDao.deleteUser(accountId);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of("Deleted", deleted));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());
            }
            String stateId = UUID.randomUUID().toString();
            internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(Map.of(JiraConstants.VALIDATION_RESULT, validationResults)));
            ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                    "confirmReenterForDeleteUser",
                    Map.of(JiraConstants.STATE_ID, stateId, JiraResources.ACCOUNT_ID, accountId));
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_SUCCESS, 1);
            return response;
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Delete User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_FAILURE, 1);
            throw cause;
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Delete User", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Delete User", cause);
            logger.error("Error occurred while deleting user using Account Id :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while deleting user using Account Id.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Delete User");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_98edc1eb-776c-45dd-9084-3015047d880e",
            name = "Get Users Account Id",
            description = "Retrieve the unique account ID of a Jira user by providing their email address.",
            area = "Organization",
            type = CatalogRequest.Type.CHANGE_SYSTEM, tool = true)
    @Field.Text(name = "Account Id", required = false, attributes = {@Attribute(name = "toolTip", value = "'The unique Jira account ID associated with the provided email address.'")}, options = {})
    public ExtensionResponse getUsersAccountId(
            @Field.Text(name = "Email", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter the email address of the user whose account ID you want to retrieve.'")}, options = {}) String email) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get Users Account Id");
        boolean auditRecorded = false;
        try {
            // Track total request count
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_COUNT, 1);

            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }

            List<ValidationOrchestrator.ValidationResult> validationResults = validationOrchestrator.validate(
                    Map.of(Validator.ValidationResource.EMAIL, FieldValidator.getNullOrEmptyDefault(email))
            );
            if (validationResults.isEmpty()) {
                // Success path
                String accountId = issueDao.getAccountIdByEmail(email);
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_SUCCESS, 1);
                return ExtensionResponseFactory.create(Map.of(JiraResources.ACCOUNT_ID, accountId));
            } else {
                // Validation failure - retry attempt
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_RETRY_ATTEMPT, 1);
                logger.debug("Validation failed, errorCount={}", validationResults.size());

                String stateId = UUID.randomUUID().toString();
                Map<String, Object> filedMap = new LinkedHashMap<>();
                filedMap.put(JiraConstants.VALIDATION_RESULT, validationResults);
                filedMap.put("Email", email);  // Include ALL original parameters
                filedMap.put(JiraConstants.RE_ENTER, false);
                internalStateManager.put(stateId, JiraConstants.GSON_BUILDER.toJson(filedMap));

                ExtensionResponse response = responseGenerator.generateConfirmationResponse(
                        ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                        "confirmReenterGetUsersAccountId",
                        Map.of(JiraConstants.STATE_ID, stateId, "Email", email, JiraConstants.RE_ENTER, false));
                telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_SUCCESS, 1);
                return response;
            }
        } catch (IllegalCallerException cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get Users Account Id", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_FAILURE, 1);
            throw new RuntimeException(cause.getMessage(), cause);
        } catch (MustAuthorizeException cause) {
            auditRecorded = true;
            jiraAuditService.authFailure(invokerId, "Get Users Account Id", cause);
            logger.error(cause.getMessage(), cause);
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_FAILURE, 1);
            throw cause;
        } catch (Exception cause) {
            auditRecorded = true;
            jiraAuditService.failure(invokerId, "Get Users Account Id", cause);
            logger.error("Error occurred while getting Users Account Id using Email :{}", cause.getMessage());
            telemetryMetrics.incrementCounter(TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_FAILURE, 1);
            return ExtensionResponseFactory.create(cause, "Error occurred while getting Users Account Id using Email.",
                    ExtensionResponse.Error.ExceptionType.SYSTEM_ERROR);
        } finally {
            if (!auditRecorded) {
                jiraAuditService.success(invokerId, "Get Users Account Id");
            }
        }
    }

    @CatalogRequest(
            id = "localDomainRequest_a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d",
            name = "Get User Account Information",
            description = "Retrieve detailed account information for a Jira user by searching with their email address or name. Returns all user fields if a match is found, or empty if no match exists.",
            area = "Organization",
            type = CatalogRequest.Type.QUERY_SYSTEM, tool = true)
    @Field.Desc(name = "User", type = "Entity(User)", required = false)
    public ExtensionResponse getUserAccountInformation(
            @Field.Text(name = "Search Query", required = false, attributes = {@Attribute(name = "toolTip", value = "'Enter the user\\'s email address or name to search for their account information.'")}, options = {}) String searchQuery) {
        String invokerId = requestContext.getInvokerId();
        jiraAuditService.triggered(invokerId, "Get User Account Information");
        try {
            if (requestContext.invokeAsUser()) {
                issueDao.validateCredentials();
            }
            ExtensionResponse result = userService.getUserAccountInformation(searchQuery);
            jiraAuditService.success(invokerId, "Get User Account Information");
            return result;
        } catch (MustAuthorizeException cause) {
            jiraAuditService.authFailure(invokerId, "Get User Account Information", cause);
            throw cause;
        } catch (Exception cause) {
            jiraAuditService.failure(invokerId, "Get User Account Information", cause);
            throw cause instanceof RuntimeException ? (RuntimeException) cause : new RuntimeException(cause);
        }
    }

}
