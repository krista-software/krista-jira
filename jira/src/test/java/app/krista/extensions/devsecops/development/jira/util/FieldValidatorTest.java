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

package app.krista.extensions.devsecops.development.jira.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for FieldValidator utility class.
 * Tests all public static methods with edge cases, null values, empty inputs, and boundary conditions.
 * 
 * Following extension revamp guidelines:
 * - No Mockito usage - using real concrete implementations only
 * - 100% coverage of utility methods
 * - Edge cases: null, empty, invalid inputs, boundary values
 */
@DisplayName("FieldValidator Utility Tests")
class FieldValidatorTest {

    @Test
    @DisplayName("getNullOrEmptyDefault: Should return empty string for null input")
    void testGetNullOrEmptyDefault_NullInput_ReturnsEmptyString() {
        // Arrange
        String input = null;
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("", result, "Should return empty string for null input");
        assertTrue(result.isEmpty(), "Result should be empty string");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should return same string for non-null input")
    void testGetNullOrEmptyDefault_NonNullInput_ReturnsSameString() {
        // Arrange
        String input = "test value";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(input, result, "Should return the same string for non-null input");
        assertSame(input, result, "Should return the exact same string reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should return empty string for empty input")
    void testGetNullOrEmptyDefault_EmptyInput_ReturnsEmptyString() {
        // Arrange
        String input = "";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("", result, "Should return empty string for empty input");
        assertSame(input, result, "Should return the exact same empty string reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should handle whitespace-only strings")
    void testGetNullOrEmptyDefault_WhitespaceInput_ReturnsWhitespaceString() {
        // Arrange
        String input = "   ";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("   ", result, "Should return whitespace string as-is");
        assertSame(input, result, "Should return the exact same whitespace string reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should handle single character strings")
    void testGetNullOrEmptyDefault_SingleCharacter_ReturnsSameString() {
        // Arrange
        String input = "a";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("a", result, "Should return single character string as-is");
        assertSame(input, result, "Should return the exact same string reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should handle very long strings")
    void testGetNullOrEmptyDefault_VeryLongString_ReturnsSameString() {
        // Arrange
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String input = sb.toString();
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(input, result, "Should return very long string as-is");
        assertSame(input, result, "Should return the exact same string reference");
        assertEquals(10000, result.length(), "Should preserve string length");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should handle special characters")
    void testGetNullOrEmptyDefault_SpecialCharacters_ReturnsSameString() {
        // Arrange
        String input = "!@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(input, result, "Should return special characters string as-is");
        assertSame(input, result, "Should return the exact same string reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should handle Unicode characters")
    void testGetNullOrEmptyDefault_UnicodeCharacters_ReturnsSameString() {
        // Arrange
        String input = "Hello 世界 🌍 Ñoël";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(input, result, "Should return Unicode string as-is");
        assertSame(input, result, "Should return the exact same string reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Should handle newline and tab characters")
    void testGetNullOrEmptyDefault_NewlineAndTabs_ReturnsSameString() {
        // Arrange
        String input = "Line 1\nLine 2\tTabbed";
        
        // Act
        String result = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals(input, result, "Should return string with newlines and tabs as-is");
        assertSame(input, result, "Should return the exact same string reference");
        assertTrue(result.contains("\n"), "Should preserve newline characters");
        assertTrue(result.contains("\t"), "Should preserve tab characters");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Multiple calls with same input should return same reference")
    void testGetNullOrEmptyDefault_MultipleCalls_ReturnsSameReference() {
        // Arrange
        String input = "consistent value";
        
        // Act
        String result1 = FieldValidator.getNullOrEmptyDefault(input);
        String result2 = FieldValidator.getNullOrEmptyDefault(input);
        
        // Assert
        assertSame(input, result1, "First call should return same reference");
        assertSame(input, result2, "Second call should return same reference");
        assertSame(result1, result2, "Both results should be the same reference");
    }

    @Test
    @DisplayName("getNullOrEmptyDefault: Multiple calls with null should return empty strings")
    void testGetNullOrEmptyDefault_MultipleNullCalls_ReturnsEmptyStrings() {
        // Act
        String result1 = FieldValidator.getNullOrEmptyDefault(null);
        String result2 = FieldValidator.getNullOrEmptyDefault(null);
        
        // Assert
        assertEquals("", result1, "First call should return empty string");
        assertEquals("", result2, "Second call should return empty string");
        // Note: We don't test reference equality for empty strings as it depends on JVM string interning
    }
}
