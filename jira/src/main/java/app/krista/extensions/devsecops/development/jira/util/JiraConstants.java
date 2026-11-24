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

import app.krista.sdk.atlassian.constants.AtlassianConstants;
import com.google.gson.Gson;

public final class JiraConstants {
    public static final String STATE = AtlassianConstants.STATE;
    public static final String STRONG = "strong";
    public static final String EM = "em";
    public static final String UNDERLINE = "underline";
    public static final String STRIKE = "strike";
    public static final String CODE_MARK = "code";
    public static final String LINK = "link";
    public static final String GROUP_NAMES = "Group Names";
    public static final String COMMENT = "Comment";
    public static final String COMMENT_VISIBILITY = "Comment Visibility";
    public static final String VALIDATION_RESULT = "Validation Results";
    public static final String ASSIGNEE = "Assignee";

    private JiraConstants() {
    }

    public static final Gson GSON = AtlassianConstants.GSON;
    public static final Gson GSON_BUILDER = AtlassianConstants.PRETTY_GSON;
    public static final String NA = "NA";
    public static final String ACCEPT = AtlassianConstants.ACCEPT;
    public static final String X_ATLASSIAN_TOKEN = AtlassianConstants.X_ATLASSIAN_TOKEN;
    public static final String NO_CHECK = AtlassianConstants.NO_CHECK;
    public static final String BUG = "Bug";
    public static final String EPIC = "Epic";
    public static final String ISSUE = "Issue";
    public static final String ISSUE_TYPE = "Issue Type";
    public static final String STORY = "Story";
    public static final String TASK = "Task";
    public static final String SUBTASK = "Sub-task";
    public static final String SUBTASK_ENTITY = "Subtask";
    public static final String TICKET_PRIORITY = "Ticket Priority";
    public static final String TICKET_PROJECT = "Ticket Project";
    public static final String TICKET_STATUS = "Ticket Status";
    public static final String USER = "User";
    public static final String VERSION = "Version";
    public static final String AND = " AND ";
    public static final String LOGICAL_NAME_FIELD = "logicalName";
    public static final String REFRESH_TOKEN = AtlassianConstants.REFRESH_TOKEN;
    public static final String CODE = AtlassianConstants.CODE;
    public static final String JAXRS_ID = "jiraextn";
    public static final String BEARER = AtlassianConstants.BEARER;
    public static final String PROJECT_KEY = "Project Key";
    public static final String SUMMARY = "Summary";
    public static final String DESCRIPTION = "Description";
    public static final String COMMENTS = "Comments";
    public static final String REPORTER_NAME = "Reporter Name";
    public static final String REPORTER_ACCOUNT_ID = "Reporter Account Id";
    public static final String ASSIGNEE_NAME = "Assignee Name";
    public static final String ID = "Id";
    public static final String TYPE = "Type";
    public static final String _TYPE = "type";

    public static final String PRIORITY = "Priority";
    public static final String STATUS = "Status";
    public static final String PARENT_KEY = "Parent Key";

    public static final String LABELS = "Labels";

    public static final String COMPONENTS = "Components";
    public static final String LABEL = "Label";
    public static final String COMPONENT = "Component";
    public static final String _DESCRIPTION = "description";
    public static final String _SUMMARY = "summary";
    public static final String STORY_KEY = "story key";
    public static final long DEFAULT_PAGE_SIZE = 50;
    public static final long PAGE_NUMBER = 1;

    public static final String UPDATES_ON_JIRA = "Updates On Jira";
    public static final String JIRA_UPDATES = "Jira Updates";

    public static final String ISSUE_ = "issue";
    public static final String UPDATE = "Update";
    public static final String ISSUE_KEY = "Issue Key";
    public static final String AUTHORIZATION_PROMPT = "To complete the setup, please:\n1. Click the 'Authorize' button to connect your JIRA account\n2. Sign in and grant the requested permissions\n3. Click 'Validate Attributes' to verify the connection\n4. Click 'Save' to apply your changes\n\nThis ensures your account is properly connected before saving.";

    public static final String CONTENT = "content";
    public static final String TEXT = "text";
    public static final String ATTRS = "attrs";
    public static final String STATE_ID = "stateId";

    public static final String ERRORS = "errors";
    public static final String QUERIES = "queries";
    public static final String QUERY = "query";

    public static final String JQL_QUERY = "JQL Query";
    public static final String VALUE = "value";
    public static final String RE_ENTER = "re-enter";

    // ========================================
    // ERROR MESSAGES - User-Friendly Format
    // ========================================

    // Authentication & Authorization Errors
    public static final String ERROR_AUTHENTICATION_REQUIRED = "Authentication is required to access JIRA. Please authorize your account by clicking the 'Authorize' button and signing in with your JIRA credentials.";
    public static final String ERROR_AUTHORIZATION_FAILED = "Authorization failed. Please ensure you have the necessary permissions in JIRA and try authorizing again. Contact your JIRA administrator if the issue persists.";
    public static final String ERROR_TOKEN_EXPIRED = "Your JIRA session has expired. Please re-authorize your account by clicking the 'Authorize' button and signing in again.";
    public static final String ERROR_INSUFFICIENT_PERMISSIONS = "You don't have sufficient permissions to perform this action in JIRA. Please contact your JIRA administrator to request the necessary permissions.";

    // Issue-Related Errors
    public static final String ERROR_ISSUE_NOT_FOUND = "The specified issue could not be found in JIRA. Please verify the issue key is correct and that you have permission to view this issue.";
    public static final String ERROR_ISSUE_KEY_INVALID = "The issue key format is invalid. Issue keys should follow the format 'PROJECT-123' (e.g., 'DEMO-456'). Please check the key and try again.";
    public static final String ERROR_ISSUE_CREATION_FAILED = "Failed to create the issue in JIRA. Please check that all required fields are filled correctly and that you have permission to create issues in this project.";
    public static final String ERROR_ISSUE_UPDATE_FAILED = "Failed to update the issue in JIRA. Please verify you have edit permissions for this issue and that all field values are valid.";
    public static final String ERROR_ISSUE_DELETE_FAILED = "Failed to delete the issue from JIRA. Please ensure you have delete permissions for this issue and try again.";

    // Project-Related Errors
    public static final String ERROR_PROJECT_NOT_FOUND = "The specified project could not be found in JIRA. Please verify the project key is correct and that you have access to this project.";
    public static final String ERROR_PROJECT_ACCESS_DENIED = "You don't have access to this JIRA project. Please contact your JIRA administrator to request access to the project.";
    public static final String ERROR_PROJECT_KEY_INVALID = "The project key format is invalid. Project keys are typically 2-10 uppercase letters (e.g., 'DEMO', 'PROJ'). Please check the key and try again.";

    // User-Related Errors
    public static final String ERROR_USER_NOT_FOUND = "The specified user could not be found in JIRA. Please verify the username or email address is correct.";
    public static final String ERROR_USER_CREATION_FAILED = "Failed to create the user in JIRA. Please check that all required information is provided and that you have user management permissions.";
    public static final String ERROR_USER_UPDATE_FAILED = "Failed to update the user in JIRA. Please verify you have user management permissions and that all information is valid.";
    public static final String ERROR_USER_DELETE_FAILED = "Failed to delete the user from JIRA. Please ensure you have user management permissions and try again.";
    public static final String ERROR_EMAIL_INVALID = "The email address format is invalid. Please provide a valid email address (e.g., 'user@company.com').";

    // JQL Query Errors
    public static final String ERROR_JQL_INVALID = "The JQL query is invalid. Please check the syntax and ensure all field names, operators, and values are correct. Refer to JIRA's JQL documentation for help.";
    public static final String ERROR_JQL_EXECUTION_FAILED = "Failed to execute the JQL query. Please verify the query syntax and that you have permission to access the requested data.";
    public static final String ERROR_JQL_TIMEOUT = "The JQL query took too long to execute. Please try simplifying the query or adding more specific filters to reduce the result set.";

    // Field Validation Errors
    public static final String ERROR_REQUIRED_FIELD_MISSING = "Required field is missing. Please provide a value for this field to continue.";
    public static final String ERROR_FIELD_VALUE_INVALID = "The field value is invalid. Please check the format and requirements for this field.";
    public static final String ERROR_FIELD_TOO_LONG = "The field value is too long. Please shorten the text to meet the maximum length requirement.";
    public static final String ERROR_FIELD_TOO_SHORT = "The field value is too short. Please provide more detailed information.";

    // Status and Workflow Errors
    public static final String ERROR_STATUS_INVALID = "The specified status is not valid for this issue type or project. Please select a valid status from the available options.";
    public static final String ERROR_WORKFLOW_TRANSITION_INVALID = "This status transition is not allowed by the workflow. Please check the available transitions for this issue.";
    public static final String ERROR_STATUS_UPDATE_FAILED = "Failed to update the issue status. Please verify you have permission to transition this issue and that the status change is valid.";

    // File and Attachment Errors
    public static final String ERROR_FILE_UPLOAD_FAILED = "Failed to upload the file to JIRA. Please check the file size, format, and your permissions, then try again.";
    public static final String ERROR_FILE_TOO_LARGE = "The file is too large to upload. Please reduce the file size or contact your administrator about file size limits.";
    public static final String ERROR_FILE_FORMAT_UNSUPPORTED = "The file format is not supported. Please use a supported file format and try again.";
    public static final String ERROR_ATTACHMENT_NOT_FOUND = "The specified attachment could not be found. It may have been deleted or you may not have permission to access it.";

    // Network and System Errors
    public static final String ERROR_NETWORK_CONNECTION = "Unable to connect to JIRA. Please check your internet connection and try again. If the problem persists, contact your system administrator.";
    public static final String ERROR_JIRA_UNAVAILABLE = "JIRA is currently unavailable. Please try again in a few minutes. If the issue continues, contact your JIRA administrator.";
    public static final String ERROR_REQUEST_TIMEOUT = "The request to JIRA timed out. Please try again. If the problem persists, contact your system administrator.";
    public static final String ERROR_RATE_LIMIT_EXCEEDED = "Too many requests have been made to JIRA. Please wait a moment before trying again.";

    // Generic Errors
    public static final String ERROR_UNEXPECTED = "An unexpected error occurred. Please try again. If the problem persists, contact your system administrator with the details of what you were trying to do.";
    public static final String ERROR_OPERATION_FAILED = "The operation could not be completed. Please check your inputs and permissions, then try again.";
    public static final String ERROR_DATA_PROCESSING = "There was an error processing the data. Please verify your inputs are correct and try again.";
}
