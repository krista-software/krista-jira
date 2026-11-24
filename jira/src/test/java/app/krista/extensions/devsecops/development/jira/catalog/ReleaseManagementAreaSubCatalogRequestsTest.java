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
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReleaseManagementAreaSubCatalogRequestsTest {

    private ReleaseManagementAreaSubCatalogRequestHelper mockedHelper;
    private ExtensionResponseGenerator responseGenerator;
    private ErrorHandlingStateManager mockedInternalStateManager;
    private ReleaseManagementAreaSubCatalogRequests subcatalogRequests;

    @Before
    public void setUp() {
        mockedHelper = Mockito.mock(ReleaseManagementAreaSubCatalogRequestHelper.class);
        // Use real ExtensionResponseGenerator (same pattern as OrganizationAreaTest)
        responseGenerator = new ExtensionResponseGenerator();
        mockedInternalStateManager = Mockito.mock(ErrorHandlingStateManager.class);
        subcatalogRequests = new ReleaseManagementAreaSubCatalogRequests(
                mockedHelper, responseGenerator, mockedInternalStateManager);
    }

    /**
     * Helper to create a ValidationResult that can be safely iterated by ExtensionResponseGenerator.
     */
    private ValidationOrchestrator.ValidationResult makeValidationResult(String field, String errMsg) {
        return new ValidationOrchestrator.ValidationResult(
                "Please confirm: " + field,
                field,
                "Please re-enter " + field,
                errMsg
        );
    }

    /**
     * Build a state map that getValidationResults(state) can read.
     * JiraConstants.VALIDATION_RESULT == "Validation Results"
     */
    private Map<String, Object> buildState(List<ValidationOrchestrator.ValidationResult> results) {
        Map<String, Object> state = new HashMap<>();
        state.put(JiraConstants.VALIDATION_RESULT, results);
        return state;
    }

    @Test
    public void testConfirmReenterForGetReleaseSummary_ReenterFalse_ReturnsDenyResponse() {
        // Arrange
        String stateId = "test-state-id-deny";
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(
                makeValidationResult("Fix version", "Invalid fix version"));

        Map<String, Object> state = buildState(validationResults);
        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(JiraConstants.RE_ENTER, false);
        inputMap.put("stateId", stateId);
        // remaining fields are not accessed when reenter=false
        inputMap.put("Status", "Open");
        inputMap.put("Assignee", "user1");
        inputMap.put("Priority", "High");
        inputMap.put("Issue type", "Bug");
        inputMap.put("Page Size", 10.0);
        inputMap.put("Page Number", 1.0);
        inputMap.put("Fix version", "1.0");

        // Act
        ExtensionResponse result = subcatalogRequests.confirmReenterForGetReleaseSummary(inputMap);

        // Assert
        Assert.assertNotNull(result);
        // generateFetchDenyResponse produces an error response
        Assert.assertNotNull(result.getError());
        Mockito.verify(mockedInternalStateManager).get(stateId);
    }

    @Test
    public void testConfirmReenterForGetReleaseSummary_ReenterTrue_ReturnsFetchResponse() {
        // Arrange
        String stateId = "test-state-id-fetch";
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of(
                makeValidationResult("Fix version", "Invalid fix version"));

        Map<String, Object> state = buildState(validationResults);
        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(JiraConstants.RE_ENTER, true);
        inputMap.put("stateId", stateId);
        // All map fields are accessed via map.get() to build the state map for generateFetchResponse.
        // Map.of() disallows null values, so all fields must be provided with non-null values.
        inputMap.put("Status", "In Progress");
        inputMap.put("Assignee", "user2");
        inputMap.put("Priority", "Medium");
        inputMap.put("Issue type", "Story");
        inputMap.put("Page Size", 25.0);
        inputMap.put("Page Number", 2.0);
        inputMap.put("Fix version", "2.0");

        // Act
        ExtensionResponse result = subcatalogRequests.confirmReenterForGetReleaseSummary(inputMap);

        // Assert
        Assert.assertNotNull(result);
        // generateFetchResponse produces an error response with an ask action
        Assert.assertNotNull(result.getError());
        Mockito.verify(mockedInternalStateManager).get(stateId);
    }

    @Test
    public void testConfirmReenterForGetReleaseSummary_ReenterFalse_EmptyValidationResults() {
        // Edge case: empty validation results list - the deny response still gets generated
        String stateId = "test-state-empty";
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of();

        Map<String, Object> state = buildState(validationResults);
        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(JiraConstants.RE_ENTER, false);
        inputMap.put("stateId", stateId);
        inputMap.put("Status", "Done");
        inputMap.put("Assignee", "user3");
        inputMap.put("Priority", "Low");
        inputMap.put("Issue type", "Task");
        inputMap.put("Page Size", 5.0);
        inputMap.put("Page Number", 1.0);
        inputMap.put("Fix version", "3.0");

        ExtensionResponse result = subcatalogRequests.confirmReenterForGetReleaseSummary(inputMap);

        Assert.assertNotNull(result);
        Mockito.verify(mockedInternalStateManager).get(stateId);
    }

    @Test
    public void testConfirmReenterForGetReleaseSummary_ReenterTrue_EmptyValidationResults() {
        // Edge case: empty validation results list with reenter=true
        String stateId = "test-state-empty-fetch";
        List<ValidationOrchestrator.ValidationResult> validationResults = List.of();

        Map<String, Object> state = buildState(validationResults);
        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(JiraConstants.RE_ENTER, true);
        inputMap.put("stateId", stateId);
        inputMap.put("Status", "Done");
        inputMap.put("Assignee", "user4");
        inputMap.put("Priority", "Low");
        inputMap.put("Issue type", "Task");
        inputMap.put("Page Size", 5.0);
        inputMap.put("Page Number", 1.0);
        inputMap.put("Fix version", "3.0");

        ExtensionResponse result = subcatalogRequests.confirmReenterForGetReleaseSummary(inputMap);

        Assert.assertNotNull(result);
        Mockito.verify(mockedInternalStateManager).get(stateId);
    }
}
