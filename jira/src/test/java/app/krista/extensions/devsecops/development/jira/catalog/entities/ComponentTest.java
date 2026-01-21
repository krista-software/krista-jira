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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for Component entity class.
 */
@DisplayName("Component Entity Tests")
class ComponentTest {

    @Test
    @DisplayName("Should create component with all fields")
    void testConstructor_AllFields() {
        // Arrange & Act
        Component component = new Component("comp-123", "Authentication", "PROJ");

        // Assert
        assertEquals("comp-123", component.id);
        assertEquals("Authentication", component.name);
        assertEquals("PROJ", component.project);
    }

    @Test
    @DisplayName("Should create component with null values")
    void testConstructor_NullValues() {
        // Arrange & Act
        Component component = new Component(null, null, null);

        // Assert
        assertNull(component.id);
        assertNull(component.name);
        assertNull(component.project);
    }

    @Test
    @DisplayName("toFields() should return correct map with all fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        Component component = new Component("comp-456", "Database", "BACKEND");

        // Act
        Map<String, Object> fieldMap = component.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(3, fieldMap.size());
        assertEquals("comp-456", fieldMap.get("Id"));
        assertEquals("Database", fieldMap.get("Name"));
        assertEquals("BACKEND", fieldMap.get("Project"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Arrange
        Component component = new Component(null, null, null);

        // Act
        Map<String, Object> fieldMap = component.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(3, fieldMap.size());
        assertNull(fieldMap.get("Id"));
        assertNull(fieldMap.get("Name"));
        assertNull(fieldMap.get("Project"));
    }

    @Test
    @DisplayName("Should create component with special characters in name")
    void testConstructor_SpecialCharacters() {
        // Arrange & Act
        Component component = new Component("comp-789", "UI/UX & Design", "FRONTEND");

        // Assert
        assertEquals("comp-789", component.id);
        assertEquals("UI/UX & Design", component.name);
        assertEquals("FRONTEND", component.project);
    }

    @Test
    @DisplayName("Should create component with long name")
    void testConstructor_LongName() {
        // Arrange
        String longName = "Very Long Component Name That Describes A Complex System Module";

        // Act
        Component component = new Component("comp-999", longName, "PROJ");

        // Assert
        assertEquals(longName, component.name);
    }

    @Test
    @DisplayName("toFields() should maintain field order")
    void testToFields_FieldOrder() {
        // Arrange
        Component component = new Component("id-1", "name-1", "proj-1");

        // Act
        Map<String, Object> fieldMap = component.toFields();
        String[] keys = fieldMap.keySet().toArray(new String[0]);

        // Assert
        assertEquals("Id", keys[0]);
        assertEquals("Name", keys[1]);
        assertEquals("Project", keys[2]);
    }

    @Test
    @DisplayName("Should create multiple components with different values")
    void testMultipleComponents() {
        // Arrange & Act
        Component comp1 = new Component("1", "Component1", "PROJ1");
        Component comp2 = new Component("2", "Component2", "PROJ2");

        // Assert
        assertNotEquals(comp1.id, comp2.id);
        assertNotEquals(comp1.name, comp2.name);
        assertNotEquals(comp1.project, comp2.project);
    }

    @Test
    @DisplayName("Should handle empty strings")
    void testConstructor_EmptyStrings() {
        // Arrange & Act
        Component component = new Component("", "", "");

        // Assert
        assertEquals("", component.id);
        assertEquals("", component.name);
        assertEquals("", component.project);
    }
}

