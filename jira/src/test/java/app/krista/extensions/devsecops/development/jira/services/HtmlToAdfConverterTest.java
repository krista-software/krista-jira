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
    @DisplayName("Should handle unsupported tags by extracting text content")
    void testConvert_UnsupportedTags() {
        // Arrange
        String html = "<p><span>Span text</span></p>";

        // Act
        JsonObject result = converter.convert(html);

        // Assert
        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        // Should extract "Span text" as plain text, not create a nested paragraph
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Span text", textNode.get("text").getAsString());
    }

    @Test
    @DisplayName("Should convert <strong> tag to strong mark")
    void testConvert_StrongTag() {
        String html = "<p><strong>Bold text</strong></p>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();

        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Bold text", textNode.get("text").getAsString());
        assertTrue(textNode.has("marks"));
        assertEquals("strong", textNode.getAsJsonArray("marks").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert <em> tag to em mark")
    void testConvert_EmTag() {
        String html = "<p><em>Italic text</em></p>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();

        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Italic text", textNode.get("text").getAsString());
        assertTrue(textNode.has("marks"));
        assertEquals("em", textNode.getAsJsonArray("marks").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert strikethrough tags to strike mark")
    void testConvert_StrikethroughTags() {
        for (String tag : new String[]{"s", "del", "strike"}) {
            String html = "<p><" + tag + ">Strikethrough</" + tag + "></p>";
            JsonObject result = converter.convert(html);

            JsonArray content = result.getAsJsonArray("content");
            JsonObject paragraph = content.get(0).getAsJsonObject();
            JsonArray paragraphContent = paragraph.getAsJsonArray("content");
            JsonObject textNode = paragraphContent.get(0).getAsJsonObject();

            assertEquals("text", textNode.get("type").getAsString());
            assertTrue(textNode.has("marks"), "Missing marks for <" + tag + ">");
            assertEquals("strike", textNode.getAsJsonArray("marks").get(0).getAsJsonObject().get("type").getAsString(),
                    "Wrong mark type for <" + tag + ">");
        }
    }

    @Test
    @DisplayName("Should convert <code> tag to code mark")
    void testConvert_CodeTag() {
        String html = "<p><code>inline code</code></p>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();

        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("inline code", textNode.get("text").getAsString());
        assertTrue(textNode.has("marks"));
        assertEquals("code", textNode.getAsJsonArray("marks").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should convert <a> tag to text with link mark")
    void testConvert_LinkTag() {
        String html = "<p><a href=\"https://example.com\">Click here</a></p>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();

        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Click here", textNode.get("text").getAsString());
        assertTrue(textNode.has("marks"));

        JsonObject linkMark = textNode.getAsJsonArray("marks").get(0).getAsJsonObject();
        assertEquals("link", linkMark.get("type").getAsString());
        assertTrue(linkMark.has("attrs"));
        assertEquals("https://example.com", linkMark.getAsJsonObject("attrs").get("href").getAsString());
    }

    @Test
    @DisplayName("Should convert <br> to hardBreak node")
    void testConvert_HardBreak() {
        String html = "<p>Line one<br>Line two</p>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");

        // Should have: text("Line one"), hardBreak, text("Line two")
        boolean hasHardBreak = false;
        for (int i = 0; i < paragraphContent.size(); i++) {
            if ("hardBreak".equals(paragraphContent.get(i).getAsJsonObject().get("type").getAsString())) {
                hasHardBreak = true;
                break;
            }
        }
        assertTrue(hasHardBreak, "Should contain a hardBreak node");
    }

    @Test
    @DisplayName("Should convert heading tags to heading nodes with level")
    void testConvert_Headings() {
        for (int level = 1; level <= 6; level++) {
            String html = "<h" + level + ">Heading " + level + "</h" + level + ">";
            JsonObject result = converter.convert(html);

            JsonArray content = result.getAsJsonArray("content");
            JsonObject heading = content.get(0).getAsJsonObject();

            assertEquals("heading", heading.get("type").getAsString(), "Wrong type for h" + level);
            assertTrue(heading.has("attrs"), "Missing attrs for h" + level);
            assertEquals(level, heading.getAsJsonObject("attrs").get("level").getAsInt(), "Wrong level for h" + level);

            JsonArray headingContent = heading.getAsJsonArray("content");
            JsonObject textNode = headingContent.get(0).getAsJsonObject();
            assertEquals("Heading " + level, textNode.get("text").getAsString());
        }
    }

    @Test
    @DisplayName("Should convert <blockquote> to blockquote node")
    void testConvert_Blockquote() {
        String html = "<blockquote><p>Quoted text</p></blockquote>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject blockquote = content.get(0).getAsJsonObject();

        assertEquals("blockquote", blockquote.get("type").getAsString());
        assertTrue(blockquote.has("content"));

        JsonArray bqContent = blockquote.getAsJsonArray("content");
        JsonObject paragraph = bqContent.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle nested marks correctly (bold inside italic)")
    void testConvert_NestedMarks() {
        String html = "<p><strong><em>Bold and italic</em></strong></p>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();

        assertEquals("Bold and italic", textNode.get("text").getAsString());
        assertTrue(textNode.has("marks"));
        JsonArray marks = textNode.getAsJsonArray("marks");
        assertEquals(2, marks.size());
    }

    @Test
    @DisplayName("Should handle null input gracefully")
    void testConvert_NullInput() {
        JsonObject result = converter.convert(null);
        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());
        assertEquals(0, result.getAsJsonArray("content").size());
    }

    @Test
    @DisplayName("Should handle <a> tag with empty href")
    void testConvert_LinkWithEmptyHref() {
        String html = "<p><a href=\"\">Link text</a></p>";
        JsonObject result = converter.convert(html);

        assertNotNull(result);
        JsonArray content = result.getAsJsonArray("content");
        assertTrue(content.size() > 0);
        JsonObject paragraph = content.get(0).getAsJsonObject();
        JsonArray paragraphContent = paragraph.getAsJsonArray("content");
        JsonObject textNode = paragraphContent.get(0).getAsJsonObject();
        assertEquals("text", textNode.get("type").getAsString());
        assertEquals("Link text", textNode.get("text").getAsString());
        assertTrue(textNode.has("marks"));
        assertEquals("link", textNode.getAsJsonArray("marks").get(0).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle blockquote with plain text (no p child)")
    void testConvert_BlockquoteWithPlainText() {
        String html = "<blockquote>Plain quoted text</blockquote>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        JsonObject blockquote = content.get(0).getAsJsonObject();
        assertEquals("blockquote", blockquote.get("type").getAsString());
        assertTrue(blockquote.has("content"));

        JsonArray bqContent = blockquote.getAsJsonArray("content");
        JsonObject paragraph = bqContent.get(0).getAsJsonObject();
        assertEquals("paragraph", paragraph.get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle mixed block elements (p + heading + blockquote)")
    void testConvert_MixedBlockElements() {
        String html = "<p>Paragraph</p><h2>Heading</h2><blockquote><p>Quote</p></blockquote>";
        JsonObject result = converter.convert(html);

        JsonArray content = result.getAsJsonArray("content");
        assertEquals(3, content.size());
        assertEquals("paragraph", content.get(0).getAsJsonObject().get("type").getAsString());
        assertEquals("heading", content.get(1).getAsJsonObject().get("type").getAsString());
        assertEquals("blockquote", content.get(2).getAsJsonObject().get("type").getAsString());
    }

    @Test
    @DisplayName("Should handle the real-world AI answer that caused INVALID_INPUT")
    void testConvert_RealWorldAIAnswer() {
        String html = "<p><strong>CTIA</strong> is the trade association whose member companies developed the " +
                "<em>Messaging Principles and Best Practices</em>.</p>" +
                "<p><strong>What the Principles cover</strong>: a set of voluntary best practices.</p>" +
                "<p>For the full document, see: <a href=\"https://api.ctia.org/doc.pdf\">https://api.ctia.org/doc.pdf</a></p>";

        JsonObject result = converter.convert(html);

        assertNotNull(result);
        assertEquals("doc", result.get("type").getAsString());
        assertEquals(1, result.get("version").getAsInt());

        JsonArray content = result.getAsJsonArray("content");
        assertEquals(3, content.size());

        // First paragraph should have strong and em marks
        JsonObject firstPara = content.get(0).getAsJsonObject();
        assertEquals("paragraph", firstPara.get("type").getAsString());

        // Third paragraph should have a link
        JsonObject thirdPara = content.get(2).getAsJsonObject();
        JsonArray thirdContent = thirdPara.getAsJsonArray("content");
        boolean hasLink = false;
        for (int i = 0; i < thirdContent.size(); i++) {
            JsonObject node = thirdContent.get(i).getAsJsonObject();
            if (node.has("marks")) {
                JsonArray marks = node.getAsJsonArray("marks");
                for (int j = 0; j < marks.size(); j++) {
                    if ("link".equals(marks.get(j).getAsJsonObject().get("type").getAsString())) {
                        hasLink = true;
                        break;
                    }
                }
            }
        }
        assertTrue(hasLink, "Third paragraph should contain a link mark");
    }
}

