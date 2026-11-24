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
import app.krista.extension.impl.anno.CatalogRequest;
import app.krista.extension.impl.anno.Field;
import app.krista.extension.impl.anno.SubCatalogRequest;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketPriority;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.sdk.atlassian.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.*;
import app.krista.sdk.atlassian.jql.JQLBuilder;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static app.krista.extensions.devsecops.development.jira.catalog.RitualsArea.LIST_OF_ISSUES;

@Service
public class RitualsAreaSubCatalogRequests {

    private final IssueDao issueDao;
    private final RequestContext requestContext;
    private final UserDao userDao;
    private final AuthorizationContext authorizationContext;
    private final EventHandler eventHandler;
    private final ErrorHandlingStateManager internalStateManager;
    private final ExtensionResponseGenerator responseGenerator;
    private final ValidationOrchestrator validationOrchestrator;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final RitualsArea ritualsArea;
    private final TicketInfoDao ticketInfoDao;
    private final ProjectDao projectDao;
    private final Logger logger = LoggerFactory.getLogger(RitualsAreaSubCatalogRequests.class);


    @Inject
    public RitualsAreaSubCatalogRequests(IssueDao issueDao, RequestContext requestContext, UserDao userDao, AuthorizationContext authorizationContext, EventHandler eventHandler, ErrorHandlingStateManager internalStateManager, ExtensionResponseGenerator responseGenerator, ValidationOrchestrator validationOrchestrator, RitualsArea ritualsArea, TicketInfoDao ticketInfoDao, ProjectDao projectDao) {
        this.issueDao = issueDao;
        this.requestContext = requestContext;
        this.userDao = userDao;
        this.authorizationContext = authorizationContext;
        this.eventHandler = eventHandler;
        this.internalStateManager = internalStateManager;
        this.responseGenerator = responseGenerator;
        this.validationOrchestrator = validationOrchestrator;
        this.ritualsArea = ritualsArea;
        this.ticketInfoDao = ticketInfoDao;
        this.projectDao = projectDao;
    }

    @SubCatalogRequest(
            name = "confirmReenterForAddAComment",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForAddAComment(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Ticket Id: Text, Comment: Paragraph, Comment Visibility : Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterForAddAComment",
                Map.of("stateId",
                        stateId, JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID),
                        "Comment", map.get("Comment"),
                        JiraConstants.COMMENT_VISIBILITY, map.get(JiraConstants.COMMENT_VISIBILITY)));
    }

    @SubCatalogRequest(
            name = "handleReenterForAddAComment",
            description = "Use the new details to add the comment to the ticket",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Is successful", type = "Switch")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterForAddAComment(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text, Comment: Paragraph, Comment Visibility : Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            issueDao.addComment((String) map.get(JiraResources.TICKET_ID), (String) map.get("Comment"),
                    (String) map.get(JiraConstants.COMMENT_VISIBILITY));
            return ExtensionResponseFactory.create(Map.of("Is successful", true));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to add comment",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterForGetUserIssues",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForGetUserIssues(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Account Id: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterForGetUserIssues",
                Map.of("stateId",
                        stateId, JiraResources.ACCOUNT_ID, map.get(JiraResources.ACCOUNT_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterForGetUserIssues",
            description = "Use the new details to add the comment to the ticket",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "User Issues", type = "[ Entity(Issue) ]")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterForGetUserIssues(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Account Id: Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse accountIdErrorResponse = accountIdErrorResponse(map);
            if (accountIdErrorResponse != null) return accountIdErrorResponse;
            return ExtensionResponseFactory.create(Map.of("User Issues",
                    issueDao.searchByJQL(" assignee in (" + map.get(JiraResources.ACCOUNT_ID) + ") order by priority DESC, duedate DESC", 30, 1)));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to get issues by user account id",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterForUpdateStatusOfIssue",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForUpdateStatusOfIssue(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Ticket Id: Text, Status Name: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterForUpdateStatusOfIssue",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID),
                        JiraResources.STATUS_NAME, map.get(JiraResources.STATUS_NAME)));
    }

    @SubCatalogRequest(
            name = "handleReenterForUpdateStatusOfIssue",
            description = "Use the new details to add the comment to the ticket",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterForUpdateStatusOfIssue(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text, Status Name: Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            issueDao.updateStatus((String) map.get(JiraResources.TICKET_ID),
                    (String) map.get(JiraResources.STATUS_NAME));
            return ExtensionResponseFactory.create(Map.of(
                    "Updated Issue", issueDao.getIssueFinal((String) map.get(JiraResources.TICKET_ID))));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to update status of issue " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterAssignATaskToUser",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterAssignATaskToUser(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Ticket Id: Text , Account Id: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterAssignATaskToUser",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID), JiraResources.ACCOUNT_ID, map.get(JiraResources.ACCOUNT_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterAssignATaskToUser",
            description = "Assign a task to user",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Updated issue details", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterAssignATaskToUser(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text,  Account Id: Text }",
            required = true) Map<String, Object> map) {
        try {
            String issueKey = (String) map.get(JiraResources.TICKET_ID);
            String toAssignUserAccountId = (String) map.get(JiraResources.ACCOUNT_ID);
            ExtensionResponse issueKeyAccountIDerrorResponse = issueKeyAccountIDerrorResponse(map);
            if (issueKeyAccountIDerrorResponse != null) return issueKeyAccountIDerrorResponse;
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            ExtensionResponse accountIdErrorResponse = accountIdErrorResponse(map);
            if (accountIdErrorResponse != null) return accountIdErrorResponse;

            issueDao.assignTask(issueKey, toAssignUserAccountId);
            return ExtensionResponseFactory.create(Map.of(
                    "Updated issue details", issueDao.getIssueFinal(issueKey)));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to assign task to user",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterGetIssueByKey",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetIssueByKey(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Ticket Id: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterGetIssueByKey",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterGetIssueByKey",
            description = "Assign a task to user",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Issue", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterGetIssueByKey(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text, Status Name: Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            return ExtensionResponseFactory.create(Map.of(
                    "Issue", issueDao.getIssueFinal((String) map.get(JiraResources.TICKET_ID))));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to get issue by key",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterUpdatePriorityOfIssue",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterUpdatePriorityOfIssue(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Ticket Id: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterUpdatePriorityOfIssue",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID)));
    }

    @SubCatalogRequest(
            name = "handleReenterUpdatePriorityOfIssue",
            description = "Update priority of the issue",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "Updated Issue", type = "Entity(Issue)")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterUpdatePriorityOfIssue(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text, Status Name: Text }",
            required = true) Map<String, Object> map) {
        try {
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            String stateId = (String) map.get("stateId");
            Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
            String priorityName = (String) state.get("Priority Name");
            TicketPriority priority = new TicketPriority();
            priority.name = priorityName;
            return ExtensionResponseFactory.create(Map.of(
                    "Updated Issue", issueDao.updatePriority(priority, (String) map.get(JiraResources.TICKET_ID))));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to update priority of issue",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterGetIssues",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetIssues(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Project Key: Text, Issue Type: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(
                ExtensionResponse.Error.ExceptionType.INPUT_ERROR, validationResults,
                "handleReenterGetIssues",
                Map.of("stateId", stateId, "Project Key", map.get("Project Key"),
                        "Issue Type", map.get("Issue Type")));
    }

    @SubCatalogRequest(
            name = "handleReenterGetIssues",
            description = "Get issues by given filters",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field.Desc(name = "List of Filtered Issues", type = "[ Entity(Issue) ]")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterGetIssues(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Project Key: Text, Issue Type: Text }",
            required = true) Map<String, Object> map) {
        try {
            String issueType = (String) map.get("Issue Type");
            String projectKey = (String) map.get("Project Key");
            String stateId = (String) map.get("stateId");
            Map<String, Object> state = internalStateManager.get(stateId);
            String status = (String) state.get("Status");
            String assignee = (String) state.get("Assignee");
            String priority = (String) state.get("Priority");
            String summary = (String) state.get("Summary");
            Double pageSize = (Double) state.get("Page Size");
            Double pageNumber = (Double) state.get("Page Number");
            String jql = new JQLBuilder().status(status)
                    .assignee(assignee)
                    .priority(priority)
                    .summary(summary)
                    .projectKey(projectKey)
                    .issueType(issueType)
                    .orderBy("priority DESC, updated", "desc")
                    .build();
            List<Issue> issues = issueDao.searchByJQL(jql, (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20, (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1);
            return ExtensionResponseFactory.create(Map.of("List of Filtered Issues", issues));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to get issues by given filters",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @SubCatalogRequest(
            name = "confirmReenterAssignIssueToEpic",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterAssignIssueToEpic(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Ticket Id: Text , Epic Key: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = (Map<String, Object>) internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterAssignIssueToEpic",
                Map.of("stateId", stateId,
                        JiraResources.TICKET_ID, map.get(JiraResources.TICKET_ID), JiraResources.EPIC_KEY, map.get(JiraResources.EPIC_KEY)));
    }

    @SubCatalogRequest(
            name = "handleReenterAssignIssueToEpic",
            description = "Assign Issue to Epic",
            type = CatalogRequest.Type.CHANGE_SYSTEM
    )
    @Field(name = "Is successful", type = "Switch", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterAssignIssueToEpic(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Ticket Id: Text, Epic Key: Text }",
            required = true) Map<String, Object> map) {
        try {
            String issueKey = (String) map.get(JiraResources.TICKET_ID);
            String epicKey = (String) map.get(JiraResources.EPIC_KEY);
            if (!issueDao.isIssueExists(issueKey) && !issueDao.isIssueExists(epicKey)) {
                return ExtensionResponseFactory.create("Provided issue key and epic key is not valid : " + "issue Key - " + issueKey + " And epic key -" + epicKey, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        List.of(RemediationActionFactory.createInformActionALLParticipants("Provided issue key and epic key is not valid : " + "issue Key - " + issueKey + " epic key -" + epicKey, List.of())),
                        null, Map.of());
            }
            ExtensionResponse ticketIdResponse = getTicketIdResponse(map);
            if (ticketIdResponse != null) return ticketIdResponse;
            ExtensionResponse epicKeyErrorResponse = getEpicKeyResponse(map);
            if (epicKeyErrorResponse != null) return epicKeyErrorResponse;
            issueDao.assignIssueToEpic(issueKey, epicKey);
            return ExtensionResponseFactory.create(Map.of("Is successful", true));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to update priority of issue",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }


    @NotNull
    private static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        List<?> results = (List<?>) state.get(JiraConstants.VALIDATION_RESULT);
        for (Object item : results) {
            validationResults.add(JiraConstants.GSON.fromJson(JiraConstants.GSON.toJson(item), ValidationOrchestrator.ValidationResult.class));
        }
        return validationResults;
    }


    private ExtensionResponse validateTicketId(String ticketId) {
        if (!issueDao.isIssueExists(ticketId)) {
            return ExtensionResponseFactory.create("Provided ticket id is not valid : " + ticketId, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided ticket id is not valid : " + ticketId, List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse issueKeyAccountIDerrorResponse(Map<String, Object> map) {
        ExtensionResponse result = null;
        if (!issueDao.isIssueExists(map.get(JiraResources.TICKET_ID).toString()) && !userDao.isUserExist(map.get(JiraResources.ACCOUNT_ID).toString())) {
            result = ExtensionResponseFactory.create("Provided ticket id is not valid :" + map.get(JiraResources.TICKET_ID).toString() + " And user is not exist for provided Account Id :" + map.get(JiraResources.ACCOUNT_ID).toString(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided ticket id is not valid :" + map.get(JiraResources.TICKET_ID), List.of())),
                    null, Map.of());
        }
        return result;
    }

    private ExtensionResponse accountIdErrorResponse(Map<String, Object> map) {
        if (!userDao.isUserExist(map.get(JiraResources.ACCOUNT_ID).toString())) {
            return ExtensionResponseFactory.create("User is not exist for provided Account Id: " + map.get(JiraResources.ACCOUNT_ID).toString(), ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("User is not exist for provided Account Id:" + map.get(JiraResources.ACCOUNT_ID).toString(), List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse projectKeyErrorResponse(String projectKey) {
        if (!projectDao.isProjectExist(projectKey)) {
            return ExtensionResponseFactory.create("Provided project key is not valid :" + projectKey, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided project key is not valid :" + projectKey, List.of())),
                    null, Map.of());
        }
        return null;
    }

    private ExtensionResponse issueTypeValidator(String projectKey, String issueType) {
        if (!issueDao.validateIssueType(projectKey, issueType)) {
            return ExtensionResponseFactory.create("Provided issue type : " + issueType + "is not valid for provided project jey : " + projectKey, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided issue type : " + issueType + "is not valid for provided project jey : " + projectKey, List.of())),
                    null, Map.of());
        }
        return null;
    }


    private ExtensionResponse getTicketIdResponse(Map<String, Object> map) {
        String ticketId = (String) map.get(JiraResources.TICKET_ID);
        return validateTicketId(ticketId);
    }

    private ExtensionResponse getEpicKeyResponse(Map<String, Object> map) {
        String ticketId = (String) map.get(JiraResources.EPIC_KEY);
        return validateTicketId(ticketId);
    }

    private ExtensionResponse projectKeyIssueTypeValidator(Map<String, Object> map, String projectKey, String issueType) {
        if (!projectDao.isProjectExist(projectKey) && !issueDao.validateIssueType(projectKey, issueType)) {
            return ExtensionResponseFactory.create("Provided project key and Issue type  is not valid : " + "Project Key : " + projectKey + " And Issue Type : " + issueType, ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided project key is not valid :" + map.get(JiraResources.PROJECT_KEY).toString(), List.of())),
                    null, Map.of());
        }
        return null;
    }

    @SubCatalogRequest(
            name = "confirmReenterGetMyIssuesByGivenStatuses",
            description = "Checks if user wants to re enter invalid status names and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetMyIssuesByGivenStatuses(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Status Names: [ Text ] }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }

        // Pass complete map to generateFetchResponse (critical fix)
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterGetMyIssuesByGivenStatuses", map);
    }

    @SubCatalogRequest(
            name = "handleReenterGetMyIssuesByGivenStatuses",
            description = "Handles re-entry of status names and validates them before executing the query",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterGetMyIssuesByGivenStatuses(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Status Names: [ Text ] }",
            required = true) Map<String, Object> map) {
        try {
            List<String> statusNames = (List<String>) map.get("Status Names");

            // Enhanced re-validation using field extraction
            String statusNamesJson = JiraConstants.GSON_BUILDER.toJson(statusNames);
            Map<Validator.ValidationResource, String> validationParams = Map.of(
                Validator.ValidationResource.STATUS_NAMES, statusNamesJson
            );

            List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(validationParams, RitualsArea.class, "getMyIssuesByGivenStatuses");

            if (!validationResults.isEmpty()) {
                // Re-validation failed, show error again
                return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults, "handleReenterGetMyIssuesByGivenStatuses", map);
            }

            // Validation passed, execute business logic
            String statusCommaSeparated = statusNames.stream()
                    .map(status -> "'" + status + "'")
                    .collect(Collectors.joining(","));
            List<Issue> issues = issueDao.searchByJQL("status in (" + statusCommaSeparated + ") AND assignee in (currentUser()) ORDER BY priority DESC, updated DESC", 30, 1);

            return ExtensionResponseFactory.create(Map.of("Issues", issues));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to get issues by given statuses",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterUpdateStatusOfMultipleIssues",
            description = "Checks if user wants to re enter invalid status name or issue keys and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterUpdateStatusOfMultipleIssues(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Status Name: Text, Issue Key: [ Text ] }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }

        // Pass complete map to generateFetchResponse (critical fix)
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterUpdateStatusOfMultipleIssues", map);
    }

    @SubCatalogRequest(
            name = "handleReenterUpdateStatusOfMultipleIssues",
            description = "Handles re-entry of status name and issue keys and validates them before executing the update",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @Field.Desc(name = "Updated issues", type = "[ Entity(Issue) ]")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterUpdateStatusOfMultipleIssues(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Status Name: Text, Issue Key: [ Text ] }",
            required = true) Map<String, Object> map) {
        try {
            String statusName = (String) map.get("Status Name");
            List<String> issueKey = (List<String>) map.get("Issue Key");

            // Enhanced re-validation using field extraction (same as main method)
            String issueKeysJson = JiraConstants.GSON_BUILDER.toJson(issueKey);
            Map<Validator.ValidationResource, String> validationParams = Map.of(
                Validator.ValidationResource.ISSUE_KEYS, issueKeysJson,
                Validator.ValidationResource.STATUS_FOR_MULTIPLE_ISSUES, statusName
            );

            List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(validationParams, RitualsArea.class, "updateStatusOfMultipleIssues");

            if (!validationResults.isEmpty()) {
                // Re-validation failed, show error again
                return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults, "handleReenterUpdateStatusOfMultipleIssues", map);
            }

            // Validation passed, execute business logic (same as main method)
            List<Issue> updatedIssue = new ArrayList<>();
            issueKey.forEach(key -> {
                issueDao.updateStatus(key, statusName);
                updatedIssue.add(issueDao.getIssueFinal(key));
            });

            return ExtensionResponseFactory.create(Map.of("Updated issues", updatedIssue));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to update status of multiple issues",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    @SubCatalogRequest(
            name = "confirmReenterGetUserIssueByIssueType",
            description = "Checks if user wants to re enter invalid user account ID or issue type and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterGetUserIssueByIssueType(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, User account Id: Text, Issue Type: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }

        // Pass complete map to generateFetchResponse (critical fix)
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenterGetUserIssueByIssueType", map);
    }

    @SubCatalogRequest(
            name = "handleReenterGetUserIssueByIssueType",
            description = "Handles re-entry of user account ID and issue type and validates them before executing the query",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @Field.Desc(name = "Issues", type = "[ Entity(Issue) ]")
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenterGetUserIssueByIssueType(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, User account Id: Text, Issue Type: Text }",
            required = true) Map<String, Object> map) {
        try {
            String userAccountId = (String) map.get("User account Id");
            String issueType = (String) map.get("Issue Type");

            // Enhanced re-validation using field extraction (same as main method)
            Map<Validator.ValidationResource, String> validationParams = Map.of(
                Validator.ValidationResource.USER_ID, userAccountId,
                Validator.ValidationResource.GLOBAL_ISSUE_TYPE, issueType
            );

            List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(validationParams, RitualsArea.class, "getUserIssueByIssueType");

            if (!validationResults.isEmpty()) {
                // Re-validation failed, show error again
                return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                        validationResults, "handleReenterGetUserIssueByIssueType", map);
            }

            // Validation passed, execute business logic (same as main method)
            List<Issue> issues = issueDao.searchByJQL("issuetype = \"" + issueType + "\"  AND assignee in (" + userAccountId + ") order by priority DESC, duedate DESC", 30, 1);

            return ExtensionResponseFactory.create(Map.of("Issues", issues));
        } catch (Exception cause) {
            return ExtensionResponseFactory.create(cause, "Failed to get user issues by issue type",
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }
}
