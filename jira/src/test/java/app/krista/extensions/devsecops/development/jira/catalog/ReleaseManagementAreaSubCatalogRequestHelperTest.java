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
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("ReleaseManagementAreaSubCatalogRequestHelper Tests")
class ReleaseManagementAreaSubCatalogRequestHelperTest {

    @Mock
    private ReleaseDao releaseDao;
    @Mock
    private UserDao userDao;
    @Mock
    private ExtensionResponseGenerator responseGenerator;
    @Mock
    private ErrorHandlingStateManager internalStateManager;

    private ReleaseManagementAreaSubCatalogRequestHelper subject;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        subject = new ReleaseManagementAreaSubCatalogRequestHelper(
                releaseDao, userDao, responseGenerator, internalStateManager);
    }

    // -------------------------------------------------------------------------
    // assigneeValidator
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("assigneeValidator returns null when assignee is null")
    void testAssigneeValidator_NullAssignee() {
        ExtensionResponse response = subject.assigneeValidator(null);
        assertNull(response);
    }

    @Test
    @DisplayName("assigneeValidator returns null when assignee is empty string")
    void testAssigneeValidator_EmptyAssignee() {
        ExtensionResponse response = subject.assigneeValidator("");
        assertNull(response);
    }

    @Test
    @DisplayName("assigneeValidator returns null when assignee exists in system")
    void testAssigneeValidator_ExistingAssignee() {
        when(userDao.isUserExist("acc-123")).thenReturn(true);
        ExtensionResponse response = subject.assigneeValidator("acc-123");
        assertNull(response);
    }

    @Test
    @DisplayName("assigneeValidator returns error response when assignee does not exist")
    void testAssigneeValidator_NonExistentAssignee() {
        when(userDao.isUserExist("unknown-acc")).thenReturn(false);
        ExtensionResponse response = subject.assigneeValidator("unknown-acc");
        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // pageSizeValidator
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("pageSizeValidator returns null when pageSize is null")
    void testPageSizeValidator_Null() {
        ExtensionResponse response = subject.pageSizeValidator(null);
        assertNull(response);
    }

    @Test
    @DisplayName("pageSizeValidator returns null for a valid page size of 20")
    void testPageSizeValidator_ValidSize() {
        ExtensionResponse response = subject.pageSizeValidator(20.0);
        assertNull(response);
    }

    @Test
    @DisplayName("pageSizeValidator returns null for the boundary value of 1")
    void testPageSizeValidator_BoundaryMin() {
        ExtensionResponse response = subject.pageSizeValidator(1.0);
        assertNull(response);
    }

    @Test
    @DisplayName("pageSizeValidator returns null for the boundary value of 100")
    void testPageSizeValidator_BoundaryMax() {
        ExtensionResponse response = subject.pageSizeValidator(100.0);
        assertNull(response);
    }

    @Test
    @DisplayName("pageSizeValidator returns error when pageSize is 0")
    void testPageSizeValidator_Zero() {
        ExtensionResponse response = subject.pageSizeValidator(0.0);
        assertNotNull(response);
    }

    @Test
    @DisplayName("pageSizeValidator returns error when pageSize exceeds 100")
    void testPageSizeValidator_TooLarge() {
        ExtensionResponse response = subject.pageSizeValidator(101.0);
        assertNotNull(response);
    }

    @Test
    @DisplayName("pageSizeValidator returns error when pageSize is negative")
    void testPageSizeValidator_Negative() {
        ExtensionResponse response = subject.pageSizeValidator(-5.0);
        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // pageNumberValidator
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("pageNumberValidator returns null when pageNumber is null")
    void testPageNumberValidator_Null() {
        ExtensionResponse response = subject.pageNumberValidator(null);
        assertNull(response);
    }

    @Test
    @DisplayName("pageNumberValidator returns null for a valid page number of 1")
    void testPageNumberValidator_ValidNumber() {
        ExtensionResponse response = subject.pageNumberValidator(1.0);
        assertNull(response);
    }

    @Test
    @DisplayName("pageNumberValidator returns error when pageNumber is 0")
    void testPageNumberValidator_Zero() {
        ExtensionResponse response = subject.pageNumberValidator(0.0);
        assertNotNull(response);
    }

    @Test
    @DisplayName("pageNumberValidator returns error when pageNumber is negative")
    void testPageNumberValidator_Negative() {
        ExtensionResponse response = subject.pageNumberValidator(-1.0);
        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // handleReenteredForGetReleaseSummary
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("handleReenteredForGetReleaseSummary succeeds with default page parameters")
    void testHandleReenteredForGetReleaseSummary_DefaultParams() {
        ReleaseSummaryResponse summaryResponse = new ReleaseSummaryResponse();
        when(releaseDao.getGetReleaseSummaryResponse(any(), any(), any(), any(), anyLong(), anyLong(), any()))
                .thenReturn(summaryResponse);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status", null);
        inputMap.put("Assignee", null);
        inputMap.put("Priority", null);
        inputMap.put("Issue type", null);
        inputMap.put("Page Size", null);
        inputMap.put("Page Number", null);
        inputMap.put("Fix version", "v1.0");

        ExtensionResponse response = subject.handleReenteredForGetReleaseSummary(inputMap);

        assertNotNull(response);
        // pageSize null -> default 20; pageNumber null -> default 1
        verify(releaseDao).getGetReleaseSummaryResponse(isNull(), isNull(), isNull(), isNull(),
                eq(20L), eq(1L), eq("v1.0"));
    }

    @Test
    @DisplayName("handleReenteredForGetReleaseSummary succeeds with explicit page parameters")
    void testHandleReenteredForGetReleaseSummary_ExplicitParams() {
        when(userDao.isUserExist("acc-123")).thenReturn(true);
        ReleaseSummaryResponse summaryResponse = new ReleaseSummaryResponse();
        when(releaseDao.getGetReleaseSummaryResponse(any(), any(), any(), any(), anyLong(), anyLong(), any()))
                .thenReturn(summaryResponse);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status", "Open");
        inputMap.put("Assignee", "acc-123");
        inputMap.put("Priority", "High");
        inputMap.put("Issue type", "Bug");
        inputMap.put("Page Size", 10.0);
        inputMap.put("Page Number", 2.0);
        inputMap.put("Fix version", "v2.0");

        ExtensionResponse response = subject.handleReenteredForGetReleaseSummary(inputMap);

        assertNotNull(response);
        verify(releaseDao).getGetReleaseSummaryResponse(eq("Open"), eq("acc-123"), eq("High"), eq("Bug"),
                eq(10L), eq(2L), eq("v2.0"));
    }

    @Test
    @DisplayName("handleReenteredForGetReleaseSummary returns error when assignee does not exist")
    void testHandleReenteredForGetReleaseSummary_InvalidAssignee() {
        when(userDao.isUserExist("bad-acc")).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status", null);
        inputMap.put("Assignee", "bad-acc");
        inputMap.put("Priority", null);
        inputMap.put("Issue type", null);
        inputMap.put("Page Size", null);
        inputMap.put("Page Number", null);
        inputMap.put("Fix version", null);

        ExtensionResponse response = subject.handleReenteredForGetReleaseSummary(inputMap);

        assertNotNull(response);
        verify(releaseDao, never()).getGetReleaseSummaryResponse(any(), any(), any(), any(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("handleReenteredForGetReleaseSummary returns error when page size is invalid")
    void testHandleReenteredForGetReleaseSummary_InvalidPageSize() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status", null);
        inputMap.put("Assignee", null);
        inputMap.put("Priority", null);
        inputMap.put("Issue type", null);
        inputMap.put("Page Size", 0.0);
        inputMap.put("Page Number", null);
        inputMap.put("Fix version", null);

        ExtensionResponse response = subject.handleReenteredForGetReleaseSummary(inputMap);

        assertNotNull(response);
        verify(releaseDao, never()).getGetReleaseSummaryResponse(any(), any(), any(), any(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("handleReenteredForGetReleaseSummary returns error when page number is invalid")
    void testHandleReenteredForGetReleaseSummary_InvalidPageNumber() {
        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status", null);
        inputMap.put("Assignee", null);
        inputMap.put("Priority", null);
        inputMap.put("Issue type", null);
        inputMap.put("Page Size", 10.0);
        inputMap.put("Page Number", -1.0);
        inputMap.put("Fix version", null);

        ExtensionResponse response = subject.handleReenteredForGetReleaseSummary(inputMap);

        assertNotNull(response);
        verify(releaseDao, never()).getGetReleaseSummaryResponse(any(), any(), any(), any(), anyLong(), anyLong(), any());
    }

    @Test
    @DisplayName("handleReenteredForGetReleaseSummary returns error response when dao throws")
    void testHandleReenteredForGetReleaseSummary_DaoThrows() {
        when(releaseDao.getGetReleaseSummaryResponse(any(), any(), any(), any(), anyLong(), anyLong(), any()))
                .thenThrow(new RuntimeException("dao failure"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status", null);
        inputMap.put("Assignee", null);
        inputMap.put("Priority", null);
        inputMap.put("Issue type", null);
        inputMap.put("Page Size", null);
        inputMap.put("Page Number", null);
        inputMap.put("Fix version", null);

        ExtensionResponse response = subject.handleReenteredForGetReleaseSummary(inputMap);

        assertNotNull(response);
    }

    // -------------------------------------------------------------------------
    // getValidationResults (static utility)
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getValidationResults returns the list stored under the VALIDATION_RESULT key")
    void testGetValidationResults() {
        Map<String, Object> state = Map.of(
                "Validation Results",
                java.util.List.of("result1", "result2"));

        Object results = ReleaseManagementAreaSubCatalogRequestHelper.getValidationResults(state);

        assertNotNull(results);
    }
}
