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

package app.krista.extensions.devsecops.development.jira.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for TelemetryConstants class.
 * Tests all public telemetry constant values.
 */
@DisplayName("TelemetryConstants Tests")
class TelemetryConstantsTest {

    @Test
    @DisplayName("Should have correct Performance constants")
    void testPerformanceConstants() {
        assertEquals("Jira.performance.request.duration", TelemetryConstants.Performance.REQUEST_DURATION);
        assertEquals("Jira.performance.api.duration", TelemetryConstants.Performance.API_CALL_DURATION);
        assertEquals("Jira.performance.validation.duration", TelemetryConstants.Performance.VALIDATION_DURATION);
        assertEquals("Jira.performance.database.duration", TelemetryConstants.Performance.DATABASE_QUERY_DURATION);
        assertEquals("Jira.performance.fileUpload.duration", TelemetryConstants.Performance.FILE_UPLOAD_DURATION);
        assertEquals("Jira.performance.jql.duration", TelemetryConstants.Performance.JQL_EXECUTION_DURATION);
    }

    @Test
    @DisplayName("Should have correct ErrorCategories constants")
    void testErrorCategoriesConstants() {
        assertEquals("Jira.error.authentication", TelemetryConstants.ErrorCategories.AUTHENTICATION_ERROR);
        assertEquals("Jira.error.authorization", TelemetryConstants.ErrorCategories.AUTHORIZATION_ERROR);
        assertEquals("Jira.error.validation", TelemetryConstants.ErrorCategories.VALIDATION_ERROR);
        assertEquals("Jira.error.network", TelemetryConstants.ErrorCategories.NETWORK_ERROR);
        assertEquals("Jira.error.jiraApi", TelemetryConstants.ErrorCategories.JIRA_API_ERROR);
        assertEquals("Jira.error.system", TelemetryConstants.ErrorCategories.SYSTEM_ERROR);
        assertEquals("Jira.error.userInput", TelemetryConstants.ErrorCategories.USER_INPUT_ERROR);
        assertEquals("Jira.error.timeout", TelemetryConstants.ErrorCategories.TIMEOUT_ERROR);
        assertEquals("Jira.error.rateLimit", TelemetryConstants.ErrorCategories.RATE_LIMIT_ERROR);
    }

    @Test
    @DisplayName("Should have correct UserExperience constants")
    void testUserExperienceConstants() {
        assertEquals("Jira.ux.retry.attempts", TelemetryConstants.UserExperience.RETRY_ATTEMPTS);
        assertEquals("Jira.ux.validation.failures", TelemetryConstants.UserExperience.VALIDATION_FAILURES);
        assertEquals("Jira.ux.completion.success", TelemetryConstants.UserExperience.SUCCESSFUL_COMPLETIONS);
        assertEquals("Jira.ux.abandonment", TelemetryConstants.UserExperience.USER_ABANDONMENT);
        assertEquals("Jira.ux.help.requests", TelemetryConstants.UserExperience.HELP_REQUESTS);
    }

    @Test
    @DisplayName("Should have correct FeatureUsage constants")
    void testFeatureUsageConstants() {
        assertEquals("Jira.feature.jql.complexity", TelemetryConstants.FeatureUsage.JQL_QUERY_COMPLEXITY);
        assertEquals("Jira.feature.bulk.operations", TelemetryConstants.FeatureUsage.BULK_OPERATIONS);
        assertEquals("Jira.feature.file.attachments", TelemetryConstants.FeatureUsage.FILE_ATTACHMENTS);
        assertEquals("Jira.feature.customFields.usage", TelemetryConstants.FeatureUsage.CUSTOM_FIELDS_USAGE);
        assertEquals("Jira.feature.workflow.transitions", TelemetryConstants.FeatureUsage.WORKFLOW_TRANSITIONS);
    }

    @Test
    @DisplayName("Should have correct OrganizationArea attach file constants")
    void testOrganizationAreaAttachFileConstants() {
        assertEquals("Jira.OrganizationArea.attachFileToTicket.count", 
            TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_COUNT);
        assertEquals("Jira.OrganizationArea.attachFileToTicket.failure", 
            TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_FAILURE);
        assertEquals("Jira.OrganizationArea.attachFileToTicket.success", 
            TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS);
        assertEquals("Jira.OrganizationArea.attachFileToTicket.retry.attempt", 
            TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_RETRY_ATTEMPT);
    }

    @Test
    @DisplayName("Should have correct OrganizationArea create issue constants")
    void testOrganizationAreaCreateIssueConstants() {
        assertEquals("Jira.OrganizationArea.createAnIssue.count", 
            TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_COUNT);
        assertEquals("Jira.OrganizationArea.createAnIssue.failure", 
            TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_FAILURE);
        assertEquals("Jira.OrganizationArea.createAnIssue.success", 
            TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_SUCCESS);
        assertEquals("Jira.OrganizationArea.createAnIssue.retry.attempt", 
            TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_RETRY_ATTEMPT);
    }

    @Test
    @DisplayName("Should have correct OrganizationArea report bug constants")
    void testOrganizationAreaReportBugConstants() {
        assertEquals("Jira.OrganizationArea.reportABug.count", 
            TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_COUNT);
        assertEquals("Jira.OrganizationArea.reportABug.failure", 
            TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_FAILURE);
        assertEquals("Jira.OrganizationArea.reportABug.success", 
            TelemetryConstants.OrganizationArea.REPORT_A_BUG_REQUEST_SUCCESS);
        assertEquals("Jira.OrganizationArea.reportABug.retry.attempt", 
            TelemetryConstants.OrganizationArea.REPORT_A_BUG_RETRY_ATTEMPT);
    }

    @Test
    @DisplayName("Should have correct OrganizationArea user management constants")
    void testOrganizationAreaUserManagementConstants() {
        // Create User
        assertEquals("Jira.OrganizationArea.createUser.count", 
            TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_COUNT);
        assertEquals("Jira.OrganizationArea.createUser.failure", 
            TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_FAILURE);
        assertEquals("Jira.OrganizationArea.createUser.success", 
            TelemetryConstants.OrganizationArea.CREATE_USER_REQUEST_SUCCESS);
        
        // Update User
        assertEquals("Jira.OrganizationArea.updateUser.count", 
            TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_COUNT);
        assertEquals("Jira.OrganizationArea.updateUser.failure", 
            TelemetryConstants.OrganizationArea.UPDATE_USER_REQUEST_FAILURE);
        
        // Delete User
        assertEquals("Jira.OrganizationArea.deleteUser.count", 
            TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_COUNT);
        assertEquals("Jira.OrganizationArea.deleteUser.success", 
            TelemetryConstants.OrganizationArea.DELETE_USER_REQUEST_SUCCESS);
        
        // Get Users Account Id
        assertEquals("Jira.OrganizationArea.getUsersAccountId.count", 
            TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_REQUEST_COUNT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea update status constants")
    void testRitualsAreaUpdateStatusConstants() {
        assertEquals("Jira.RitualsArea.updateStatusOfMultipleIssues.count", 
            TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.updateStatusOfMultipleIssues.failure", 
            TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.updateStatusOfMultipleIssues.success", 
            TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS);
        assertEquals("Jira.RitualsArea.updateStatusOfMultipleIssues.retry.attempt", 
            TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_MULTIPLE_ISSUES_RETRY_ATTEMPT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea comment constants")
    void testRitualsAreaCommentConstants() {
        assertEquals("Jira.RitualsArea.addComment.count", 
            TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.addComment.failure", 
            TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.addComment.success", 
            TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_SUCCESS);
        assertEquals("Jira.RitualsArea.addComment.retry.attempt", 
            TelemetryConstants.RitualsArea.ADD_COMMENT_RETRY_ATTEMPT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea get issues constants")
    void testRitualsAreaGetIssuesConstants() {
        assertEquals("Jira.RitualsArea.getUserIssues.count", 
            TelemetryConstants.RitualsArea.GET_USER_ISSUES_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.getIssues.count", 
            TelemetryConstants.RitualsArea.GET_ISSUES_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.getIssueByKey.count", 
            TelemetryConstants.RitualsArea.GET_ISSUE_BY_KEY_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.getUserIssueByIssueType.count", 
            TelemetryConstants.RitualsArea.GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_COUNT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea assignment constants")
    void testRitualsAreaAssignmentConstants() {
        assertEquals("Jira.RitualsArea.assignTaskToUser.count", 
            TelemetryConstants.RitualsArea.ASSIGN_TASK_TO_USER_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.assignIssueToEpic.count", 
            TelemetryConstants.RitualsArea.ASSIGN_ISSUE_TO_EPIC_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.assignChildIssueToEpic.count", 
            TelemetryConstants.RitualsArea.ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_COUNT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea JQL constants")
    void testRitualsAreaJQLConstants() {
        assertEquals("Jira.RitualsArea.fetchByJQLQuery.count", 
            TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.fetchByJQLQuery.failure", 
            TelemetryConstants.RitualsArea.FETCH_BY_JQL_QUERY_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.getResultsForFetchIssuesByJQLQuery.count", 
            TelemetryConstants.RitualsArea.GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_COUNT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea update constants")
    void testRitualsAreaUpdateConstants() {
        assertEquals("Jira.RitualsArea.updateAnIssue.count", 
            TelemetryConstants.RitualsArea.UPDATE_AN_ISSUE_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.updatePriorityOfIssue.count", 
            TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_ISSUE_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.updateThePriorityOfMultipleIssues.count", 
            TelemetryConstants.RitualsArea.UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.updateStatusOfIssue.count", 
            TelemetryConstants.RitualsArea.UPDATE_STATUS_OF_ISSUE_REQUEST_COUNT);
    }

    @Test
    @DisplayName("Should have correct RitualsArea work log constants")
    void testRitualsAreaWorkLogConstants() {
        assertEquals("Jira.RitualsArea.addWorkLog.count", 
            TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.addWorkLog.failure", 
            TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.addWorkLog.success", 
            TelemetryConstants.RitualsArea.ADD_WORK_LOG_REQUEST_SUCCESS);
    }

    @Test
    @DisplayName("Should have correct RitualsArea custom field constants")
    void testRitualsAreaCustomFieldConstants() {
        assertEquals("Jira.RitualsArea.getCustomFieldValue.count", 
            TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.getCustomFieldValue.failure", 
            TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.getCustomFieldValue.success", 
            TelemetryConstants.RitualsArea.GET_CUSTOM_FIELD_VALUE_REQUEST_SUCCESS);
    }

    @Test
    @DisplayName("Should have correct RitualsArea notification constants")
    void testRitualsAreaNotificationConstants() {
        assertEquals("Jira.RitualsArea.getNotification.count", 
            TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.getNotification.failure", 
            TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.getNotification.success", 
            TelemetryConstants.RitualsArea.GET_NOTIFICATION_REQUEST_SUCCESS);
    }

    @Test
    @DisplayName("Should have correct RitualsArea status filter constants")
    void testRitualsAreaStatusFilterConstants() {
        assertEquals("Jira.RitualsArea.getMyIssuesByGivenStatuses.count", 
            TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_COUNT);
        assertEquals("Jira.RitualsArea.getMyIssuesByGivenStatuses.failure", 
            TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_FAILURE);
        assertEquals("Jira.RitualsArea.getMyIssuesByGivenStatuses.success", 
            TelemetryConstants.RitualsArea.GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_SUCCESS);
    }

    @Test
    @DisplayName("Should have correct ReleaseManagementArea constants")
    void testReleaseManagementAreaConstants() {
        assertEquals("Jira.ReleaseManagementArea.getReleaseSummary.count", 
            TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_COUNT);
        assertEquals("Jira.ReleaseManagementArea.getReleaseSummary.failure", 
            TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_FAILURE);
        assertEquals("Jira.ReleaseManagementArea.getReleaseSummary.success", 
            TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_SUCCESS);
        assertEquals("Jira.ReleaseManagementArea.getReleaseSummary.retry.attempt", 
            TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_RETRY_ATTEMPT);
    }

    @Test
    @DisplayName("All telemetry constants should follow naming convention")
    void testNamingConvention() {
        // Performance constants should start with "Jira.performance"
        assertTrue(TelemetryConstants.Performance.REQUEST_DURATION.startsWith("Jira.performance"));
        
        // Error constants should start with "Jira.error"
        assertTrue(TelemetryConstants.ErrorCategories.AUTHENTICATION_ERROR.startsWith("Jira.error"));
        
        // UX constants should start with "Jira.ux"
        assertTrue(TelemetryConstants.UserExperience.RETRY_ATTEMPTS.startsWith("Jira.ux"));
        
        // Feature constants should start with "Jira.feature"
        assertTrue(TelemetryConstants.FeatureUsage.JQL_QUERY_COMPLEXITY.startsWith("Jira.feature"));
        
        // Organization constants should start with "Jira.OrganizationArea"
        assertTrue(TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_REQUEST_COUNT.startsWith("Jira.OrganizationArea"));
        
        // Rituals constants should start with "Jira.RitualsArea"
        assertTrue(TelemetryConstants.RitualsArea.ADD_COMMENT_REQUEST_COUNT.startsWith("Jira.RitualsArea"));
        
        // Release constants should start with "Jira.ReleaseManagementArea"
        assertTrue(TelemetryConstants.ReleaseManagementArea.GET_RELEASE_SUMMARY_REQUEST_COUNT.startsWith("Jira.ReleaseManagementArea"));
    }
}

