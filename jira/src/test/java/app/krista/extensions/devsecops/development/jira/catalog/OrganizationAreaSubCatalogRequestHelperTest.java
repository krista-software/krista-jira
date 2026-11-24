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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@DisplayName("OrganizationAreaSubCatalogRequestHelper Tests")
class OrganizationAreaSubCatalogRequestHelperTest {

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

    private OrganizationAreaSubCatalogRequestHelper subject;

    private static final ValidationOrchestrator.ValidationResult SAMPLE_VALIDATION_RESULT =
            new ValidationOrchestrator.ValidationResult(
                    "Please re-enter.", "Email", "Enter a valid email.", "Invalid email.");

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subject = new OrganizationAreaSubCatalogRequestHelper(
                issueDao, projectDao, userDao, responseGenerator, internalStateManager, kristaMediaClient);
    }

    // -------------------------------------------------------------------------
    // confirmReenterGetUsersAccountId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterGetUsersAccountId returns deny response when re-enter is false")
    void testConfirmReenterGetUsersAccountId_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Email", "user@example.com");

        ExtensionResponse response = subject.confirmReenterGetUsersAccountId(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterGetUsersAccountId returns fetch response when re-enter is true")
    void testConfirmReenterGetUsersAccountId_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Email", "user@example.com");

        ExtensionResponse response = subject.confirmReenterGetUsersAccountId(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenterGetUsersAccountId"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenterGetUsersAccountId
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenterGetUsersAccountId returns error for invalid email")
    void testHandleReenterGetUsersAccountId_InvalidEmail() {
        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Email", "not-an-email");

        ExtensionResponse response = subject.handleReenterGetUsersAccountId(inputMap);

        assertNotNull(response);
        // The email validator returns an error response; issueDao should NOT be called
        verify(issueDao, never()).getAccountIdByEmail(any());
    }

    @Test
    @DisplayName("handleReenterGetUsersAccountId succeeds for valid email")
    void testHandleReenterGetUsersAccountId_ValidEmail() {
        when(issueDao.getAccountIdByEmail("user@example.com")).thenReturn("acc-xyz");

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Email", "user@example.com");

        ExtensionResponse response = subject.handleReenterGetUsersAccountId(inputMap);

        assertNotNull(response);
        verify(issueDao).getAccountIdByEmail("user@example.com");
    }

    @Test
    @DisplayName("handleReenterGetUsersAccountId returns error response when dao throws")
    void testHandleReenterGetUsersAccountId_DaoThrows() {
        when(issueDao.getAccountIdByEmail(any())).thenThrow(new RuntimeException("dao failure"));

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Email", "user@example.com");

        ExtensionResponse response = subject.handleReenterGetUsersAccountId(inputMap);

        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // confirmReenterCreateUser
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("confirmReenterCreateUser returns deny response when re-enter is false")
    void testConfirmReenterCreateUser_ReenterFalse() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchDenyResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, false,
                "stateId", stateId,
                "Email", "new.user@example.com");

        ExtensionResponse response = subject.confirmReenterCreateUser(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchDenyResponse(any(), any(), isNull(), any());
    }

    @Test
    @DisplayName("confirmReenterCreateUser returns fetch response when re-enter is true")
    void testConfirmReenterCreateUser_ReenterTrue() {
        String stateId = UUID.randomUUID().toString();
        Map<String, Object> state = Map.of(JiraConstants.VALIDATION_RESULT,
                List.of(SAMPLE_VALIDATION_RESULT));
        when(internalStateManager.get(stateId)).thenReturn(state);
        when(responseGenerator.generateFetchResponse(any(), any(), any(), any()))
                .thenReturn(mock(ExtensionResponse.class));

        Map<String, Object> inputMap = Map.of(
                JiraConstants.RE_ENTER, true,
                "stateId", stateId,
                "Email", "new.user@example.com");

        ExtensionResponse response = subject.confirmReenterCreateUser(inputMap);

        assertNotNull(response);
        verify(responseGenerator).generateFetchResponse(any(), any(),
                eq("handleReenterCreateUser"), any());
    }

    // -------------------------------------------------------------------------
    // handleReenterCreateUser
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenterCreateUser returns error for invalid email")
    void testHandleReenterCreateUser_InvalidEmail() {
        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Email", "bad-email");

        ExtensionResponse response = subject.handleReenterCreateUser(inputMap);

        assertNotNull(response);
        verify(issueDao, never()).createUser(any());
    }

    @Test
    @DisplayName("handleReenterCreateUser succeeds for valid email")
    void testHandleReenterCreateUser_ValidEmail() {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("Self", "https://example.atlassian.net/user?accountId=abc");
        userResponse.put("Account Id", "abc-123");
        userResponse.put("Email Address", "new.user@example.com");
        userResponse.put("Display Name", "New User");
        when(issueDao.createUser("new.user@example.com")).thenReturn(userResponse);

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Email", "new.user@example.com");

        ExtensionResponse response = subject.handleReenterCreateUser(inputMap);

        assertNotNull(response);
        verify(issueDao).createUser("new.user@example.com");
    }

    @Test
    @DisplayName("handleReenterCreateUser returns error response when dao throws")
    void testHandleReenterCreateUser_DaoThrows() {
        when(issueDao.createUser(any())).thenThrow(new RuntimeException("create failed"));

        Map<String, Object> inputMap = Map.of(
                "stateId", UUID.randomUUID().toString(),
                "Email", "new.user@example.com");

        ExtensionResponse response = subject.handleReenterCreateUser(inputMap);

        assertNotNull(response);
    }
}
