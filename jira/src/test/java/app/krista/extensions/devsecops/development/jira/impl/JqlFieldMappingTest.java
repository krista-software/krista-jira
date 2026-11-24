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

package app.krista.extensions.devsecops.development.jira.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JqlFieldMapping — centralized field name and type registry
 * for Jira issue entities.
 */
@DisplayName("JqlFieldMapping Tests")
class JqlFieldMappingTest {

    // ========== forIssueField() — basic lookup ==========

    @Test
    @DisplayName("summary maps to JQL 'summary' with TEXT type")
    void testForIssueField_Summary_TextType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("summary");
        assertNotNull(mapping);
        assertEquals("summary", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.TEXT, mapping.getFieldType());
    }

    @Test
    @DisplayName("description maps to JQL 'text' with FULL_TEXT type")
    void testForIssueField_Description_FullTextType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("description");
        assertNotNull(mapping);
        assertEquals("text", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.FULL_TEXT, mapping.getFieldType());
    }

    @Test
    @DisplayName("status maps to JQL 'status' with EXACT type")
    void testForIssueField_Status_ExactType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("status");
        assertNotNull(mapping);
        assertEquals("status", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.EXACT, mapping.getFieldType());
    }

    @Test
    @DisplayName("priority maps to JQL 'priority' with COMPARABLE type")
    void testForIssueField_Priority_ComparableType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("priority");
        assertNotNull(mapping);
        assertEquals("priority", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.COMPARABLE, mapping.getFieldType());
    }

    @Test
    @DisplayName("created date maps to JQL 'created' with DATE type")
    void testForIssueField_CreatedDate_DateType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("created date");
        assertNotNull(mapping);
        assertEquals("created", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.DATE, mapping.getFieldType());
    }

    @Test
    @DisplayName("last modified date maps to JQL 'updated' with DATE type")
    void testForIssueField_LastModifiedDate_DateType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("last modified date");
        assertNotNull(mapping);
        assertEquals("updated", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.DATE, mapping.getFieldType());
    }

    @Test
    @DisplayName("key maps to JQL 'key' with IDENTITY type")
    void testForIssueField_Key_IdentityType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("key");
        assertNotNull(mapping);
        assertEquals("key", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.IDENTITY, mapping.getFieldType());
    }

    // ========== Entity-specific key aliases ==========

    @Test
    @DisplayName("bug key, epic key, story key, task key all map to JQL 'key'")
    void testForIssueField_EntityKeyAliases_AllMapToKey() {
        String[] keyFields = {"bug key", "epic key", "story key", "task key"};
        for (String field : keyFields) {
            JqlFieldMapping mapping = JqlFieldMapping.forIssueField(field);
            assertNotNull(mapping, field + " should be mapped");
            assertEquals("key", mapping.getJqlFieldName());
            assertEquals(JqlFieldMapping.FieldType.IDENTITY, mapping.getFieldType());
        }
    }

    // ========== Entity-specific type aliases ==========

    @Test
    @DisplayName("type, story type, task type all map to JQL 'issuetype'")
    void testForIssueField_TypeAliases_AllMapToIssuetype() {
        String[] typeFields = {"type", "story type", "task type", "issue type"};
        for (String field : typeFields) {
            JqlFieldMapping mapping = JqlFieldMapping.forIssueField(field);
            assertNotNull(mapping, field + " should be mapped");
            assertEquals("issuetype", mapping.getJqlFieldName());
            assertEquals(JqlFieldMapping.FieldType.EXACT, mapping.getFieldType());
        }
    }

    // ========== User fields ==========

    @Test
    @DisplayName("assignee name maps to JQL 'assignee' with EXACT type")
    void testForIssueField_AssigneeName_ExactType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("assignee name");
        assertNotNull(mapping);
        assertEquals("assignee", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.EXACT, mapping.getFieldType());
    }

    @Test
    @DisplayName("reporter name maps to JQL 'reporter' with EXACT type")
    void testForIssueField_ReporterName_ExactType() {
        JqlFieldMapping mapping = JqlFieldMapping.forIssueField("reporter name");
        assertNotNull(mapping);
        assertEquals("reporter", mapping.getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.EXACT, mapping.getFieldType());
    }

    // ========== Case insensitivity ==========

    @Test
    @DisplayName("field lookup is case insensitive")
    void testForIssueField_CaseInsensitive() {
        assertNotNull(JqlFieldMapping.forIssueField("Summary"));
        assertNotNull(JqlFieldMapping.forIssueField("SUMMARY"));
        assertNotNull(JqlFieldMapping.forIssueField("Bug Key"));
        assertNotNull(JqlFieldMapping.forIssueField("BUG KEY"));
    }

    // ========== Null/unknown handling ==========

    @Test
    @DisplayName("null field name returns null")
    void testForIssueField_NullFieldName_ReturnsNull() {
        assertNull(JqlFieldMapping.forIssueField(null));
    }

    @Test
    @DisplayName("unknown field name returns null")
    void testForIssueField_UnknownField_ReturnsNull() {
        assertNull(JqlFieldMapping.forIssueField("nonexistent_field"));
    }

    // ========== orderByField() ==========

    @Test
    @DisplayName("orderByField returns correct JQL field for sortable fields")
    void testOrderByField_ValidFields() {
        assertEquals("summary", JqlFieldMapping.orderByField("summary"));
        assertEquals("status", JqlFieldMapping.orderByField("status"));
        assertEquals("priority", JqlFieldMapping.orderByField("priority"));
        assertEquals("created", JqlFieldMapping.orderByField("created date"));
        assertEquals("updated", JqlFieldMapping.orderByField("last modified date"));
        assertEquals("key", JqlFieldMapping.orderByField("key"));
        assertEquals("assignee", JqlFieldMapping.orderByField("assignee name"));
    }

    @Test
    @DisplayName("orderByField is case insensitive")
    void testOrderByField_CaseInsensitive() {
        assertEquals("summary", JqlFieldMapping.orderByField("Summary"));
        assertEquals("summary", JqlFieldMapping.orderByField("SUMMARY"));
    }

    @Test
    @DisplayName("orderByField returns null for null input")
    void testOrderByField_NullField_ReturnsNull() {
        assertNull(JqlFieldMapping.orderByField(null));
    }

    @Test
    @DisplayName("orderByField returns null for unknown field")
    void testOrderByField_UnknownField_ReturnsNull() {
        assertNull(JqlFieldMapping.orderByField("nonexistent"));
    }

    // ========== Other fields ==========

    @Test
    @DisplayName("labels, components, parent key, comments are mapped")
    void testForIssueField_OtherFields() {
        assertNotNull(JqlFieldMapping.forIssueField("labels"));
        assertEquals("labels", JqlFieldMapping.forIssueField("labels").getJqlFieldName());

        assertNotNull(JqlFieldMapping.forIssueField("components"));
        assertEquals("component", JqlFieldMapping.forIssueField("components").getJqlFieldName());

        assertNotNull(JqlFieldMapping.forIssueField("parent key"));
        assertEquals("parent", JqlFieldMapping.forIssueField("parent key").getJqlFieldName());

        assertNotNull(JqlFieldMapping.forIssueField("comments"));
        assertEquals("comment", JqlFieldMapping.forIssueField("comments").getJqlFieldName());
        assertEquals(JqlFieldMapping.FieldType.FULL_TEXT, JqlFieldMapping.forIssueField("comments").getFieldType());
    }
}
