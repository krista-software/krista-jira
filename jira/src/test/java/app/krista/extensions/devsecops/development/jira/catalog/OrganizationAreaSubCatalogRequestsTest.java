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
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@DisplayName("OrganizationAreaSubCatalogRequests Tests")
class OrganizationAreaSubCatalogRequestsTest {

    @Mock
    private IssueDao issueDao;
    @Mock
    private ProjectDao projectDao;
    @Mock
    private UserDao userDao;
    @Mock
    private ExtensionResponseGenerator responseGenerator;
    @Mock
    private ErrorHandlingStateManager internalStateManager;
    @Mock
    private KristaMediaClient kristaMediaClient;

    private OrganizationAreaSubCatalogRequests subject;

    private static final ValidationOrchestrator.ValidationResult SAMPLE_VALIDATION_RESULT =
            new ValidationOrchestrator.ValidationResult(
                    "Please re-enter.", "Ticket Id", "Enter a valid ticket Id.", "Invalid ticket Id.");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subject = new OrganizationAreaSubCatalogRequests(
                issueDao, projectDao, userDao, responseGenerator, internalStateManager, kristaMediaClient);
    }

    // -------------------------------------------------------------------------
    // confirmReenterTicketId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterTicketId returns deny response when re-enter is false")
    void testConfirmReenterTicketId_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId);

        ExtensionResponse response = subject.confirmReenterTicketId(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterTicketId returns fetch response when re-enter is true")
    void testConfirmReenterTicketId_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId);

        ExtensionResponse response = subject.confirmReenterTicketId(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenteredTicketId"), any());
    }

    // -------------------------------------------------------------------------
    // confirmReenterForCreateIssue
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterForCreateIssue returns deny response when re-enter is false")
    void testConfirmReenterForCreateIssue_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(JiraConstants.RE_ENTER, false);
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Summary", "A summary");
        inputMap.put("Description", "A description");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = subject.confirmReenterForCreateIssue(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterForCreateIssue returns fetch response when re-enter is true")
    void testConfirmReenterForCreateIssue_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put(JiraConstants.RE_ENTER, true);
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Summary", "A summary");
        inputMap.put("Description", "A description");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = subject.confirmReenterForCreateIssue(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenteredForCreateIssue"), any());
    }

    // -------------------------------------------------------------------------
    // confirmReenterForReportABug
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterForReportABug returns deny response when re-enter is false")
    void testConfirmReenterForReportABug_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Project Key", "PROJ",
                "Summary", "A bug summary");

        ExtensionResponse response = subject.confirmReenterForReportABug(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterForReportABug returns fetch response when re-enter is true")
    void testConfirmReenterForReportABug_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Project Key", "PROJ",
                "Summary", "A bug summary");

        ExtensionResponse response = subject.confirmReenterForReportABug(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenteredForReportABug"), any());
    }

    // -------------------------------------------------------------------------
    // confirmReenterForUpdateUser
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterForUpdateUser returns deny response when re-enter is false")
    void testConfirmReenterForUpdateUser_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Account Id", "acc-123",
                "Group Names", List.of("group-a"));

        ExtensionResponse response = subject.confirmReenterForUpdateUser(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterForUpdateUser returns fetch response when re-enter is true")
    void testConfirmReenterForUpdateUser_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Account Id", "acc-123",
                "Group Names", List.of("group-a"));

        ExtensionResponse response = subject.confirmReenterForUpdateUser(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenteredForUpdateUser"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenteredForUpdateUser - user does not exist
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenteredForUpdateUser returns error response when user does not exist")
    void testHandleReenteredForUpdateUser_UserNotExist() {
        when(userDao.isUserExist("unknown-acc")).thenReturn(false);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Account Id", "unknown-acc",
                "Group Names", List.of("group-a"));

        ExtensionResponse response = subject.handleReenteredForUpdateUser(inputMap);

        assertNotNull(response);
    }

    @Test
    @DisplayName("handleReenteredForUpdateUser succeeds when user exists")
    void testHandleReenteredForUpdateUser_Success() {
        when(userDao.isUserExist("acc-123")).thenReturn(true);
        when(issueDao.updateUser(eq("acc-123"), any())).thenReturn(true);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Account Id", "acc-123");
        inputMap.put("Group Names", List.of("group-a"));

        ExtensionResponse response = subject.handleReenteredForUpdateUser(inputMap);

        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // confirmReenterForDeleteUser
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterForDeleteUser returns deny response when re-enter is false")
    void testConfirmReenterForDeleteUser_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Account Id", "acc-123");

        ExtensionResponse response = subject.confirmReenterForDeleteUser(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterForDeleteUser returns fetch response when re-enter is true")
    void testConfirmReenterForDeleteUser_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Account Id", "acc-123");

        ExtensionResponse response = subject.confirmReenterForDeleteUser(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenterForDeleteUser"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenterForDeleteUser
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenterForDeleteUser returns error response when user does not exist")
    void testHandleReenterForDeleteUser_UserNotExist() {
        when(userDao.isUserExist("unknown-acc")).thenReturn(false);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Account Id", "unknown-acc");

        ExtensionResponse response = subject.handleReenterForDeleteUser(inputMap);

        assertNotNull(response);
    }

    @Test
    @DisplayName("handleReenterForDeleteUser succeeds when user exists")
    void testHandleReenterForDeleteUser_Success() {
        when(userDao.isUserExist("acc-123")).thenReturn(true);
        when(issueDao.deleteUser("acc-123")).thenReturn(true);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Account Id", "acc-123");

        ExtensionResponse response = subject.handleReenterForDeleteUser(inputMap);

        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // getSummaryErrorResponse / getProjectKeyResponse (package-private)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getSummaryErrorResponse returns null for a short summary")
    void testGetSummaryErrorResponse_ValidSummary() {
        Map<String, Object> inputMap = Map.of("Summary", "Short summary");

        ExtensionResponse response = subject.getSummaryErrorResponse(inputMap);

        org.junit.jupiter.api.Assertions.assertNull(response);
    }

    @Test
    @DisplayName("getSummaryErrorResponse returns error for summary > 255 chars")
    void testGetSummaryErrorResponse_TooLong() {
        String longSummary = "A".repeat(256);
        Map<String, Object> inputMap = Map.of("Summary", longSummary);

        ExtensionResponse response = subject.getSummaryErrorResponse(inputMap);

        assertNotNull(response);
    }

    @Test
    @DisplayName("getProjectKeyResponse returns null when project exists")
    void testGetProjectKeyResponse_ValidProject() {
        when(projectDao.isProjectExist("PROJ")).thenReturn(true);
        Map<String, Object> inputMap = Map.of("Project Key", "PROJ");

        ExtensionResponse response = subject.getProjectKeyResponse(inputMap);

        org.junit.jupiter.api.Assertions.assertNull(response);
    }

    @Test
    @DisplayName("getProjectKeyResponse returns error when project does not exist")
    void testGetProjectKeyResponse_InvalidProject() {
        when(projectDao.isProjectExist("BADKEY")).thenReturn(false);
        Map<String, Object> inputMap = Map.of("Project Key", "BADKEY");

        ExtensionResponse response = subject.getProjectKeyResponse(inputMap);

        assertNotNull(response);
    }
}
