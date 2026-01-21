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
 * Comprehensive test suite for Story entity class.
 * Tests all fields, toFields() method, and edge cases.
 */
@DisplayName("Story Entity Tests")
class StoryTest {

    private Story story;

    @BeforeEach
    void setUp() {
        story = new Story();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testAllFieldsSetAndGet() {
        // Arrange
        List<String> comments = Arrays.asList("Comment 1", "Comment 2");
        
        // Act
        story.summary = "User story for login feature";
        story.reporterName = "Product Owner";
        story.assigneeName = "Developer";
        story.storyType = "Story";
        story.storyKey = "STORY-456";
        story.id = "20001";
        story.projectKey = "PROJ";
        story.priority = "High";
        story.status = "In Progress";
        story.description = "As a user, I want to login";
        story.comments = comments;

        // Assert
        assertEquals("User story for login feature", story.summary);
        assertEquals("Product Owner", story.reporterName);
        assertEquals("Developer", story.assigneeName);
        assertEquals("Story", story.storyType);
        assertEquals("STORY-456", story.storyKey);
        assertEquals("20001", story.id);
        assertEquals("PROJ", story.projectKey);
        assertEquals("High", story.priority);
        assertEquals("In Progress", story.status);
        assertEquals("As a user, I want to login", story.description);
        assertEquals(comments, story.comments);
    }

    @Test
    @DisplayName("toFields() should return correct map with all fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        List<String> comments = Arrays.asList("Comment 1");
        story.summary = "Test Story";
        story.reporterName = "Reporter";
        story.assigneeName = "Assignee";
        story.storyType = "Story";
        story.storyKey = "STORY-001";
        story.id = "123";
        story.projectKey = "TEST";
        story.priority = "Medium";
        story.status = "Done";
        story.description = "Story Description";
        story.comments = comments;

        // Act
        Map<String, Object> fieldMap = story.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(11, fieldMap.size());
        assertEquals("Test Story", fieldMap.get("Summary"));
        assertEquals(comments, fieldMap.get("Comments"));
        assertEquals("Reporter", fieldMap.get("Reporter Name"));
        assertEquals("Assignee", fieldMap.get("Assignee Name"));
        assertEquals("STORY-001", fieldMap.get("Story Key"));
        assertEquals("123", fieldMap.get("Id"));
        assertEquals("TEST", fieldMap.get("Project Key"));
        assertEquals("Story Description", fieldMap.get("Description"));
        assertEquals("Story", fieldMap.get("Story Type"));
        assertEquals("Medium", fieldMap.get("Priority"));
        assertEquals("Done", fieldMap.get("Status"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Act
        Map<String, Object> fieldMap = story.toFields();
        
        // Assert
        assertNotNull(fieldMap);
        assertEquals(11, fieldMap.size());
        assertNull(fieldMap.get("Summary"));
        assertNull(fieldMap.get("Comments"));
        assertNull(fieldMap.get("Reporter Name"));
        assertNull(fieldMap.get("Assignee Name"));
        assertNull(fieldMap.get("Story Key"));
        assertNull(fieldMap.get("Id"));
        assertNull(fieldMap.get("Project Key"));
        assertNull(fieldMap.get("Description"));
        assertNull(fieldMap.get("Story Type"));
        assertNull(fieldMap.get("Priority"));
        assertNull(fieldMap.get("Status"));
    }

    @Test
    @DisplayName("toFields() should maintain field order")
    void testToFields_FieldOrder() {
        // Arrange
        story.summary = "Summary";
        story.comments = Arrays.asList("Comment");
        story.reporterName = "Reporter";
        story.assigneeName = "Assignee";
        story.storyKey = "KEY-1";
        story.id = "1";
        story.projectKey = "PROJ";
        story.description = "Desc";
        story.storyType = "Story";
        story.priority = "High";
        story.status = "Open";

        // Act
        Map<String, Object> fieldMap = story.toFields();

        // Assert
        List<String> expectedOrder = Arrays.asList(
            "Comments", "Summary", "Reporter Name", "Assignee Name",
            "Story Type", "Story Key", "Id", "Project Key",
            "Priority", "Status", "Description"
        );
        List<String> actualOrder = Arrays.asList(fieldMap.keySet().toArray(new String[0]));
        assertEquals(expectedOrder, actualOrder);
    }

    @Test
    @DisplayName("Should create new instance with default null values")
    void testDefaultValues() {
        // Arrange & Act
        Story newStory = new Story();
        
        // Assert
        assertNull(newStory.summary);
        assertNull(newStory.reporterName);
        assertNull(newStory.assigneeName);
        assertNull(newStory.storyType);
        assertNull(newStory.storyKey);
        assertNull(newStory.id);
        assertNull(newStory.projectKey);
        assertNull(newStory.priority);
        assertNull(newStory.status);
        assertNull(newStory.description);
        assertNull(newStory.comments);
    }
}

