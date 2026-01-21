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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for HtmlToAdfConverter class.
 * Tests HTML to Atlassian Document Format (ADF) conversion.
 */
@DisplayName("HtmlToAdfConverter Tests")
class HtmlToAdfConverterTest {

    private HtmlToAdfConverter converter;

    @BeforeEach
    void setUp() {
        converter = new HtmlToAdfConverter();
    }

    @Test
    @DisplayName("Should convert simple paragraph to ADF")
    void testConvert_SimpleParagraph() {
        // Arrange
        String html = "<p>Simple text</p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());
        assertTrue(result.has("content"));
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should wrap non-paragraph HTML with paragraph tags")
    void testConvert_NonParagraphHTML() {
        // Arrange
        String html = "Plain text without paragraph";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertTrue(result.has("content"));
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should convert bold text to ADF with strong mark")
    void testConvert_BoldText() {
        // Arrange
        String html = "<p><b>Bold text</b></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
        
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertTrue(textNode.has("marks"));
        
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals("strong", marks.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert italic text to ADF with em mark")
    void testConvert_ItalicText() {
        // Arrange
        String html = "<p><i>Italic text</i></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        
        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals("em", marks.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert underlined text to ADF with underline mark")
    void testConvert_UnderlinedText() {
        // Arrange
        String html = "<p><u>Underlined text</u></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        
        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals("underline", marks.get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert multiple paragraphs to ADF")
    void testConvert_MultipleParagraphs() {
        // Arrange
        String html = "<p>First paragraph</p><p>Second paragraph</p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertEquals(2, content.size());
        
        JsonObject firstParagraph = content.get(0).getAsJsonObject();
        assertEquals("paragraph", firstParagraph.get("type").getAsString());
        
        JsonObject secondParagraph = content.get(1).getAsJsonObject();
        assertEquals("paragraph", secondParagraph.get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle nested formatting tags")
    void testConvert_NestedFormatting() {
        // Arrange
        String html = "<p><b><i>Bold and italic</i></b></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        
        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertTrue(marks.size() >= 1);
    }

    @Test
    @DisplayName("Should handle mixed formatted and plain text")
    void testConvert_MixedText() {
        // Arrange
        String html = "<p>Plain <b>bold</b> plain</p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        assertTrue(paragraph.has("content"));
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        assertTrue(paragraphContent.size() >= 1);
    }

    @Test
    @DisplayName("Should handle empty paragraph")
    void testConvert_EmptyParagraph() {
        // Arrange
        String html = "<p></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertTrue(result.has("content"));
    }

    @Test
    @DisplayName("Should handle empty string")
    void testConvert_EmptyString() {
        // Arrange
        String html = "";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());
    }

    @Test
    @DisplayName("Should handle paragraph with special characters")
    void testConvert_SpecialCharacters() {
        // Arrange
        String html = "<p>Text with &amp; ampersand and &lt; less than</p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }

    @Test
    @DisplayName("Should handle all supported tags together")
    void testConvert_AllSupportedTags() {
        // Arrange
        String html = "<p><b>Bold</b> <i>Italic</i> <u>Underline</u></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        assertTrue(paragraphContent.size() >= 3);
    }

    @Test
    @DisplayName("Should maintain ADF structure with version and type")
    void testConvert_AdfStructure() {
        // Arrange
        String html = "<p>Test</p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertTrue(result.has("type"));
        assertTrue(result.has("version"));
        assertTrue(result.has("content"));
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());
    }

    @Test
    @DisplayName("Should handle complex HTML with multiple formatting")
    void testConvert_ComplexHTML() {
        // Arrange
        String html = "<p>Start <b>bold text</b> middle <i>italic text</i> end</p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertEquals(1, content.size());
        JsonObject paragraph = content.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle paragraph with only whitespace")
    void testConvert_WhitespaceOnly() {
        // Arrange
        String html = "<p>   </p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle unsupported tags as paragraphs")
    void testConvert_UnsupportedTags() {
        // Arrange
        String html = "<p><span>Span text</span></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
    }
}

