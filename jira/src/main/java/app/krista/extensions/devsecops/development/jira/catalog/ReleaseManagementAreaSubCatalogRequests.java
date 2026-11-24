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
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;

public class ReleaseManagementAreaSubCatalogRequests {

    private final ReleaseManagementAreaSubCatalogRequestHelper helper;
    private final ExtensionResponseGenerator responseGenerator;
    private final ErrorHandlingStateManager internalStateManager;
    private final Logger logger = LoggerFactory.getLogger(ReleaseManagementAreaSubCatalogRequests.class);

    @Inject
    public ReleaseManagementAreaSubCatalogRequests(ReleaseManagementAreaSubCatalogRequestHelper helper,
                                                   ExtensionResponseGenerator responseGenerator,
                                                   ErrorHandlingStateManager internalStateManager) {
        this.helper = helper;
        this.responseGenerator = responseGenerator;
        this.internalStateManager = internalStateManager;
    }

    @SubCatalogRequest(
            name = "confirmReenterForGetReleaseSummary",
            description = "Checks if user wants to re enter invalid details and if yes, sends prompt to do so",
            type = CatalogRequest.Type.QUERY_SYSTEM
    )
    @SuppressWarnings("unchecked")
    public ExtensionResponse confirmReenterForGetReleaseSummary(@Field.Desc(name = "inputMap",
            type = "{ re-enter: Boolean, stateId: Text, Status: Text, Assignee: Text, Priority: Text, Issue type: Text, Page Size: Number, Page Number: Number, Fix version: Text }",
            required = true) Map<String, Object> map) {
        Boolean reenter = (Boolean) map.get(JiraConstants.RE_ENTER);
        String stateId = (String) map.get("stateId");
        Map<String, Object> state = internalStateManager.get(stateId);
        List<ValidationOrchestrator.ValidationResult> validationResults = ReleaseManagementAreaSubCatalogRequestHelper.getValidationResults(state);
        if (!reenter) {
            return responseGenerator.generateFetchDenyResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                    validationResults, null,
                    Map.of());
        }
        return responseGenerator.generateFetchResponse(ExtensionResponse.Error.ExceptionType.INPUT_ERROR,
                validationResults, "handleReenteredForGetReleaseSummary",
                Map.of(JiraConstants.STATE_ID, stateId,
                        "Status", map.get("Status"),
                        "Assignee", map.get("Assignee"),
                        "Priority", map.get("Priority"),
                        "Issue type", map.get("Issue type"),
                        "Page Size", map.get("Page Size"),
                        "Page Number", map.get("Page Number"),
                        "Fix version", map.get("Fix version")));
    }

}
