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

package app.krista.extensions.devsecops.development.jira.catalog.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SummaryValidator class.
 * Tests validation logic for summary length constraints.
 */
@DisplayName("SummaryValidator Tests")
class SummaryValidatorTest {

    private SummaryValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SummaryValidator();
    }

    @Test
    @DisplayName("Should validate summary with exactly 255 characters")
    void testValidate_Exactly255Characters_ReturnsTrue() {
        // Arrange
        String summary = "A".repeat(255);
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept summary with exactly 255 characters");
    }

    @Test
    @DisplayName("Should validate summary with less than 255 characters")
    void testValidate_LessThan255Characters_ReturnsTrue() {
        // Arrange
        String summary = "This is a valid summary";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept summary with less than 255 characters");
    }

    @Test
    @DisplayName("Should reject summary with more than 255 characters")
    void testValidate_MoreThan255Characters_ReturnsFalse() {
        // Arrange
        String summary = "A".repeat(256);
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertFalse(result, "Should reject summary with more than 255 characters");
    }

    @Test
    @DisplayName("Should validate empty summary")
    void testValidate_EmptySummary_ReturnsTrue() {
        // Arrange
        String summary = "";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept empty summary");
    }

    @Test
    @DisplayName("Should validate single character summary")
    void testValidate_SingleCharacter_ReturnsTrue() {
        // Arrange
        String summary = "A";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept single character summary");
    }

    @Test
    @DisplayName("Should validate summary with special characters")
    void testValidate_SpecialCharacters_ReturnsTrue() {
        // Arrange
        String summary = "Bug: Login fails @#$%^&*() - urgent!";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept summary with special characters");
    }

    @Test
    @DisplayName("Should validate summary with unicode characters")
    void testValidate_UnicodeCharacters_ReturnsTrue() {
        // Arrange
        String summary = "Bug: 日本語 テスト 中文测试";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept summary with unicode characters");
    }

    @Test
    @DisplayName("Should reject summary with 300 characters")
    void testValidate_300Characters_ReturnsFalse() {
        // Arrange
        String summary = "A".repeat(300);
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertFalse(result, "Should reject summary with 300 characters");
    }

    @Test
    @DisplayName("getFetchFieldName should return 'Summary'")
    void testGetFetchFieldName() {
        // Act
        String fieldName = validator.getFetchFieldName();

        // Assert
        assertEquals("Summary", fieldName);
    }

    @Test
    @DisplayName("getFetchStepMessage should return helpful message")
    void testGetFetchStepMessage() {
        // Act
        String message = validator.getFetchStepMessage();

        // Assert
        assertNotNull(message);
        assertTrue(message.contains("255"));
        assertTrue(message.contains("summary"));
    }

    @Test
    @DisplayName("getConfirmationStepMessage should include summary")
    void testGetConfirmationStepMessage() {
        // Arrange
        String summary = "A".repeat(300);
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        String message = validator.getConfirmationStepMessage(summary, context);

        // Assert
        assertNotNull(message);
        assertTrue(message.contains("255"));
        assertTrue(message.contains("summary"));
    }

    @Test
    @DisplayName("getErrMessage should include summary")
    void testGetErrMessage() {
        // Arrange
        String summary = "A".repeat(300);

        // Act
        String message = validator.getErrMessage(summary);

        // Assert
        assertNotNull(message);
        assertTrue(message.contains("255"));
        assertTrue(message.contains(summary));
    }

    @Test
    @DisplayName("Should validate summary at boundary - 254 characters")
    void testValidate_254Characters_ReturnsTrue() {
        // Arrange
        String summary = "A".repeat(254);
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertTrue(result, "Should accept summary with 254 characters");
    }

    @Test
    @DisplayName("Should reject summary at boundary - 256 characters")
    void testValidate_256Characters_ReturnsFalse() {
        // Arrange
        String summary = "A".repeat(256);
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.SUMMARY, summary
        );

        // Act
        Boolean result = validator.validate(summary, context);

        // Assert
        assertFalse(result, "Should reject summary with 256 characters");
    }
}

