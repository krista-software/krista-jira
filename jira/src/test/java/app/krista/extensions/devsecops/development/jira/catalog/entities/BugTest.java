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

package app.krista.extensions.devsecops.development.jira.catalog.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Bug entity class.
 * Tests all fields, toFields() method, and edge cases.
 * 
 * Following Java development best practices:
 * - No Mockito usage for entity tests
 * - Testing all public methods and field assignments
 * - Edge cases: null values, empty strings, empty lists
 */
@DisplayName("Bug Entity Tests")
class BugTest {

    private Bug bug;

    @BeforeEach
    void setUp() {
        bug = new Bug();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testAllFieldsSetAndGet() {
        // Arrange
        List<String> comments = Arrays.asList("Comment 1", "Comment 2");
        
        // Act
        bug.summary = "Critical bug in login";
        bug.reporterName = "John Doe";
        bug.assigneeName = "Jane Smith";
        bug.type = "Bug";
        bug.bugKey = "PROJ-123";
        bug.id = "10001";
        bug.projectKey = "PROJ";
        bug.priority = "High";
        bug.status = "Open";
        bug.description = "Detailed description of the bug";
        bug.comments = comments;
        
        // Assert
        assertEquals("Critical bug in login", bug.summary);
        assertEquals("John Doe", bug.reporterName);
        assertEquals("Jane Smith", bug.assigneeName);
        assertEquals("Bug", bug.type);
        assertEquals("PROJ-123", bug.bugKey);
        assertEquals("10001", bug.id);
        assertEquals("PROJ", bug.projectKey);
        assertEquals("High", bug.priority);
        assertEquals("Open", bug.status);
        assertEquals("Detailed description of the bug", bug.description);
        assertEquals(comments, bug.comments);
    }

    @Test
    @DisplayName("toFields() should return correct map with all fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        List<String> comments = Arrays.asList("Comment 1", "Comment 2");
        bug.summary = "Test Summary";
        bug.reporterName = "Reporter";
        bug.assigneeName = "Assignee";
        bug.type = "Bug";
        bug.bugKey = "BUG-001";
        bug.id = "123";
        bug.projectKey = "TEST";
        bug.priority = "Medium";
        bug.status = "In Progress";
        bug.description = "Test Description";
        bug.comments = comments;
        
        // Act
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertEquals(14, fieldMap.size());
        assertEquals("Test Summary", fieldMap.get("Summary"));
        assertEquals(comments, fieldMap.get("Comments"));
        assertEquals("Reporter", fieldMap.get("Reporter Name"));
        assertEquals("Assignee", fieldMap.get("Assignee Name"));
        assertEquals("BUG-001", fieldMap.get("Bug Key"));
        assertEquals("123", fieldMap.get("Id"));
        assertNull(fieldMap.get("Parent Key"));
        assertEquals("TEST", fieldMap.get("Project Key"));
        assertEquals("Test Description", fieldMap.get("Description"));
        assertEquals("Bug", fieldMap.get("Type"));
        assertEquals("Medium", fieldMap.get("Priority"));
        assertEquals("In Progress", fieldMap.get("Status"));
        assertNull(fieldMap.get("Created Date"));
        assertNull(fieldMap.get("Last Modified Date"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Arrange - all fields are null by default
        
        // Act
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertEquals(14, fieldMap.size());
        assertNull(fieldMap.get("Summary"));
        assertNull(fieldMap.get("Comments"));
        assertNull(fieldMap.get("Reporter Name"));
        assertNull(fieldMap.get("Assignee Name"));
        assertNull(fieldMap.get("Bug Key"));
        assertNull(fieldMap.get("Id"));
        assertNull(fieldMap.get("Parent Key"));
        assertNull(fieldMap.get("Project Key"));
        assertNull(fieldMap.get("Description"));
        assertNull(fieldMap.get("Type"));
        assertNull(fieldMap.get("Priority"));
        assertNull(fieldMap.get("Status"));
        assertNull(fieldMap.get("Created Date"));
        assertNull(fieldMap.get("Last Modified Date"));
    }

    @Test
    @DisplayName("toFields() should handle empty strings")
    void testToFields_EmptyStrings() {
        // Arrange
        bug.summary = "";
        bug.reporterName = "";
        bug.assigneeName = "";
        bug.type = "";
        bug.bugKey = "";
        bug.id = "";
        bug.projectKey = "";
        bug.priority = "";
        bug.status = "";
        bug.description = "";
        bug.comments = Arrays.asList();
        
        // Act
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertEquals("", fieldMap.get("Summary"));
        assertEquals("", fieldMap.get("Reporter Name"));
        assertEquals("", fieldMap.get("Assignee Name"));
        assertEquals("", fieldMap.get("Bug Key"));
        assertEquals("", fieldMap.get("Id"));
        assertEquals("", fieldMap.get("Project Key"));
        assertEquals("", fieldMap.get("Description"));
        assertEquals("", fieldMap.get("Type"));
        assertEquals("", fieldMap.get("Priority"));
        assertEquals("", fieldMap.get("Status"));
        assertTrue(((List<?>) fieldMap.get("Comments")).isEmpty());
    }

    @Test
    @DisplayName("toFields() should maintain field order")
    void testToFields_FieldOrder() {
        // Arrange
        bug.summary = "Summary";
        bug.comments = Arrays.asList("Comment");
        bug.reporterName = "Reporter";
        bug.assigneeName = "Assignee";
        bug.bugKey = "KEY-1";
        bug.id = "1";
        bug.projectKey = "PROJ";
        bug.description = "Desc";
        bug.type = "Bug";
        bug.priority = "High";
        bug.status = "Open";
        
        // Act
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert - LinkedHashMap maintains insertion order
        List<String> expectedOrder = Arrays.asList(
            "Summary", "Comments", "Reporter Name", "Assignee Name",
            "Bug Key", "Id", "Parent Key", "Project Key", "Description",
            "Type", "Priority", "Status", "Created Date", "Last Modified Date"
        );
        List<String> actualOrder = Arrays.asList(fieldMap.keySet().toArray(new String[0]));
        assertEquals(expectedOrder, actualOrder);
    }

    @Test
    @DisplayName("Should handle multiple comments")
    void testMultipleComments() {
        // Arrange
        List<String> comments = Arrays.asList(
            "First comment",
            "Second comment",
            "Third comment with special chars: @#$%"
        );
        
        // Act
        bug.comments = comments;
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert
        assertEquals(comments, bug.comments);
        assertEquals(comments, fieldMap.get("Comments"));
        assertEquals(3, ((List<?>) fieldMap.get("Comments")).size());
    }

    @Test
    @DisplayName("Should handle special characters in fields")
    void testSpecialCharactersInFields() {
        // Arrange
        bug.summary = "Bug with special chars: @#$%^&*()";
        bug.description = "Description with <html> tags & symbols";
        bug.bugKey = "PROJ-123";
        
        // Act
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert
        assertEquals("Bug with special chars: @#$%^&*()", fieldMap.get("Summary"));
        assertEquals("Description with <html> tags & symbols", fieldMap.get("Description"));
    }

    @Test
    @DisplayName("Should handle long text values")
    void testLongTextValues() {
        // Arrange
        String longSummary = "A".repeat(1000);
        String longDescription = "B".repeat(5000);
        
        // Act
        bug.summary = longSummary;
        bug.description = longDescription;
        Map<String, Object> fieldMap = bug.toFields();
        
        // Assert
        assertEquals(longSummary, bug.summary);
        assertEquals(longDescription, bug.description);
        assertEquals(longSummary, fieldMap.get("Summary"));
        assertEquals(longDescription, fieldMap.get("Description"));
    }

    @Test
    @DisplayName("Should create new instance with default null values")
    void testDefaultValues() {
        // Arrange & Act
        Bug newBug = new Bug();
        
        // Assert
        assertNull(newBug.summary);
        assertNull(newBug.reporterName);
        assertNull(newBug.assigneeName);
        assertNull(newBug.type);
        assertNull(newBug.bugKey);
        assertNull(newBug.id);
        assertNull(newBug.projectKey);
        assertNull(newBug.priority);
        assertNull(newBug.status);
        assertNull(newBug.description);
        assertNull(newBug.comments);
    }
}

