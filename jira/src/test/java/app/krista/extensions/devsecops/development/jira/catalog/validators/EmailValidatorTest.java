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
 * Comprehensive test suite for EmailValidator class.
 * Tests email validation logic and email extraction.
 */
@DisplayName("EmailValidator Tests")
class EmailValidatorTest {

    private EmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new EmailValidator();
    }

    @Test
    @DisplayName("Should validate correct email address")
    void testValidate_ValidEmail_ReturnsTrue() {
        // Arrange
        String email = "user@example.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept valid email address");
    }

    @Test
    @DisplayName("Should validate email with subdomain")
    void testValidate_EmailWithSubdomain_ReturnsTrue() {
        // Arrange
        String email = "user@mail.example.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept email with subdomain");
    }

    @Test
    @DisplayName("Should validate email with plus sign")
    void testValidate_EmailWithPlusSign_ReturnsTrue() {
        // Arrange
        String email = "user+tag@example.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept email with plus sign");
    }

    @Test
    @DisplayName("Should validate email with dots")
    void testValidate_EmailWithDots_ReturnsTrue() {
        // Arrange
        String email = "first.last@example.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept email with dots");
    }

    @Test
    @DisplayName("Should validate email with numbers")
    void testValidate_EmailWithNumbers_ReturnsTrue() {
        // Arrange
        String email = "user123@example456.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept email with numbers");
    }

    @Test
    @DisplayName("Should reject invalid email without @")
    void testValidate_EmailWithoutAt_ReturnsFalse() {
        // Arrange
        String email = "userexample.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertFalse(result, "Should reject email without @");
    }

    @Test
    @DisplayName("Should reject invalid email without domain")
    void testValidate_EmailWithoutDomain_ReturnsFalse() {
        // Arrange
        String email = "user@";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertFalse(result, "Should reject email without domain");
    }

    @Test
    @DisplayName("Should reject invalid email without TLD")
    void testValidate_EmailWithoutTLD_ReturnsFalse() {
        // Arrange
        String email = "user@example";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertFalse(result, "Should reject email without TLD");
    }

    @Test
    @DisplayName("Should reject empty email")
    void testValidate_EmptyEmail_ReturnsFalse() {
        // Arrange
        String email = "";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertFalse(result, "Should reject empty email");
    }

    @Test
    @DisplayName("Should extract email from text with surrounding content")
    void testEmailExtractor_EmailInText_ExtractsEmail() {
        // Arrange
        String text = "Contact us at support@example.com for help";

        // Act
        String extracted = EmailValidator.emailExtractor(text);

        // Assert
        assertEquals("support@example.com", extracted);
    }

    @Test
    @DisplayName("Should extract email from text with multiple words")
    void testEmailExtractor_EmailWithWords_ExtractsEmail() {
        // Arrange
        String text = "Please email john.doe@company.com today";

        // Act
        String extracted = EmailValidator.emailExtractor(text);

        // Assert
        assertEquals("john.doe@company.com", extracted);
    }

    @Test
    @DisplayName("Should return original text when no email found")
    void testEmailExtractor_NoEmail_ReturnsOriginal() {
        // Arrange
        String text = "No email here";

        // Act
        String extracted = EmailValidator.emailExtractor(text);

        // Assert
        assertEquals("No email here", extracted);
    }

    @Test
    @DisplayName("Should extract first email when multiple emails present")
    void testEmailExtractor_MultipleEmails_ExtractsFirst() {
        // Arrange
        String text = "Contact first@example.com or second@example.com";

        // Act
        String extracted = EmailValidator.emailExtractor(text);

        // Assert
        assertEquals("first@example.com", extracted);
    }

    @Test
    @DisplayName("Should extract email with plus sign")
    void testEmailExtractor_EmailWithPlus_ExtractsEmail() {
        // Arrange
        String text = "Send to user+tag@example.com";

        // Act
        String extracted = EmailValidator.emailExtractor(text);

        // Assert
        assertEquals("user+tag@example.com", extracted);
    }

    @Test
    @DisplayName("Should validate extracted email from text")
    void testValidate_EmailInText_ReturnsTrue() {
        // Arrange
        String emailInText = "Please contact support@example.com for assistance";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, emailInText
        );

        // Act
        Boolean result = validator.validate(emailInText, context);

        // Assert
        assertTrue(result, "Should extract and validate email from text");
    }

    @Test
    @DisplayName("getFetchFieldName should return 'Email'")
    void testGetFetchFieldName() {
        // Act
        String fieldName = validator.getFetchFieldName();

        // Assert
        assertEquals("Email", fieldName);
    }

    @Test
    @DisplayName("getFetchStepMessage should return helpful message")
    void testGetFetchStepMessage() {
        // Act
        String message = validator.getFetchStepMessage();

        // Assert
        assertNotNull(message);
        assertTrue(message.contains("Email"));
    }

    @Test
    @DisplayName("getConfirmationStepMessage should include email")
    void testGetConfirmationStepMessage() {
        // Arrange
        String email = "invalid@email";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        String message = validator.getConfirmationStepMessage(email, context);

        // Assert
        assertNotNull(message);
        assertTrue(message.contains(email));
    }

    @Test
    @DisplayName("getErrMessage should include email")
    void testGetErrMessage() {
        // Arrange
        String email = "invalid@email";

        // Act
        String message = validator.getErrMessage(email);

        // Assert
        assertNotNull(message);
        assertTrue(message.contains(email));
        assertTrue(message.contains("Invalid"));
    }

    @Test
    @DisplayName("Should validate email with hyphen in domain")
    void testValidate_EmailWithHyphenInDomain_ReturnsTrue() {
        // Arrange
        String email = "user@my-company.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept email with hyphen in domain");
    }

    @Test
    @DisplayName("Should validate email with underscore")
    void testValidate_EmailWithUnderscore_ReturnsTrue() {
        // Arrange
        String email = "user_name@example.com";
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.EMAIL, email
        );

        // Act
        Boolean result = validator.validate(email, context);

        // Assert
        assertTrue(result, "Should accept email with underscore");
    }
}

