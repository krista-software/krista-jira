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
 * Comprehensive test suite for Task entity class.
 */
@DisplayName("Task Entity Tests")
class TaskTest {

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testAllFieldsSetAndGet() {
        // Arrange
        List<String> comments = Arrays.asList("Task comment 1", "Task comment 2");
        
        // Act
        task.summary = "Implement authentication";
        task.reporterName = "Manager";
        task.assigneeName = "Engineer";
        task.taskType = "Task";
        task.taskKey = "TASK-789";
        task.id = "30001";
        task.projectKey = "PROJ";
        task.priority = "Medium";
        task.status = "To Do";
        task.description = "Implement OAuth authentication";
        task.comments = comments;

        // Assert
        assertEquals("Implement authentication", task.summary);
        assertEquals("Manager", task.reporterName);
        assertEquals("Engineer", task.assigneeName);
        assertEquals("Task", task.taskType);
        assertEquals("TASK-789", task.taskKey);
        assertEquals("30001", task.id);
        assertEquals("PROJ", task.projectKey);
        assertEquals("Medium", task.priority);
        assertEquals("To Do", task.status);
        assertEquals("Implement OAuth authentication", task.description);
        assertEquals(comments, task.comments);
    }

    @Test
    @DisplayName("toFields() should return correct map with all fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        List<String> comments = Arrays.asList("Comment");
        task.summary = "Test Task";
        task.reporterName = "Reporter";
        task.assigneeName = "Assignee";
        task.taskType = "Task";
        task.taskKey = "TASK-001";
        task.id = "123";
        task.projectKey = "TEST";
        task.priority = "Low";
        task.status = "Done";
        task.description = "Task Description";
        task.comments = comments;
        
        // Act
        Map<String, Object> fieldMap = task.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertEquals(11, fieldMap.size());
        assertEquals("Test Task", fieldMap.get("Summary"));
        assertEquals(comments, fieldMap.get("Comments"));
        assertEquals("Reporter", fieldMap.get("Reporter Name"));
        assertEquals("Assignee", fieldMap.get("Assignee Name"));
        assertEquals("TASK-001", fieldMap.get("Task Key"));
        assertEquals("123", fieldMap.get("Id"));
        assertEquals("TEST", fieldMap.get("Project Key"));
        assertEquals("Task Description", fieldMap.get("Description"));
        assertEquals("Task", fieldMap.get("Task Type"));
        assertEquals("Low", fieldMap.get("Priority"));
        assertEquals("Done", fieldMap.get("Status"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Act
        Map<String, Object> fieldMap = task.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertNull(fieldMap.get("Summary"));
        assertNull(fieldMap.get("Comments"));
        assertNull(fieldMap.get("Task Key"));
    }

    @Test
    @DisplayName("Should create new instance with default null values")
    void testDefaultValues() {
        // Arrange & Act
        Task newTask = new Task();
        
        // Assert
        assertNull(newTask.summary);
        assertNull(newTask.reporterName);
        assertNull(newTask.assigneeName);
        assertNull(newTask.taskType);
        assertNull(newTask.taskKey);
        assertNull(newTask.id);
        assertNull(newTask.projectKey);
        assertNull(newTask.priority);
        assertNull(newTask.status);
        assertNull(newTask.description);
        assertNull(newTask.comments);
    }
}

