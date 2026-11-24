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
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.sdk.atlassian.extresp.RemediationActionFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

@Service
public class ReleaseManagementAreaSubCatalogRequestHelper {

    private final ReleaseDao releaseDao;
    private final UserDao userDao;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final Logger logger = LoggerFactory.getLogger(ReleaseManagementAreaSubCatalogRequestHelper.class);

    @Inject
    public ReleaseManagementAreaSubCatalogRequestHelper(ReleaseDao releaseDao, UserDao userDao,
                                                        ExtensionResponseGenerator responseGenerator,
                                                        ErrorHandlingStateManager internalStateManager) {
        this.releaseDao = releaseDao;
        this.userDao = userDao;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
    }

    @SubCatalogRequest(
            name = "handleReenteredForGetReleaseSummary",
            description = "Handles re-entered data for Get Release Summary request",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @Field(name = "Total user stories", type = "Number", required = false)
    @Field(name = "Open user stories", type = "Number", required = false)
    @Field(name = "Total epics", type = "Number", required = false)
    @Field(name = "Total Tasks", type = "Number", required = false)
    @Field(name = "Open Tasks", type = "Number", required = false)
    @Field(name = "Total Bugs", type = "Number", required = false)
    @Field(name = "Open Bugs", type = "Number", required = false)
    @Field.Date(name = "Release day", required = false)
    @Field(name = "Days remaining", type = "Number", required = false)
    @Field.Desc(name = "User stories", type = "[ Entity(Issue) ]", required = false)
    @Field.Desc(name = "Epics", type = "[ Entity(Issue) ]", required = false)
    @Field.Desc(name = "Bugs", type = "[ Entity(Issue) ]", required = false)
    @Field.Desc(name = "Tasks", type = "[ Entity(Issue) ]", required = false)
    @SuppressWarnings("unchecked")
    public ExtensionResponse handleReenteredForGetReleaseSummary(@Field.Desc(name = "inputMap",
            type = "{ stateId: Text, Status: Text, Assignee: Text, Priority: Text, Issue type: Text, Page Size: Number, Page Number: Number, Fix version: Text }",
            required = true) Map<String, Object> map) {
        try {
            String status = (String) map.get("Status");
            String assignee = (String) map.get("Assignee");
            String priority = (String) map.get("Priority");
            String issueType = (String) map.get("Issue type");
            Double pageSize = (Double) map.get("Page Size");
            Double pageNumber = (Double) map.get("Page Number");
            String fixVersion = (String) map.get("Fix version");

            // Validate assignee if provided
            ExtensionResponse assigneeErrorResponse = assigneeValidator(assignee);
            if (assigneeErrorResponse != null) return assigneeErrorResponse;

            // Validate page size if provided
            ExtensionResponse pageSizeErrorResponse = pageSizeValidator(pageSize);
            if (pageSizeErrorResponse != null) return pageSizeErrorResponse;

            // Validate page number if provided
            ExtensionResponse pageNumberErrorResponse = pageNumberValidator(pageNumber);
            if (pageNumberErrorResponse != null) return pageNumberErrorResponse;

            // Process the request
            long pSize = (pageSize != null && pageSize > 0) ? pageSize.longValue() : 20;
            long pNumber = (pageNumber != null && pageNumber > 0) ? pageNumber.longValue() : 1;
            ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, pSize, pNumber, fixVersion);

            return ExtensionResponseFactory.create(response);
        } catch (Exception cause) {
            logger.error("Failed to get release summary: {}", cause.getMessage());
            return ExtensionResponseFactory.create(cause, "Failed to get release summary: " + cause.getMessage(),
                    ExtensionResponse.Error.ExceptionType.LOGIC_ERROR);
        }
    }

    /**
     * Validates if the provided assignee exists in the system
     */
    public ExtensionResponse assigneeValidator(String assignee) {
        if (assignee != null && !assignee.isEmpty() && !userDao.isUserExist(assignee)) {
            return ExtensionResponseFactory.create("Provided assignee does not exist: " + assignee,
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Provided assignee does not exist: " + assignee, List.of())),
                    null, Map.of());
        }
        return null;
    }

    /**
     * Validates page size is within acceptable range (1-100)
     */
    public ExtensionResponse pageSizeValidator(Double pageSize) {
        if (pageSize != null && (pageSize <= 0 || pageSize > 100)) {
            return ExtensionResponseFactory.create("Page size must be between 1 and 100: " + pageSize,
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Page size must be between 1 and 100: " + pageSize, List.of())),
                    null, Map.of());
        }
        return null;
    }

    /**
     * Validates page number is greater than 0
     */
    public ExtensionResponse pageNumberValidator(Double pageNumber) {
        if (pageNumber != null && pageNumber <= 0) {
            return ExtensionResponseFactory.create("Page number must be greater than 0: " + pageNumber,
                    ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    List.of(RemediationActionFactory.createInformActionALLParticipants("Page number must be greater than 0: " + pageNumber, List.of())),
                    null, Map.of());
        }
        return null;
    }

    /**
     * Utility method to extract validation results from state
     */
    @SuppressWarnings("unchecked")
    public static List<ValidationOrchestrator.ValidationResult> getValidationResults(Map<String, Object> state) {
        return (List<ValidationOrchestrator.ValidationResult>) state.get(JiraConstants.VALIDATION_RESULT);
    }
}
