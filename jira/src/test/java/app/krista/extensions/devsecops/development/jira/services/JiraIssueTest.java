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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JiraIssue class.
 * Tests Jira issue creation and conversion to Issue entity.
 */
@DisplayName("JiraIssue Tests")
class JiraIssueTest {

    @Test
    @DisplayName("Should create JiraIssue with all fields")
    void testConstructor_AllFields() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Issue");
        
        JsonObject renderedFields = new JsonObject();

        // Act
        JiraIssue jiraIssue = new JiraIssue("10001", "TEST-123", fields, renderedFields);

        // Assert
        assertEquals("10001", jiraIssue.id);
        assertEquals("TEST-123", jiraIssue.key);
        assertNotNull(jiraIssue.fields);
        assertNotNull(jiraIssue.renderedFields);
    }

    @Test
    @DisplayName("Should create JiraIssue from JsonObject with all fields")
    void testCreate_AllFields() {
        // Arrange
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "10002");
        issueObject.addProperty("key", "PROJ-456");
        
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Sample Issue");
        issueObject.add("fields", fields);
        
        JsonObject renderedFields = new JsonObject();
        issueObject.add("renderedFields", renderedFields);

        // Act
        JiraIssue jiraIssue = JiraIssue.create(issueObject);

        // Assert
        assertNotNull(jiraIssue);
        assertEquals("10002", jiraIssue.id);
        assertEquals("PROJ-456", jiraIssue.key);
        assertNotNull(jiraIssue.fields);
        assertNotNull(jiraIssue.renderedFields);
    }

    @Test
    @DisplayName("Should create JiraIssue from JsonObject with missing fields")
    void testCreate_MissingFields() {
        // Arrange
        JsonObject issueObject = new JsonObject();
        issueObject.addProperty("id", "10003");

        // Act
        JiraIssue jiraIssue = JiraIssue.create(issueObject);

        // Assert
        assertNotNull(jiraIssue);
        assertEquals("10003", jiraIssue.id);
        assertNull(jiraIssue.key);
        assertNull(jiraIssue.fields);
        assertNull(jiraIssue.renderedFields);
    }

    @Test
    @DisplayName("Should get summary from fields")
    void testGetSummary() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "This is a test summary");
        
        JiraIssue jiraIssue = new JiraIssue("10004", "TEST-789", fields, null);

        // Act
        String summary = jiraIssue.getSummary();

        // Assert
        assertEquals("This is a test summary", summary);
    }

    @Test
    @DisplayName("Should return empty string when summary is missing")
    void testGetSummary_Missing() {
        // Arrange
        JsonObject fields = new JsonObject();
        JiraIssue jiraIssue = new JiraIssue("10005", "TEST-999", fields, null);

        // Act
        String summary = jiraIssue.getSummary();

        // Assert
        assertEquals("", summary);
    }

    @Test
    @DisplayName("Should get issue type from fields")
    void testGetIssueType_Static() {
        // Arrange
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Story");
        fields.add("issuetype", issueType);
        issueObject.add("fields", fields);

        // Act
        String type = JiraIssue.getIssueType(issueObject);

        // Assert
        assertEquals("Story", type);
    }

    @Test
    @DisplayName("Should return empty string when issue type is missing")
    void testGetIssueType_Missing() {
        // Arrange
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        issueObject.add("fields", fields);

        // Act
        String type = JiraIssue.getIssueType(issueObject);

        // Assert
        assertEquals("", type);
    }

    @Test
    @DisplayName("Should get subtasks from fields")
    void testGetSubtask() {
        // Arrange
        JsonObject issueObject = new JsonObject();
        JsonObject fields = new JsonObject();
        JsonArray subtasks = new JsonArray();
        
        JsonObject subtask1 = new JsonObject();
        subtask1.addProperty("key", "SUB-1");
        subtasks.add(subtask1);
        
        fields.add("subtasks", subtasks);
        issueObject.add("fields", fields);

        // Act
        JsonArray result = JiraIssue.getSubtask(issueObject);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should convert JiraIssue to Issue entity with basic fields")
    void testGetIssue_BasicFields() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test Summary");
        
        JsonObject priority = new JsonObject();
        priority.addProperty("name", "High");
        fields.add("priority", priority);
        
        JsonObject status = new JsonObject();
        status.addProperty("name", "Open");
        fields.add("status", status);
        
        JsonObject project = new JsonObject();
        project.addProperty("key", "PROJ");
        fields.add("project", project);
        
        JsonObject issueType = new JsonObject();
        issueType.addProperty("name", "Bug");
        fields.add("issuetype", issueType);
        
        JiraIssue jiraIssue = new JiraIssue("10006", "PROJ-100", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertEquals("10006", issue.id);
        assertEquals("PROJ-100", issue.key);
        assertEquals("Test Summary", issue.summary);
        assertEquals("High", issue.priority);
        assertEquals("OPEN", issue.status);
        assertEquals("PROJ", issue.projectKey);
        assertEquals("Bug", issue.issueType);
    }

    @Test
    @DisplayName("Should handle missing assignee")
    void testGetIssue_MissingAssignee() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JiraIssue jiraIssue = new JiraIssue("10007", "TEST-200", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertEquals("NA", issue.assigneeName);
    }

    @Test
    @DisplayName("Should handle missing reporter")
    void testGetIssue_MissingReporter() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JiraIssue jiraIssue = new JiraIssue("10008", "TEST-300", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertEquals("", issue.reporterName);
    }

    @Test
    @DisplayName("Should handle empty comments")
    void testGetIssue_EmptyComments() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JiraIssue jiraIssue = new JiraIssue("10009", "TEST-400", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertNotNull(issue.comments);
        assertEquals(0, issue.comments.size());
    }

    @Test
    @DisplayName("Should handle labels")
    void testGetIssue_WithLabels() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JsonArray labels = new JsonArray();
        labels.add("label1");
        labels.add("label2");
        fields.add("labels", labels);
        
        JiraIssue jiraIssue = new JiraIssue("10010", "TEST-500", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertNotNull(issue.labels);
        assertEquals(2, issue.labels.size());
        assertTrue(issue.labels.contains("label1"));
        assertTrue(issue.labels.contains("label2"));
    }

    @Test
    @DisplayName("Should handle components")
    void testGetIssue_WithComponents() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JsonArray components = new JsonArray();
        JsonObject component1 = new JsonObject();
        component1.addProperty("name", "Backend");
        components.add(component1);
        
        JsonObject component2 = new JsonObject();
        component2.addProperty("name", "Frontend");
        components.add(component2);
        
        fields.add("components", components);
        
        JiraIssue jiraIssue = new JiraIssue("10011", "TEST-600", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertNotNull(issue.components);
        assertEquals(2, issue.components.size());
        assertTrue(issue.components.contains("Backend"));
        assertTrue(issue.components.contains("Frontend"));
    }

    @Test
    @DisplayName("Should handle parent key")
    void testGetIssue_WithParent() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JsonObject parent = new JsonObject();
        parent.addProperty("key", "PARENT-100");
        fields.add("parent", parent);
        
        JiraIssue jiraIssue = new JiraIssue("10012", "TEST-700", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertEquals("PARENT-100", issue.parentKey);
    }

    @Test
    @DisplayName("Should handle missing parent key")
    void testGetIssue_MissingParent() {
        // Arrange
        JsonObject fields = new JsonObject();
        fields.addProperty("summary", "Test");
        
        JiraIssue jiraIssue = new JiraIssue("10013", "TEST-800", fields, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertEquals("NA", issue.parentKey);
    }

    @Test
    @DisplayName("Should handle exception gracefully in getIssue")
    void testGetIssue_ExceptionHandling() {
        // Arrange
        JiraIssue jiraIssue = new JiraIssue("10014", "TEST-900", null, null);

        // Act
        Issue issue = jiraIssue.getIssue();

        // Assert
        assertNotNull(issue);
        assertEquals("10014", issue.id);
        assertEquals("TEST-900", issue.key);
    }
}

