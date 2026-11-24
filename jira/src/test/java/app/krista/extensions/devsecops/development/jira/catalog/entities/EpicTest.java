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
 * Comprehensive test suite for Epic entity class.
 */
@DisplayName("Epic Entity Tests")
class EpicTest {

    private Epic epic;

    @BeforeEach
    void setUp() {
        epic = new Epic();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testAllFieldsSetAndGet() {
        // Arrange
        List<String> comments = Arrays.asList("Epic comment 1", "Epic comment 2");
        
        // Act
        epic.summary = "Q1 2024 Release";
        epic.reporterName = "Product Manager";
        epic.assigneeName = "Tech Lead";
        epic.type = "Epic";
        epic.epicKey = "EPIC-100";
        epic.id = "40001";
        epic.projectKey = "PROJ";
        epic.priority = "Highest";
        epic.status = "In Progress";
        epic.description = "Major release for Q1";
        epic.comments = comments;
        
        // Assert
        assertEquals("Q1 2024 Release", epic.summary);
        assertEquals("Product Manager", epic.reporterName);
        assertEquals("Tech Lead", epic.assigneeName);
        assertEquals("Epic", epic.type);
        assertEquals("EPIC-100", epic.epicKey);
        assertEquals("40001", epic.id);
        assertEquals("PROJ", epic.projectKey);
        assertEquals("Highest", epic.priority);
        assertEquals("In Progress", epic.status);
        assertEquals("Major release for Q1", epic.description);
        assertEquals(comments, epic.comments);
    }

    @Test
    @DisplayName("toFields() should return correct map with all fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        List<String> comments = Arrays.asList("Comment");
        epic.summary = "Test Epic";
        epic.reporterName = "Reporter";
        epic.assigneeName = "Assignee";
        epic.type = "Epic";
        epic.epicKey = "EPIC-001";
        epic.id = "123";
        epic.projectKey = "TEST";
        epic.priority = "High";
        epic.status = "Open";
        epic.description = "Epic Description";
        epic.comments = comments;
        
        // Act
        Map<String, Object> fieldMap = epic.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertEquals(13, fieldMap.size());
        assertEquals("Test Epic", fieldMap.get("Summary"));
        assertEquals(comments, fieldMap.get("Comments"));
        assertEquals("Reporter", fieldMap.get("Reporter Name"));
        assertEquals("Assignee", fieldMap.get("Assignee Name"));
        assertEquals("EPIC-001", fieldMap.get("Epic Key"));
        assertEquals("123", fieldMap.get("Id"));
        assertEquals("TEST", fieldMap.get("Project Key"));
        assertEquals("Epic Description", fieldMap.get("Description"));
        assertEquals("Epic", fieldMap.get("Type"));
        assertEquals("High", fieldMap.get("Priority"));
        assertEquals("Open", fieldMap.get("Status"));
        assertNull(fieldMap.get("Created Date"));
        assertNull(fieldMap.get("Last Modified Date"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Act
        Map<String, Object> fieldMap = epic.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertNull(fieldMap.get("Summary"));
        assertNull(fieldMap.get("Epic Key"));
    }

    @Test
    @DisplayName("Should create new instance with default null values")
    void testDefaultValues() {
        // Arrange & Act
        Epic newEpic = new Epic();
        
        // Assert
        assertNull(newEpic.summary);
        assertNull(newEpic.reporterName);
        assertNull(newEpic.assigneeName);
        assertNull(newEpic.type);
        assertNull(newEpic.epicKey);
        assertNull(newEpic.id);
        assertNull(newEpic.projectKey);
        assertNull(newEpic.priority);
        assertNull(newEpic.status);
        assertNull(newEpic.description);
        assertNull(newEpic.comments);
    }
}

