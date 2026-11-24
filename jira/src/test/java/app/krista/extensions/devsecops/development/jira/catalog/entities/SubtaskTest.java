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
 * Comprehensive test suite for Subtask entity class.
 */
@DisplayName("Subtask Entity Tests")
class SubtaskTest {

    private Subtask subtask;

    @BeforeEach
    void setUp() {
        subtask = new Subtask();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testAllFieldsSetAndGet() {
        // Arrange
        List<String> comments = Arrays.asList("Subtask comment 1");
        
        // Act
        subtask.summary = "Implement unit tests";
        subtask.reporterName = "Developer";
        subtask.assigneeName = "QA Engineer";
        subtask.taskType = "Sub-task";
        subtask.key = "SUBTASK-50";
        subtask.id = "50001";
        subtask.projectKey = "PROJ";
        subtask.priority = "Medium";
        subtask.status = "In Progress";
        subtask.description = "Write unit tests for authentication";
        subtask.comments = comments;

        // Assert
        assertEquals("Implement unit tests", subtask.summary);
        assertEquals("Developer", subtask.reporterName);
        assertEquals("QA Engineer", subtask.assigneeName);
        assertEquals("Sub-task", subtask.taskType);
        assertEquals("SUBTASK-50", subtask.key);
        assertEquals("50001", subtask.id);
        assertEquals("PROJ", subtask.projectKey);
        assertEquals("Medium", subtask.priority);
        assertEquals("In Progress", subtask.status);
        assertEquals("Write unit tests for authentication", subtask.description);
        assertEquals(comments, subtask.comments);
    }

    @Test
    @DisplayName("toFields() should return correct map with all fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        List<String> comments = Arrays.asList("Comment");
        subtask.summary = "Test Subtask";
        subtask.reporterName = "Reporter";
        subtask.assigneeName = "Assignee";
        subtask.taskType = "Sub-task";
        subtask.key = "SUB-001";
        subtask.id = "123";
        subtask.projectKey = "TEST";
        subtask.priority = "Low";
        subtask.status = "Done";
        subtask.description = "Subtask Description";
        subtask.comments = comments;
        subtask.parentKey = "PARENT-001";

        // Act
        Map<String, Object> fieldMap = subtask.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(14, fieldMap.size());
        assertEquals("Test Subtask", fieldMap.get("Summary"));
        assertEquals(comments, fieldMap.get("Comments"));
        assertEquals("Reporter", fieldMap.get("Reporter Name"));
        assertEquals("Assignee", fieldMap.get("Assignee Name"));
        assertEquals("SUB-001", fieldMap.get("Key"));
        assertEquals("123", fieldMap.get("Id"));
        assertEquals("TEST", fieldMap.get("Project Key"));
        assertEquals("Subtask Description", fieldMap.get("Description"));
        assertEquals("Sub-task", fieldMap.get("Task Type"));
        assertEquals("Low", fieldMap.get("Priority"));
        assertEquals("Done", fieldMap.get("Status"));
        assertEquals("PARENT-001", fieldMap.get("Parent Key"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Act
        Map<String, Object> fieldMap = subtask.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertNull(fieldMap.get("Summary"));
        assertNull(fieldMap.get("Subtask Key"));
    }

    @Test
    @DisplayName("Should create new instance with default null values")
    void testDefaultValues() {
        // Arrange & Act
        Subtask newSubtask = new Subtask();
        
        // Assert
        assertNull(newSubtask.summary);
        assertNull(newSubtask.reporterName);
        assertNull(newSubtask.assigneeName);
        assertNull(newSubtask.taskType);
        assertNull(newSubtask.key);
        assertNull(newSubtask.id);
        assertNull(newSubtask.projectKey);
        assertNull(newSubtask.priority);
        assertNull(newSubtask.status);
        assertNull(newSubtask.description);
        assertNull(newSubtask.comments);
        assertNull(newSubtask.parentKey);
    }
}

