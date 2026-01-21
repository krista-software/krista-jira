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

public class TelemetryConstants {

    // ========================================
    // ENHANCED TELEMETRY CONSTANTS
    // ========================================

    // Performance Metrics
    public static final class Performance {
        public static final String REQUEST_DURATION = "Jira.performance.request.duration";
        public static final String API_CALL_DURATION = "Jira.performance.api.duration";
        public static final String VALIDATION_DURATION = "Jira.performance.validation.duration";
        public static final String DATABASE_QUERY_DURATION = "Jira.performance.database.duration";
        public static final String FILE_UPLOAD_DURATION = "Jira.performance.fileUpload.duration";
        public static final String JQL_EXECUTION_DURATION = "Jira.performance.jql.duration";
    }

    // Error Categorization
    public static final class ErrorCategories {
        public static final String AUTHENTICATION_ERROR = "Jira.error.authentication";
        public static final String AUTHORIZATION_ERROR = "Jira.error.authorization";
        public static final String VALIDATION_ERROR = "Jira.error.validation";
        public static final String NETWORK_ERROR = "Jira.error.network";
        public static final String JIRA_API_ERROR = "Jira.error.jiraApi";
        public static final String SYSTEM_ERROR = "Jira.error.system";
        public static final String USER_INPUT_ERROR = "Jira.error.userInput";
        public static final String TIMEOUT_ERROR = "Jira.error.timeout";
        public static final String RATE_LIMIT_ERROR = "Jira.error.rateLimit";
    }

    // User Experience Metrics
    public static final class UserExperience {
        public static final String RETRY_ATTEMPTS = "Jira.ux.retry.attempts";
        public static final String VALIDATION_FAILURES = "Jira.ux.validation.failures";
        public static final String SUCCESSFUL_COMPLETIONS = "Jira.ux.completion.success";
        public static final String USER_ABANDONMENT = "Jira.ux.abandonment";
        public static final String HELP_REQUESTS = "Jira.ux.help.requests";
    }

    // Feature Usage Analytics
    public static final class FeatureUsage {
        public static final String JQL_QUERY_COMPLEXITY = "Jira.feature.jql.complexity";
        public static final String BULK_OPERATIONS = "Jira.feature.bulk.operations";
        public static final String FILE_ATTACHMENTS = "Jira.feature.file.attachments";
        public static final String CUSTOM_FIELDS_USAGE = "Jira.feature.customFields.usage";
        public static final String WORKFLOW_TRANSITIONS = "Jira.feature.workflow.transitions";
    }

    public static final class OrganizationArea {
        // Attach File to Ticket
        public static final String ATTACH_FILE_TO_TICKET_REQUEST_COUNT = "Jira.OrganizationArea.attachFileToTicket.count";
        public static final String ATTACH_FILE_TO_TICKET_REQUEST_FAILURE = "Jira.OrganizationArea.attachFileToTicket.failure";
        public static final String ATTACH_FILE_TO_TICKET_REQUEST_SUCCESS = "Jira.OrganizationArea.attachFileToTicket.success";
        public static final String ATTACH_FILE_TO_TICKET_RETRY_ATTEMPT = "Jira.OrganizationArea.attachFileToTicket.retry.attempt";

        // Create an Issue
        public static final String CREATE_AN_ISSUE_REQUEST_COUNT = "Jira.OrganizationArea.createAnIssue.count";
        public static final String CREATE_AN_ISSUE_REQUEST_FAILURE = "Jira.OrganizationArea.createAnIssue.failure";
        public static final String CREATE_AN_ISSUE_REQUEST_SUCCESS = "Jira.OrganizationArea.createAnIssue.success";
        public static final String CREATE_AN_ISSUE_RETRY_ATTEMPT = "Jira.OrganizationArea.createAnIssue.retry.attempt";

        // Report a Bug
        public static final String REPORT_A_BUG_REQUEST_COUNT = "Jira.OrganizationArea.reportABug.count";
        public static final String REPORT_A_BUG_REQUEST_FAILURE = "Jira.OrganizationArea.reportABug.failure";
        public static final String REPORT_A_BUG_REQUEST_SUCCESS = "Jira.OrganizationArea.reportABug.success";
        public static final String REPORT_A_BUG_RETRY_ATTEMPT = "Jira.OrganizationArea.reportABug.retry.attempt";

        // Create User
        public static final String CREATE_USER_REQUEST_COUNT = "Jira.OrganizationArea.createUser.count";
        public static final String CREATE_USER_REQUEST_FAILURE = "Jira.OrganizationArea.createUser.failure";
        public static final String CREATE_USER_REQUEST_SUCCESS = "Jira.OrganizationArea.createUser.success";
        public static final String CREATE_USER_RETRY_ATTEMPT = "Jira.OrganizationArea.createUser.retry.attempt";

        // Update User
        public static final String UPDATE_USER_REQUEST_COUNT = "Jira.OrganizationArea.updateUser.count";
        public static final String UPDATE_USER_REQUEST_FAILURE = "Jira.OrganizationArea.updateUser.failure";
        public static final String UPDATE_USER_REQUEST_SUCCESS = "Jira.OrganizationArea.updateUser.success";
        public static final String UPDATE_USER_RETRY_ATTEMPT = "Jira.OrganizationArea.updateUser.retry.attempt";

        // Delete User
        public static final String DELETE_USER_REQUEST_COUNT = "Jira.OrganizationArea.deleteUser.count";
        public static final String DELETE_USER_REQUEST_FAILURE = "Jira.OrganizationArea.deleteUser.failure";
        public static final String DELETE_USER_REQUEST_SUCCESS = "Jira.OrganizationArea.deleteUser.success";
        public static final String DELETE_USER_RETRY_ATTEMPT = "Jira.OrganizationArea.deleteUser.retry.attempt";

        // Get Users Account Id
        public static final String GET_USERS_ACCOUNT_ID_REQUEST_COUNT = "Jira.OrganizationArea.getUsersAccountId.count";
        public static final String GET_USERS_ACCOUNT_ID_REQUEST_FAILURE = "Jira.OrganizationArea.getUsersAccountId.failure";
        public static final String GET_USERS_ACCOUNT_ID_REQUEST_SUCCESS = "Jira.OrganizationArea.getUsersAccountId.success";
        public static final String GET_USERS_ACCOUNT_ID_RETRY_ATTEMPT = "Jira.OrganizationArea.getUsersAccountId.retry.attempt";

        // Get User Account Information
        public static final String GET_USER_ACCOUNT_INFORMATION_REQUEST_COUNT = "Jira.OrganizationArea.getUserAccountInformation.count";
        public static final String GET_USER_ACCOUNT_INFORMATION_REQUEST_FAILURE = "Jira.OrganizationArea.getUserAccountInformation.failure";
        public static final String GET_USER_ACCOUNT_INFORMATION_REQUEST_SUCCESS = "Jira.OrganizationArea.getUserAccountInformation.success";
        public static final String GET_USER_ACCOUNT_INFORMATION_RETRY_ATTEMPT = "Jira.OrganizationArea.getUserAccountInformation.retry.attempt";

    }

    public static final class RitualsArea {
        // Update Status of Multiple Issues
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_COUNT = "Jira.RitualsArea.updateStatusOfMultipleIssues.count";
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_FAILURE = "Jira.RitualsArea.updateStatusOfMultipleIssues.failure";
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS = "Jira.RitualsArea.updateStatusOfMultipleIssues.success";
        public static final String UPDATE_STATUS_OF_MULTIPLE_ISSUES_RETRY_ATTEMPT = "Jira.RitualsArea.updateStatusOfMultipleIssues.retry.attempt";

        // Add Comment
        public static final String ADD_COMMENT_REQUEST_COUNT = "Jira.RitualsArea.addComment.count";
        public static final String ADD_COMMENT_REQUEST_FAILURE = "Jira.RitualsArea.addComment.failure";
        public static final String ADD_COMMENT_REQUEST_SUCCESS = "Jira.RitualsArea.addComment.success";
        public static final String ADD_COMMENT_RETRY_ATTEMPT = "Jira.RitualsArea.addComment.retry.attempt";

        // Get User Issues
        public static final String GET_USER_ISSUES_REQUEST_COUNT = "Jira.RitualsArea.getUserIssues.count";
        public static final String GET_USER_ISSUES_REQUEST_FAILURE = "Jira.RitualsArea.getUserIssues.failure";
        public static final String GET_USER_ISSUES_REQUEST_SUCCESS = "Jira.RitualsArea.getUserIssues.success";
        public static final String GET_USER_ISSUES_RETRY_ATTEMPT = "Jira.RitualsArea.getUserIssues.retry.attempt";

        // Assign Task to User
        public static final String ASSIGN_TASK_TO_USER_REQUEST_COUNT = "Jira.RitualsArea.assignTaskToUser.count";
        public static final String ASSIGN_TASK_TO_USER_REQUEST_FAILURE = "Jira.RitualsArea.assignTaskToUser.failure";
        public static final String ASSIGN_TASK_TO_USER_REQUEST_SUCCESS = "Jira.RitualsArea.assignTaskToUser.success";
        public static final String ASSIGN_TASK_TO_USER_RETRY_ATTEMPT = "Jira.RitualsArea.assignTaskToUser.retry.attempt";

        // Fetch by JQL Query
        public static final String FETCH_BY_JQL_QUERY_REQUEST_COUNT = "Jira.RitualsArea.fetchByJQLQuery.count";
        public static final String FETCH_BY_JQL_QUERY_REQUEST_FAILURE = "Jira.RitualsArea.fetchByJQLQuery.failure";
        public static final String FETCH_BY_JQL_QUERY_REQUEST_SUCCESS = "Jira.RitualsArea.fetchByJQLQuery.success";
        public static final String FETCH_BY_JQL_QUERY_RETRY_ATTEMPT = "Jira.RitualsArea.fetchByJQLQuery.retry.attempt";

        // Get Issues
        public static final String GET_ISSUES_REQUEST_COUNT = "Jira.RitualsArea.getIssues.count";
        public static final String GET_ISSUES_REQUEST_FAILURE = "Jira.RitualsArea.getIssues.failure";
        public static final String GET_ISSUES_REQUEST_SUCCESS = "Jira.RitualsArea.getIssues.success";
        public static final String GET_ISSUES_RETRY_ATTEMPT = "Jira.RitualsArea.getIssues.retry.attempt";

        // Get User Issue by Issue Type
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_COUNT = "Jira.RitualsArea.getUserIssueByIssueType.count";
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_FAILURE = "Jira.RitualsArea.getUserIssueByIssueType.failure";
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_REQUEST_SUCCESS = "Jira.RitualsArea.getUserIssueByIssueType.success";
        public static final String GET_USER_ISSUE_BY_ISSUE_TYPE_RETRY_ATTEMPT = "Jira.RitualsArea.getUserIssueByIssueType.retry.attempt";

        // Add Work Log
        public static final String ADD_WORK_LOG_REQUEST_COUNT = "Jira.RitualsArea.addWorkLog.count";
        public static final String ADD_WORK_LOG_REQUEST_FAILURE = "Jira.RitualsArea.addWorkLog.failure";
        public static final String ADD_WORK_LOG_REQUEST_SUCCESS = "Jira.RitualsArea.addWorkLog.success";
        public static final String ADD_WORK_LOG_RETRY_ATTEMPT = "Jira.RitualsArea.addWorkLog.retry.attempt";

        // Get Issue by Key
        public static final String GET_ISSUE_BY_KEY_REQUEST_COUNT = "Jira.RitualsArea.getIssueByKey.count";
        public static final String GET_ISSUE_BY_KEY_REQUEST_FAILURE = "Jira.RitualsArea.getIssueByKey.failure";
        public static final String GET_ISSUE_BY_KEY_REQUEST_SUCCESS = "Jira.RitualsArea.getIssueByKey.success";
        public static final String GET_ISSUE_BY_KEY_RETRY_ATTEMPT = "Jira.RitualsArea.getIssueByKey.retry.attempt";

        // Assign Issue to Epic
        public static final String ASSIGN_ISSUE_TO_EPIC_REQUEST_COUNT = "Jira.RitualsArea.assignIssueToEpic.count";
        public static final String ASSIGN_ISSUE_TO_EPIC_REQUEST_FAILURE = "Jira.RitualsArea.assignIssueToEpic.failure";
        public static final String ASSIGN_ISSUE_TO_EPIC_REQUEST_SUCCESS = "Jira.RitualsArea.assignIssueToEpic.success";
        public static final String ASSIGN_ISSUE_TO_EPIC_RETRY_ATTEMPT = "Jira.RitualsArea.assignIssueToEpic.retry.attempt";

        // Get Custom Field Value
        public static final String GET_CUSTOM_FIELD_VALUE_REQUEST_COUNT = "Jira.RitualsArea.getCustomFieldValue.count";
        public static final String GET_CUSTOM_FIELD_VALUE_REQUEST_FAILURE = "Jira.RitualsArea.getCustomFieldValue.failure";
        public static final String GET_CUSTOM_FIELD_VALUE_REQUEST_SUCCESS = "Jira.RitualsArea.getCustomFieldValue.success";
        public static final String GET_CUSTOM_FIELD_VALUE_RETRY_ATTEMPT = "Jira.RitualsArea.getCustomFieldValue.retry.attempt";

        // Update Status of Issue
        public static final String UPDATE_STATUS_OF_ISSUE_REQUEST_COUNT = "Jira.RitualsArea.updateStatusOfIssue.count";
        public static final String UPDATE_STATUS_OF_ISSUE_REQUEST_FAILURE = "Jira.RitualsArea.updateStatusOfIssue.failure";
        public static final String UPDATE_STATUS_OF_ISSUE_REQUEST_SUCCESS = "Jira.RitualsArea.updateStatusOfIssue.success";
        public static final String UPDATE_STATUS_OF_ISSUE_RETRY_ATTEMPT = "Jira.RitualsArea.updateStatusOfIssue.retry.attempt";

        // Get Results for Fetch Issues by JQL Query
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_COUNT = "Jira.RitualsArea.getResultsForFetchIssuesByJQLQuery.count";
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_FAILURE = "Jira.RitualsArea.getResultsForFetchIssuesByJQLQuery.failure";
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_REQUEST_SUCCESS = "Jira.RitualsArea.getResultsForFetchIssuesByJQLQuery.success";
        public static final String GET_RESULTS_FOR_FETCH_ISSUES_BY_JQL_QUERY_RETRY_ATTEMPT = "Jira.RitualsArea.getResultsForFetchIssuesByJQLQuery.retry.attempt";

        // Get My Issues by Given Statuses
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_COUNT = "Jira.RitualsArea.getMyIssuesByGivenStatuses.count";
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_FAILURE = "Jira.RitualsArea.getMyIssuesByGivenStatuses.failure";
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_REQUEST_SUCCESS = "Jira.RitualsArea.getMyIssuesByGivenStatuses.success";
        public static final String GET_MY_ISSUES_BY_GIVEN_STATUSES_RETRY_ATTEMPT = "Jira.RitualsArea.getMyIssuesByGivenStatuses.retry.attempt";

        // Update An Issue
        public static final String UPDATE_AN_ISSUE_REQUEST_COUNT = "Jira.RitualsArea.updateAnIssue.count";
        public static final String UPDATE_AN_ISSUE_REQUEST_FAILURE = "Jira.RitualsArea.updateAnIssue.failure";
        public static final String UPDATE_AN_ISSUE_REQUEST_SUCCESS = "Jira.RitualsArea.updateAnIssue.success";
        public static final String UPDATE_AN_ISSUE_RETRY_ATTEMPT = "Jira.RitualsArea.updateAnIssue.retry.attempt";

        // Update Priority of Issue
        public static final String UPDATE_PRIORITY_OF_ISSUE_REQUEST_COUNT = "Jira.RitualsArea.updatePriorityOfIssue.count";
        public static final String UPDATE_PRIORITY_OF_ISSUE_REQUEST_FAILURE = "Jira.RitualsArea.updatePriorityOfIssue.failure";
        public static final String UPDATE_PRIORITY_OF_ISSUE_REQUEST_SUCCESS = "Jira.RitualsArea.updatePriorityOfIssue.success";
        public static final String UPDATE_PRIORITY_OF_ISSUE_RETRY_ATTEMPT = "Jira.RitualsArea.updatePriorityOfIssue.retry.attempt";

        // Update Priority of Multiple Issues
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_COUNT = "Jira.RitualsArea.updateThePriorityOfMultipleIssues.count";
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_FAILURE = "Jira.RitualsArea.updateThePriorityOfMultipleIssues.failure";
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_REQUEST_SUCCESS = "Jira.RitualsArea.updateThePriorityOfMultipleIssues.success";
        public static final String UPDATE_PRIORITY_OF_MULTIPLE_ISSUES_RETRY_ATTEMPT = "Jira.RitualsArea.updateThePriorityOfMultipleIssues.retry.attempt";

        // Assign Child Issue to Epic
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_COUNT = "Jira.RitualsArea.assignChildIssueToEpic.count";
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_FAILURE = "Jira.RitualsArea.assignChildIssueToEpic.failure";
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_REQUEST_SUCCESS = "Jira.RitualsArea.assignChildIssueToEpic.success";
        public static final String ASSIGN_CHILD_ISSUE_TO_EPIC_RETRY_ATTEMPT = "Jira.RitualsArea.assignChildIssueToEpic.retry.attempt";

        // Get Notification
        public static final String GET_NOTIFICATION_REQUEST_COUNT = "Jira.RitualsArea.getNotification.count";
        public static final String GET_NOTIFICATION_REQUEST_FAILURE = "Jira.RitualsArea.getNotification.failure";
        public static final String GET_NOTIFICATION_REQUEST_SUCCESS = "Jira.RitualsArea.getNotification.success";
        public static final String GET_NOTIFICATION_RETRY_ATTEMPT = "Jira.RitualsArea.getNotification.retry.attempt";
    }

    public static final class ReleaseManagementArea {
        // Get Release Summary
        public static final String GET_RELEASE_SUMMARY_REQUEST_COUNT = "Jira.ReleaseManagementArea.getReleaseSummary.count";
        public static final String GET_RELEASE_SUMMARY_REQUEST_FAILURE = "Jira.ReleaseManagementArea.getReleaseSummary.failure";
        public static final String GET_RELEASE_SUMMARY_REQUEST_SUCCESS = "Jira.ReleaseManagementArea.getReleaseSummary.success";
        public static final String GET_RELEASE_SUMMARY_RETRY_ATTEMPT = "Jira.ReleaseManagementArea.getReleaseSummary.retry.attempt";

    }

    public static final class SetUpArea {
        // Health Check
        public static final String HEALTHCHECK_REQUEST_TOTAL = "Jira.SetUpArea.healthcheck.total";
        public static final String HEALTHCHECK_REQUEST_SUCCESS = "Jira.SetUpArea.healthcheck.success";
        public static final String HEALTHCHECK_REQUEST_FAILURE = "Jira.SetUpArea.healthcheck.failure";

        // Save Configuration
        public static final String SAVE_CONFIGURATION_REQUEST_TOTAL = "Jira.SetUpArea.saveConfiguration.total";
        public static final String SAVE_CONFIGURATION_REQUEST_SUCCESS = "Jira.SetUpArea.saveConfiguration.success";
        public static final String SAVE_CONFIGURATION_REQUEST_FAILURE = "Jira.SetUpArea.saveConfiguration.failure";
    }
}
