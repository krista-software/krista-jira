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
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@DisplayName("RitualsAreaSubCatalogRequestsHelper Tests")
class RitualsAreaSubCatalogRequestsHelperTest {

    @Mock
    private IssueDao issueDao;
    @Mock
    private RequestContext requestContext;
    @Mock
    private UserDao userDao;
    @Mock
    private AuthorizationContext authorizationContext;
    @Mock
    private EventHandler eventHandler;
    @Mock
    private ErrorHandlingStateManager internalStateManager;
    @Mock
    private ExtensionResponseGenerator responseGenerator;
    @Mock
    private RitualsArea ritualsArea;
    @Mock
    private TicketInfoDao ticketInfoDao;
    @Mock
    private ProjectDao projectDao;

    private RitualsAreaSubCatalogRequestsHelper subject;

    private static final ValidationOrchestrator.ValidationResult SAMPLE_VALIDATION_RESULT =
            new ValidationOrchestrator.ValidationResult(
                    "Please re-enter.", "Ticket Id", "Enter a valid ticket Id.", "Invalid ticket Id.");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subject = new RitualsAreaSubCatalogRequestsHelper(
                issueDao, requestContext, userDao, authorizationContext, eventHandler,
                internalStateManager, responseGenerator, ritualsArea, ticketInfoDao, projectDao);
    }

    // -------------------------------------------------------------------------
    // confirmReenterForUpdateAnIssue
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterForUpdateAnIssue returns deny response when re-enter is false")
    void testConfirmReenterForUpdateAnIssue_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Ticket Id", "PROJ-1",
                "Status Name", "Done");

        ExtensionResponse response = subject.confirmReenterForUpdateAnIssue(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterForUpdateAnIssue returns fetch response when re-enter is true")
    void testConfirmReenterForUpdateAnIssue_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Ticket Id", "PROJ-1",
                "Status Name", "Done");

        ExtensionResponse response = subject.confirmReenterForUpdateAnIssue(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenterForUpdateAnIssue"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenterForUpdateAnIssue
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenterForUpdateAnIssue returns error when ticket id is invalid")
    void testHandleReenterForUpdateAnIssue_InvalidTicketId() {
        when(issueDao.isIssueExists("INVALID-1")).thenReturn(false);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Ticket Id", "INVALID-1",
                "Status Name", "Done");

        ExtensionResponse response = subject.handleReenterForUpdateAnIssue(inputMap);

        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // confirmReenterAssignChildIssueToEpic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterAssignChildIssueToEpic returns deny response when re-enter is false")
    void testConfirmReenterAssignChildIssueToEpic_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Child Issue Key", "PROJ-10",
                "Parent Epic Key", "PROJ-1");

        ExtensionResponse response = subject.confirmReenterAssignChildIssueToEpic(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterAssignChildIssueToEpic returns fetch response when re-enter is true")
    void testConfirmReenterAssignChildIssueToEpic_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Child Issue Key", "PROJ-10",
                "Parent Epic Key", "PROJ-1");

        ExtensionResponse response = subject.confirmReenterAssignChildIssueToEpic(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenterAssignChildIssueToEpic"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenterAssignChildIssueToEpic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenterAssignChildIssueToEpic returns error when both keys are invalid")
    void testHandleReenterAssignChildIssueToEpic_BothKeysInvalid() {
        when(issueDao.isIssueExists("CHILD-1")).thenReturn(false);
        when(issueDao.isIssueExists("EPIC-1")).thenReturn(false);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Child Issue Key", "CHILD-1",
                "Parent Epic Key", "EPIC-1");

        ExtensionResponse response = subject.handleReenterAssignChildIssueToEpic(inputMap);

        assertNotNull(response);
    }

    @Test
    @DisplayName("handleReenterAssignChildIssueToEpic returns error when child key is invalid")
    void testHandleReenterAssignChildIssueToEpic_ChildKeyInvalid() {
        when(issueDao.isIssueExists("CHILD-1")).thenReturn(false);
        when(issueDao.isIssueExists("EPIC-1")).thenReturn(true);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Child Issue Key", "CHILD-1",
                "Parent Epic Key", "EPIC-1");

        ExtensionResponse response = subject.handleReenterAssignChildIssueToEpic(inputMap);

        assertNotNull(response);
    }

    @Test
    @DisplayName("handleReenterAssignChildIssueToEpic succeeds when both keys are valid")
    void testHandleReenterAssignChildIssueToEpic_Success() {
        when(issueDao.isIssueExists("CHILD-1")).thenReturn(true);
        when(issueDao.isIssueExists("EPIC-1")).thenReturn(true);
        doNothing().when(issueDao).assignChildIssueToEpic("CHILD-1", "EPIC-1");

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Child Issue Key", "CHILD-1",
                "Parent Epic Key", "EPIC-1");

        ExtensionResponse response = subject.handleReenterAssignChildIssueToEpic(inputMap);

        assertNotNull(response);
        verify(issueDao).assignChildIssueToEpic("CHILD-1", "EPIC-1");
    }

    // -------------------------------------------------------------------------
    // confirmReenterAddWorkLog
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterAddWorkLog returns deny response when re-enter is false")
    void testConfirmReenterAddWorkLog_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Ticket Id", "PROJ-5");

        ExtensionResponse response = subject.confirmReenterAddWorkLog(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterAddWorkLog returns fetch response when re-enter is true")
    void testConfirmReenterAddWorkLog_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Ticket Id", "PROJ-5");

        ExtensionResponse response = subject.confirmReenterAddWorkLog(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenterAddWorkLog"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenterAddWorkLog
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenterAddWorkLog returns error when ticket id is invalid")
    void testHandleReenterAddWorkLog_InvalidTicket() {
        when(issueDao.isIssueExists("INVALID-1")).thenReturn(false);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Ticket Id", "INVALID-1");

        ExtensionResponse response = subject.handleReenterAddWorkLog(inputMap);

        assertNotNull(response);
    }

    @Test
    @DisplayName("handleReenterAddWorkLog succeeds when ticket is valid")
    void testHandleReenterAddWorkLog_Success() {
        String stateId = UUID.randomUUID().toString();
        when(issueDao.isIssueExists("PROJ-5")).thenReturn(true);
        Map<String, Object> state = Map.of(
                "Start Date time", 1718000000.0,
                "Time Spent", "1h");
        when(internalStateManager.get(stateId)).thenReturn(state);
        doNothing().when(issueDao).addWorklog(any(), anyLong(), any(), any());

        Map<String, Object> inputMap = Map.of(
                "stateId", stateId,
                "Ticket Id", "PROJ-5");

        ExtensionResponse response = subject.handleReenterAddWorkLog(inputMap);

        assertNotNull(response);
        verify(issueDao).addWorklog(eq("1h"), anyLong(), eq("PROJ-5"), any());
    }
}
