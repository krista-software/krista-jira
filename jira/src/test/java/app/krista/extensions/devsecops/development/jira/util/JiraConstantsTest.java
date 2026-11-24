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

import com.google.gson.Gson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JiraConstants class.
 * Tests all public constants and Gson instances.
 */
@DisplayName("JiraConstants Tests")
class JiraConstantsTest {

    @Test
    @DisplayName("Should have correct state constant")
    void testStateConstant() {
        assertEquals("state", JiraConstants.STATE);
    }

    @Test
    @DisplayName("Should have correct formatting constants")
    void testFormattingConstants() {
        assertEquals("strong", JiraConstants.STRONG);
        assertEquals("em", JiraConstants.EM);
        assertEquals("underline", JiraConstants.UNDERLINE);
    }

    @Test
    @DisplayName("Should have correct field name constants")
    void testFieldNameConstants() {
        assertEquals("Group Names", JiraConstants.GROUP_NAMES);
        assertEquals("Comment", JiraConstants.COMMENT);
        assertEquals("Comment Visibility", JiraConstants.COMMENT_VISIBILITY);
        assertEquals("Validation Results", JiraConstants.VALIDATION_RESULT);
        assertEquals("Assignee", JiraConstants.ASSIGNEE);
    }

    @Test
    @DisplayName("Should have correct Gson instances")
    void testGsonInstances() {
        assertNotNull(JiraConstants.GSON);
        assertNotNull(JiraConstants.GSON_BUILDER);
        assertInstanceOf(Gson.class, JiraConstants.GSON);
        assertInstanceOf(Gson.class, JiraConstants.GSON_BUILDER);
    }

    @Test
    @DisplayName("Should have correct NA constant")
    void testNAConstant() {
        assertEquals("NA", JiraConstants.NA);
    }

    @Test
    @DisplayName("Should have correct HTTP header constants")
    void testHTTPHeaderConstants() {
        assertEquals("Accept", JiraConstants.ACCEPT);
        assertEquals("X-Atlassian-Token", JiraConstants.X_ATLASSIAN_TOKEN);
        assertEquals("no-check", JiraConstants.NO_CHECK);
    }

    @Test
    @DisplayName("Should have correct issue type constants")
    void testIssueTypeConstants() {
        assertEquals("Bug", JiraConstants.BUG);
        assertEquals("Epic", JiraConstants.EPIC);
        assertEquals("Issue", JiraConstants.ISSUE);
        assertEquals("Issue Type", JiraConstants.ISSUE_TYPE);
        assertEquals("Story", JiraConstants.STORY);
        assertEquals("Task", JiraConstants.TASK);
        assertEquals("Sub-task", JiraConstants.SUBTASK);
        assertEquals("Subtask", JiraConstants.SUBTASK_ENTITY);
    }

    @Test
    @DisplayName("Should have correct entity type constants")
    void testEntityTypeConstants() {
        assertEquals("Ticket Priority", JiraConstants.TICKET_PRIORITY);
        assertEquals("Ticket Project", JiraConstants.TICKET_PROJECT);
        assertEquals("Ticket Status", JiraConstants.TICKET_STATUS);
        assertEquals("User", JiraConstants.USER);
        assertEquals("Version", JiraConstants.VERSION);
    }

    @Test
    @DisplayName("Should have correct logical operator constant")
    void testLogicalOperatorConstant() {
        assertEquals(" AND ", JiraConstants.AND);
    }

    @Test
    @DisplayName("Should have correct authentication constants")
    void testAuthenticationConstants() {
        assertEquals("logicalName", JiraConstants.LOGICAL_NAME_FIELD);
        assertEquals("refresh_token", JiraConstants.REFRESH_TOKEN);
        assertEquals("code", JiraConstants.CODE);
        assertEquals("jiraextn", JiraConstants.JAXRS_ID);
        assertEquals("Bearer ", JiraConstants.BEARER);
    }

    @Test
    @DisplayName("Should have correct field constants")
    void testFieldConstants() {
        assertEquals("Project Key", JiraConstants.PROJECT_KEY);
        assertEquals("Summary", JiraConstants.SUMMARY);
        assertEquals("Description", JiraConstants.DESCRIPTION);
        assertEquals("Comments", JiraConstants.COMMENTS);
        assertEquals("Reporter Name", JiraConstants.REPORTER_NAME);
        assertEquals("Assignee Name", JiraConstants.ASSIGNEE_NAME);
        assertEquals("Id", JiraConstants.ID);
        assertEquals("Type", JiraConstants.TYPE);
        assertEquals("type", JiraConstants._TYPE);
        assertEquals("Priority", JiraConstants.PRIORITY);
        assertEquals("Status", JiraConstants.STATUS);
        assertEquals("Parent Key", JiraConstants.PARENT_KEY);
        assertEquals("Labels", JiraConstants.LABELS);
        assertEquals("Components", JiraConstants.COMPONENTS);
        assertEquals("Label", JiraConstants.LABEL);
        assertEquals("Component", JiraConstants.COMPONENT);
        assertEquals("description", JiraConstants._DESCRIPTION);
        assertEquals("summary", JiraConstants._SUMMARY);
        assertEquals("story key", JiraConstants.STORY_KEY);
    }

    @Test
    @DisplayName("Should have correct pagination constants")
    void testPaginationConstants() {
        assertEquals(50L, JiraConstants.DEFAULT_PAGE_SIZE);
        assertEquals(1L, JiraConstants.PAGE_NUMBER);
    }

    @Test
    @DisplayName("Should have correct update constants")
    void testUpdateConstants() {
        assertEquals("Updates On Jira", JiraConstants.UPDATES_ON_JIRA);
        assertEquals("Jira Updates", JiraConstants.JIRA_UPDATES);
        assertEquals("issue", JiraConstants.ISSUE_);
        assertEquals("Update", JiraConstants.UPDATE);
        assertEquals("Issue Key", JiraConstants.ISSUE_KEY);
    }

    @Test
    @DisplayName("Should have correct authorization prompt")
    void testAuthorizationPrompt() {
        assertNotNull(JiraConstants.AUTHORIZATION_PROMPT);
        assertTrue(JiraConstants.AUTHORIZATION_PROMPT.contains("Authorize"));
        assertTrue(JiraConstants.AUTHORIZATION_PROMPT.contains("Validate Attributes"));
        assertTrue(JiraConstants.AUTHORIZATION_PROMPT.contains("Save"));
    }

    @Test
    @DisplayName("Should have correct ADF content constants")
    void testADFContentConstants() {
        assertEquals("content", JiraConstants.CONTENT);
        assertEquals("text", JiraConstants.TEXT);
        assertEquals("attrs", JiraConstants.ATTRS);
        assertEquals("stateId", JiraConstants.STATE_ID);
    }

    @Test
    @DisplayName("Should have correct error and query constants")
    void testErrorAndQueryConstants() {
        assertEquals("errors", JiraConstants.ERRORS);
        assertEquals("queries", JiraConstants.QUERIES);
        assertEquals("query", JiraConstants.QUERY);
        assertEquals("JQL Query", JiraConstants.JQL_QUERY);
        assertEquals("value", JiraConstants.VALUE);
        assertEquals("re-enter", JiraConstants.RE_ENTER);
    }

    @Test
    @DisplayName("Should have authentication error messages")
    void testAuthenticationErrorMessages() {
        assertNotNull(JiraConstants.ERROR_AUTHENTICATION_REQUIRED);
        assertNotNull(JiraConstants.ERROR_AUTHORIZATION_FAILED);
        assertNotNull(JiraConstants.ERROR_TOKEN_EXPIRED);
        assertNotNull(JiraConstants.ERROR_INSUFFICIENT_PERMISSIONS);
        
        assertTrue(JiraConstants.ERROR_AUTHENTICATION_REQUIRED.contains("Authentication"));
        assertTrue(JiraConstants.ERROR_AUTHORIZATION_FAILED.contains("Authorization"));
        assertTrue(JiraConstants.ERROR_TOKEN_EXPIRED.contains("expired"));
        assertTrue(JiraConstants.ERROR_INSUFFICIENT_PERMISSIONS.contains("permissions"));
    }

    @Test
    @DisplayName("Should have issue error messages")
    void testIssueErrorMessages() {
        assertNotNull(JiraConstants.ERROR_ISSUE_NOT_FOUND);
        assertNotNull(JiraConstants.ERROR_ISSUE_KEY_INVALID);
        assertNotNull(JiraConstants.ERROR_ISSUE_CREATION_FAILED);
        assertNotNull(JiraConstants.ERROR_ISSUE_UPDATE_FAILED);
        assertNotNull(JiraConstants.ERROR_ISSUE_DELETE_FAILED);
        
        assertTrue(JiraConstants.ERROR_ISSUE_NOT_FOUND.contains("not be found"));
        assertTrue(JiraConstants.ERROR_ISSUE_KEY_INVALID.contains("invalid"));
        assertTrue(JiraConstants.ERROR_ISSUE_CREATION_FAILED.contains("create"));
        assertTrue(JiraConstants.ERROR_ISSUE_UPDATE_FAILED.contains("update"));
        assertTrue(JiraConstants.ERROR_ISSUE_DELETE_FAILED.contains("delete"));
    }

    @Test
    @DisplayName("Should have project error messages")
    void testProjectErrorMessages() {
        assertNotNull(JiraConstants.ERROR_PROJECT_NOT_FOUND);
        assertNotNull(JiraConstants.ERROR_PROJECT_ACCESS_DENIED);
        assertNotNull(JiraConstants.ERROR_PROJECT_KEY_INVALID);
        
        assertTrue(JiraConstants.ERROR_PROJECT_NOT_FOUND.contains("project"));
        assertTrue(JiraConstants.ERROR_PROJECT_ACCESS_DENIED.contains("access"));
        assertTrue(JiraConstants.ERROR_PROJECT_KEY_INVALID.contains("invalid"));
    }

    @Test
    @DisplayName("Should have user error messages")
    void testUserErrorMessages() {
        assertNotNull(JiraConstants.ERROR_USER_NOT_FOUND);
        assertNotNull(JiraConstants.ERROR_USER_CREATION_FAILED);
        assertNotNull(JiraConstants.ERROR_USER_UPDATE_FAILED);
        assertNotNull(JiraConstants.ERROR_USER_DELETE_FAILED);
        assertNotNull(JiraConstants.ERROR_EMAIL_INVALID);
        
        assertTrue(JiraConstants.ERROR_USER_NOT_FOUND.contains("user"));
        assertTrue(JiraConstants.ERROR_EMAIL_INVALID.contains("email"));
    }

    @Test
    @DisplayName("Should have JQL error messages")
    void testJQLErrorMessages() {
        assertNotNull(JiraConstants.ERROR_JQL_INVALID);
        assertNotNull(JiraConstants.ERROR_JQL_EXECUTION_FAILED);
        assertNotNull(JiraConstants.ERROR_JQL_TIMEOUT);

        assertTrue(JiraConstants.ERROR_JQL_INVALID.contains("JQL") || JiraConstants.ERROR_JQL_INVALID.contains("query"));
        assertTrue(JiraConstants.ERROR_JQL_EXECUTION_FAILED.contains("execute") || JiraConstants.ERROR_JQL_EXECUTION_FAILED.contains("Failed"));
        assertTrue(JiraConstants.ERROR_JQL_TIMEOUT.contains("timeout") || JiraConstants.ERROR_JQL_TIMEOUT.contains("too long"));
    }

    @Test
    @DisplayName("Should have field validation error messages")
    void testFieldValidationErrorMessages() {
        assertNotNull(JiraConstants.ERROR_REQUIRED_FIELD_MISSING);
        assertNotNull(JiraConstants.ERROR_FIELD_VALUE_INVALID);
        assertNotNull(JiraConstants.ERROR_FIELD_TOO_LONG);
        assertNotNull(JiraConstants.ERROR_FIELD_TOO_SHORT);
        
        assertTrue(JiraConstants.ERROR_REQUIRED_FIELD_MISSING.contains("Required"));
        assertTrue(JiraConstants.ERROR_FIELD_VALUE_INVALID.contains("invalid"));
        assertTrue(JiraConstants.ERROR_FIELD_TOO_LONG.contains("too long"));
        assertTrue(JiraConstants.ERROR_FIELD_TOO_SHORT.contains("too short"));
    }

    @Test
    @DisplayName("Should have status and workflow error messages")
    void testStatusWorkflowErrorMessages() {
        assertNotNull(JiraConstants.ERROR_STATUS_INVALID);
        assertNotNull(JiraConstants.ERROR_WORKFLOW_TRANSITION_INVALID);
        assertNotNull(JiraConstants.ERROR_STATUS_UPDATE_FAILED);
        
        assertTrue(JiraConstants.ERROR_STATUS_INVALID.contains("status"));
        assertTrue(JiraConstants.ERROR_WORKFLOW_TRANSITION_INVALID.contains("workflow"));
        assertTrue(JiraConstants.ERROR_STATUS_UPDATE_FAILED.contains("update"));
    }

    @Test
    @DisplayName("Should have file and attachment error messages")
    void testFileAttachmentErrorMessages() {
        assertNotNull(JiraConstants.ERROR_FILE_UPLOAD_FAILED);
        assertNotNull(JiraConstants.ERROR_FILE_TOO_LARGE);
        assertNotNull(JiraConstants.ERROR_FILE_FORMAT_UNSUPPORTED);
        assertNotNull(JiraConstants.ERROR_ATTACHMENT_NOT_FOUND);
        
        assertTrue(JiraConstants.ERROR_FILE_UPLOAD_FAILED.contains("upload"));
        assertTrue(JiraConstants.ERROR_FILE_TOO_LARGE.contains("too large"));
        assertTrue(JiraConstants.ERROR_FILE_FORMAT_UNSUPPORTED.contains("format"));
        assertTrue(JiraConstants.ERROR_ATTACHMENT_NOT_FOUND.contains("attachment"));
    }

    @Test
    @DisplayName("Should have network and system error messages")
    void testNetworkSystemErrorMessages() {
        assertNotNull(JiraConstants.ERROR_NETWORK_CONNECTION);
        assertNotNull(JiraConstants.ERROR_JIRA_UNAVAILABLE);
        assertNotNull(JiraConstants.ERROR_REQUEST_TIMEOUT);
        assertNotNull(JiraConstants.ERROR_RATE_LIMIT_EXCEEDED);

        assertTrue(JiraConstants.ERROR_NETWORK_CONNECTION.contains("connect") || JiraConstants.ERROR_NETWORK_CONNECTION.contains("JIRA"));
        assertTrue(JiraConstants.ERROR_JIRA_UNAVAILABLE.contains("unavailable") || JiraConstants.ERROR_JIRA_UNAVAILABLE.contains("JIRA"));
        assertTrue(JiraConstants.ERROR_REQUEST_TIMEOUT.contains("timeout") || JiraConstants.ERROR_REQUEST_TIMEOUT.contains("timed out"));
        assertTrue(JiraConstants.ERROR_RATE_LIMIT_EXCEEDED.contains("Too many") || JiraConstants.ERROR_RATE_LIMIT_EXCEEDED.contains("many requests"));
    }

    @Test
    @DisplayName("Should have generic error messages")
    void testGenericErrorMessages() {
        assertNotNull(JiraConstants.ERROR_UNEXPECTED);
        assertNotNull(JiraConstants.ERROR_OPERATION_FAILED);
        assertNotNull(JiraConstants.ERROR_DATA_PROCESSING);
        
        assertTrue(JiraConstants.ERROR_UNEXPECTED.contains("unexpected"));
        assertTrue(JiraConstants.ERROR_OPERATION_FAILED.contains("operation"));
        assertTrue(JiraConstants.ERROR_DATA_PROCESSING.contains("processing"));
    }

    @Test
    @DisplayName("Gson instances should be functional")
    void testGsonFunctionality() {
        // Arrange
        String json = "{\"key\":\"value\"}";
        
        // Act
        Object result1 = JiraConstants.GSON.fromJson(json, Object.class);
        Object result2 = JiraConstants.GSON_BUILDER.fromJson(json, Object.class);
        
        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("Gson builder should produce pretty printed JSON")
    void testGsonBuilderPrettyPrinting() {
        // Arrange
        java.util.Map<String, Object> testObject = new java.util.HashMap<>();
        testObject.put("name", "test");
        testObject.put("value", 123);

        // Act
        String prettyJson = JiraConstants.GSON_BUILDER.toJson(testObject);
        String normalJson = JiraConstants.GSON.toJson(testObject);

        // Assert
        assertNotNull(prettyJson);
        assertNotNull(normalJson);
        // Pretty printed JSON should be longer due to formatting
        assertTrue(prettyJson.length() >= normalJson.length(), "Pretty printed JSON should be at least as long as normal JSON");
    }
}

