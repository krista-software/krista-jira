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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for HtmlManipulator class.
 * Tests HTML list manipulation functionality.
 */
@DisplayName("HtmlManipulator Tests")
class HtmlManipulatorTest {

    @Test
    @DisplayName("Should convert simple unordered list to text")
    void testHandleUnorderedLists_Simple() {
        // Arrange
        String html = "<ul><li>Item 1</li><li>Item 2</li><li>Item 3</li></ul>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("- Item 1"));
        assertTrue(result.contains("- Item 2"));
        assertTrue(result.contains("- Item 3"));
    }

    @Test
    @DisplayName("Should handle multiple unordered lists")
    void testHandleUnorderedLists_Multiple() {
        // Arrange
        String html = "<ul><li>First list item 1</li></ul><p>Some text</p><ul><li>Second list item 1</li></ul>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("- First list item 1"));
        assertTrue(result.contains("- Second list item 1"));
    }

    @Test
    @DisplayName("Should handle empty unordered list")
    void testHandleUnorderedLists_Empty() {
        // Arrange
        String html = "<ul></ul>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle HTML without unordered lists")
    void testHandleUnorderedLists_NoLists() {
        // Arrange
        String html = "<p>Just a paragraph</p>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Just a paragraph"));
    }

    @Test
    @DisplayName("Should handle nested unordered lists")
    void testHandleUnorderedLists_Nested() {
        // Arrange
        String html = "<ul><li>Parent item<ul><li>Child item</li></ul></li></ul>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("- Parent item"));
        assertTrue(result.contains("- Child item"));
    }

    @Test
    @DisplayName("Should convert simple ordered list to numbered text")
    void testHandleOrderedList_Simple() {
        // Arrange
        String html = "<ol><li>First</li><li>Second</li><li>Third</li></ol>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1. First"));
        assertTrue(result.contains("2. Second"));
        assertTrue(result.contains("3. Third"));
    }

    @Test
    @DisplayName("Should handle multiple ordered lists")
    void testHandleOrderedList_Multiple() {
        // Arrange
        String html = "<ol><li>List 1 Item 1</li></ol><p>Text</p><ol><li>List 2 Item 1</li></ol>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1. List 1 Item 1"));
        assertTrue(result.contains("1. List 2 Item 1"));
    }

    @Test
    @DisplayName("Should handle empty ordered list")
    void testHandleOrderedList_Empty() {
        // Arrange
        String html = "<ol></ol>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle HTML without ordered lists")
    void testHandleOrderedList_NoLists() {
        // Arrange
        String html = "<p>Just a paragraph</p>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Just a paragraph"));
    }

    @Test
    @DisplayName("Should handle ordered list with many items")
    void testHandleOrderedList_ManyItems() {
        // Arrange
        String html = "<ol><li>One</li><li>Two</li><li>Three</li><li>Four</li><li>Five</li></ol>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1. One"));
        assertTrue(result.contains("2. Two"));
        assertTrue(result.contains("3. Three"));
        assertTrue(result.contains("4. Four"));
        assertTrue(result.contains("5. Five"));
    }

    @Test
    @DisplayName("Should handle ordered list with special characters")
    void testHandleOrderedList_SpecialCharacters() {
        // Arrange
        String html = "<ol><li>Item with &amp; ampersand</li><li>Item with &lt; less than</li></ol>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("1. Item with"));
        assertTrue(result.contains("2. Item with"));
    }

    @Test
    @DisplayName("Should handle unordered list with special characters")
    void testHandleUnorderedLists_SpecialCharacters() {
        // Arrange
        String html = "<ul><li>Item with &amp; ampersand</li><li>Item with &lt; less than</li></ul>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("- Item with"));
    }

    @Test
    @DisplayName("Should handle null or empty HTML for unordered lists")
    void testHandleUnorderedLists_EmptyString() {
        // Arrange
        String html = "";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should handle null or empty HTML for ordered lists")
    void testHandleOrderedList_EmptyString() {
        // Arrange
        String html = "";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Should preserve other HTML tags when handling unordered lists")
    void testHandleUnorderedLists_PreserveOtherTags() {
        // Arrange
        String html = "<div><p>Paragraph</p><ul><li>Item</li></ul></div>";

        // Act
        String result = HtmlManipulator.handleUnorderedLists(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Paragraph"));
        assertTrue(result.contains("- Item"));
    }

    @Test
    @DisplayName("Should preserve other HTML tags when handling ordered lists")
    void testHandleOrderedList_PreserveOtherTags() {
        // Arrange
        String html = "<div><p>Paragraph</p><ol><li>Item</li></ol></div>";

        // Act
        String result = HtmlManipulator.handleOrderedList(html);

        // Assert
        assertNotNull(result);
        assertTrue(result.contains("Paragraph"));
        assertTrue(result.contains("1. Item"));
    }
}

