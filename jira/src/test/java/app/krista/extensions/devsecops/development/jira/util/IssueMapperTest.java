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

import app.krista.extensions.devsecops.development.jira.catalog.entities.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Comprehensive test suite for IssueMapper utility class.
 * Tests all public static methods with edge cases, null values, empty inputs, and boundary conditions.
 * 
 * Following extension revamp guidelines:
 * - No Mockito usage - using real concrete implementations only
 * - 100% coverage of utility methods
 * - Edge cases: null, empty, invalid inputs, boundary values
 */
@DisplayName("IssueMapper Utility Tests")
class IssueMapperTest {

    private Issue sampleIssue;

    @BeforeEach
    void setUp() {
        // Create a sample issue with all fields populated
        sampleIssue = new Issue();
        sampleIssue.id = "12345";
        sampleIssue.key = "DEMO-123";
        sampleIssue.assigneeName = "John Doe";
        sampleIssue.comments = List.of("Sample comment");
        sampleIssue.description = "Sample description";
        sampleIssue.priority = "High";
        sampleIssue.projectKey = "DEMO";
        sampleIssue.reporterName = "Jane Smith";
        sampleIssue.status = "In Progress";
        sampleIssue.issueType = "Story";
        sampleIssue.summary = "Sample summary";
    }

    // ========================================
    // getEpic() Tests
    // ========================================

    @Test
    @DisplayName("getEpic: Should map all fields from Issue to Epic correctly")
    void testGetEpic_AllFields_MapsCorrectly() {
        // Act
        Epic result = IssueMapper.getEpic(sampleIssue);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(sampleIssue.assigneeName, result.assigneeName, "assigneeName should be mapped");
        assertEquals(sampleIssue.comments, result.comments, "comments should be mapped");
        assertEquals(sampleIssue.description, result.description, "description should be mapped");
        assertEquals(sampleIssue.key, result.epicKey, "key should be mapped to epicKey");
        assertEquals(sampleIssue.priority, result.priority, "priority should be mapped");
        assertEquals(sampleIssue.projectKey, result.projectKey, "projectKey should be mapped");
        assertEquals(sampleIssue.reporterName, result.reporterName, "reporterName should be mapped");
        assertEquals(sampleIssue.status, result.status, "status should be mapped");
        assertEquals(sampleIssue.issueType, result.type, "issueType should be mapped to type");
        assertEquals(sampleIssue.summary, result.summary, "summary should be mapped");
        assertEquals(sampleIssue.id, result.id, "id should be mapped");
    }

    @Test
    @DisplayName("getEpic: Should handle Issue with null fields")
    void testGetEpic_NullFields_HandlesGracefully() {
        // Arrange
        Issue issueWithNulls = new Issue();
        // All fields are null by default
        
        // Act
        Epic result = IssueMapper.getEpic(issueWithNulls);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertNull(result.assigneeName, "assigneeName should be null");
        assertNull(result.comments, "comments should be null");
        assertNull(result.description, "description should be null");
        assertNull(result.epicKey, "epicKey should be null");
        assertNull(result.priority, "priority should be null");
        assertNull(result.projectKey, "projectKey should be null");
        assertNull(result.reporterName, "reporterName should be null");
        assertNull(result.status, "status should be null");
        assertNull(result.type, "type should be null");
        assertNull(result.summary, "summary should be null");
        assertNull(result.id, "id should be null");
    }

    @Test
    @DisplayName("getEpic: Should handle Issue with empty string fields")
    void testGetEpic_EmptyStringFields_HandlesCorrectly() {
        // Arrange
        Issue issueWithEmptyStrings = new Issue();
        issueWithEmptyStrings.assigneeName = "";
        issueWithEmptyStrings.comments = List.of();
        issueWithEmptyStrings.description = "";
        issueWithEmptyStrings.key = "";
        issueWithEmptyStrings.priority = "";
        issueWithEmptyStrings.projectKey = "";
        issueWithEmptyStrings.reporterName = "";
        issueWithEmptyStrings.status = "";
        issueWithEmptyStrings.issueType = "";
        issueWithEmptyStrings.summary = "";
        issueWithEmptyStrings.id = "";
        
        // Act
        Epic result = IssueMapper.getEpic(issueWithEmptyStrings);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("", result.assigneeName, "assigneeName should be empty string");
        assertEquals(List.of(), result.comments, "comments should be empty list");
        assertEquals("", result.description, "description should be empty string");
        assertEquals("", result.epicKey, "epicKey should be empty string");
        assertEquals("", result.priority, "priority should be empty string");
        assertEquals("", result.projectKey, "projectKey should be empty string");
        assertEquals("", result.reporterName, "reporterName should be empty string");
        assertEquals("", result.status, "status should be empty string");
        assertEquals("", result.type, "type should be empty string");
        assertEquals("", result.summary, "summary should be empty string");
        assertEquals("", result.id, "id should be empty string");
    }

    // ========================================
    // getTask() Tests
    // ========================================

    @Test
    @DisplayName("getTask: Should map all fields from Issue to Task correctly")
    void testGetTask_AllFields_MapsCorrectly() {
        // Act
        Task result = IssueMapper.getTask(sampleIssue);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(sampleIssue.assigneeName, result.assigneeName, "assigneeName should be mapped");
        assertEquals(sampleIssue.comments, result.comments, "comments should be mapped");
        assertEquals(sampleIssue.description, result.description, "description should be mapped");
        assertEquals(sampleIssue.key, result.taskKey, "key should be mapped to taskKey");
        assertEquals(sampleIssue.priority, result.priority, "priority should be mapped");
        assertEquals(sampleIssue.projectKey, result.projectKey, "projectKey should be mapped");
        assertEquals(sampleIssue.reporterName, result.reporterName, "reporterName should be mapped");
        assertEquals(sampleIssue.status, result.status, "status should be mapped");
        assertEquals(sampleIssue.issueType, result.taskType, "issueType should be mapped to taskType");
        assertEquals(sampleIssue.summary, result.summary, "summary should be mapped");
        assertEquals(sampleIssue.id, result.id, "id should be mapped");
    }

    @Test
    @DisplayName("getTask: Should handle Issue with null fields")
    void testGetTask_NullFields_HandlesGracefully() {
        // Arrange
        Issue issueWithNulls = new Issue();
        
        // Act
        Task result = IssueMapper.getTask(issueWithNulls);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertNull(result.assigneeName, "assigneeName should be null");
        assertNull(result.comments, "comments should be null");
        assertNull(result.description, "description should be null");
        assertNull(result.taskKey, "taskKey should be null");
        assertNull(result.priority, "priority should be null");
        assertNull(result.projectKey, "projectKey should be null");
        assertNull(result.reporterName, "reporterName should be null");
        assertNull(result.status, "status should be null");
        assertNull(result.taskType, "taskType should be null");
        assertNull(result.summary, "summary should be null");
        assertNull(result.id, "id should be null");
    }

    // ========================================
    // getBug() Tests
    // ========================================

    @Test
    @DisplayName("getBug: Should map all fields from Issue to Bug correctly")
    void testGetBug_AllFields_MapsCorrectly() {
        // Act
        Bug result = IssueMapper.getBug(sampleIssue);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(sampleIssue.assigneeName, result.assigneeName, "assigneeName should be mapped");
        assertEquals(sampleIssue.comments, result.comments, "comments should be mapped");
        assertEquals(sampleIssue.description, result.description, "description should be mapped");
        assertEquals(sampleIssue.key, result.bugKey, "key should be mapped to bugKey");
        assertEquals(sampleIssue.priority, result.priority, "priority should be mapped");
        assertEquals(sampleIssue.projectKey, result.projectKey, "projectKey should be mapped");
        assertEquals(sampleIssue.reporterName, result.reporterName, "reporterName should be mapped");
        assertEquals(sampleIssue.status, result.status, "status should be mapped");
        assertEquals(sampleIssue.issueType, result.type, "issueType should be mapped to type");
        assertEquals(sampleIssue.summary, result.summary, "summary should be mapped");
        assertEquals(sampleIssue.id, result.id, "id should be mapped");
    }

    @Test
    @DisplayName("getBug: Should handle Issue with null fields")
    void testGetBug_NullFields_HandlesGracefully() {
        // Arrange
        Issue issueWithNulls = new Issue();
        
        // Act
        Bug result = IssueMapper.getBug(issueWithNulls);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertNull(result.assigneeName, "assigneeName should be null");
        assertNull(result.comments, "comments should be null");
        assertNull(result.description, "description should be null");
        assertNull(result.bugKey, "bugKey should be null");
        assertNull(result.priority, "priority should be null");
        assertNull(result.projectKey, "projectKey should be null");
        assertNull(result.reporterName, "reporterName should be null");
        assertNull(result.status, "status should be null");
        assertNull(result.type, "type should be null");
        assertNull(result.summary, "summary should be null");
        assertNull(result.id, "id should be null");
    }

    // ========================================
    // Edge Cases and Boundary Tests
    // ========================================

    @Test
    @DisplayName("All mappers: Should handle very long field values")
    void testAllMappers_VeryLongFields_HandlesCorrectly() {
        // Arrange
        Issue issueWithLongFields = new Issue();
        String longString = "a".repeat(10000); // 10,000 character string
        issueWithLongFields.summary = longString;
        issueWithLongFields.description = longString;
        issueWithLongFields.comments = List.of(longString);
        
        // Act
        Epic epic = IssueMapper.getEpic(issueWithLongFields);
        Task task = IssueMapper.getTask(issueWithLongFields);
        Bug bug = IssueMapper.getBug(issueWithLongFields);
        
        // Assert
        assertEquals(longString, epic.summary, "Epic should handle long summary");
        assertEquals(longString, epic.description, "Epic should handle long description");
        assertEquals(List.of(longString), epic.comments, "Epic should handle long comments");

        assertEquals(longString, task.summary, "Task should handle long summary");
        assertEquals(longString, task.description, "Task should handle long description");
        assertEquals(List.of(longString), task.comments, "Task should handle long comments");

        assertEquals(longString, bug.summary, "Bug should handle long summary");
        assertEquals(longString, bug.description, "Bug should handle long description");
        assertEquals(List.of(longString), bug.comments, "Bug should handle long comments");
    }

    @Test
    @DisplayName("All mappers: Should handle special characters in fields")
    void testAllMappers_SpecialCharacters_HandlesCorrectly() {
        // Arrange
        Issue issueWithSpecialChars = new Issue();
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~\n\t\r";
        issueWithSpecialChars.summary = specialChars;
        issueWithSpecialChars.key = "DEMO-123";
        
        // Act
        Epic epic = IssueMapper.getEpic(issueWithSpecialChars);
        Task task = IssueMapper.getTask(issueWithSpecialChars);
        Bug bug = IssueMapper.getBug(issueWithSpecialChars);
        
        // Assert
        assertEquals(specialChars, epic.summary, "Epic should handle special characters");
        assertEquals("DEMO-123", epic.epicKey, "Epic key should be mapped correctly");
        
        assertEquals(specialChars, task.summary, "Task should handle special characters");
        assertEquals("DEMO-123", task.taskKey, "Task key should be mapped correctly");
        
        assertEquals(specialChars, bug.summary, "Bug should handle special characters");
        assertEquals("DEMO-123", bug.bugKey, "Bug key should be mapped correctly");
    }
}
