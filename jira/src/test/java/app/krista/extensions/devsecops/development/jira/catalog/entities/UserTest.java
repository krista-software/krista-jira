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
 * Comprehensive test suite for User entity class.
 */
@DisplayName("User Entity Tests")
class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
    }

    @Test
    @DisplayName("Should set and get all fields correctly")
    void testAllFieldsSetAndGet() {
        // Arrange
        List<String> appKeys = Arrays.asList("jira", "confluence");

        // Act
        user.name = "John Doe";
        user.accountId = "acc-12345";
        user.email = "john.doe@example.com";
        user.accountType = "atlassian";
        user.self = "https://api.atlassian.com/users/acc-12345";
        user.displayName = "John D.";
        user.applicationKeys = appKeys;

        // Assert
        assertEquals("John Doe", user.name);
        assertEquals("acc-12345", user.accountId);
        assertEquals("john.doe@example.com", user.email);
        assertEquals("atlassian", user.accountType);
        assertEquals("https://api.atlassian.com/users/acc-12345", user.self);
        assertEquals("John D.", user.displayName);
        assertEquals(appKeys, user.applicationKeys);
    }

    @Test
    @DisplayName("toFields() should return correct map with all required fields")
    void testToFields_AllFieldsPopulated() {
        // Arrange
        user.name = "Jane Smith";
        user.accountId = "acc-67890";
        user.email = "jane.smith@example.com";
        user.accountType = "customer";
        user.self = "https://api.example.com/users/acc-67890";
        user.displayName = "Jane S.";
        user.applicationKeys = Arrays.asList("jira-software");

        // Act
        Map<String, Object> fieldMap = user.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(7, fieldMap.size());
        assertEquals("Jane Smith", fieldMap.get("Name"));
        assertEquals("acc-67890", fieldMap.get("Account Id"));
        assertEquals("jane.smith@example.com", fieldMap.get("Email"));
        assertEquals("customer", fieldMap.get("Account Type"));
        assertEquals("https://api.example.com/users/acc-67890", fieldMap.get("Self"));
        assertEquals("Jane S.", fieldMap.get("Display Name"));
        assertEquals(Arrays.asList("jira-software"), fieldMap.get("Application Keys"));
    }

    @Test
    @DisplayName("toFields() should include all fields")
    void testToFields_AllFieldsIncluded() {
        // Arrange
        user.name = "Test User";
        user.accountId = "acc-111";
        user.email = "test@example.com";
        user.accountType = "app";
        user.self = "https://api.example.com/users/acc-111";
        user.displayName = "Test";
        user.applicationKeys = Arrays.asList("app1", "app2");

        // Act
        Map<String, Object> fieldMap = user.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(7, fieldMap.size());
        assertTrue(fieldMap.containsKey("Self"));
        assertTrue(fieldMap.containsKey("Display Name"));
        assertTrue(fieldMap.containsKey("Application Keys"));
        assertEquals("https://api.example.com/users/acc-111", fieldMap.get("Self"));
        assertEquals("Test", fieldMap.get("Display Name"));
        assertEquals(Arrays.asList("app1", "app2"), fieldMap.get("Application Keys"));
    }

    @Test
    @DisplayName("toFields() should handle null values")
    void testToFields_NullValues() {
        // Act
        Map<String, Object> fieldMap = user.toFields();

        // Assert
        assertNotNull(fieldMap);
        assertEquals(7, fieldMap.size());
        assertNull(fieldMap.get("Name"));
        assertNull(fieldMap.get("Account Id"));
        assertNull(fieldMap.get("Email"));
        assertNull(fieldMap.get("Account Type"));
        assertNull(fieldMap.get("Self"));
        assertNull(fieldMap.get("Display Name"));
        assertNull(fieldMap.get("Application Keys"));
    }

    @Test
    @DisplayName("Should create new instance with default null values")
    void testDefaultValues() {
        // Arrange & Act
        User newUser = new User();

        // Assert
        assertNull(newUser.name);
        assertNull(newUser.accountId);
        assertNull(newUser.email);
        assertNull(newUser.accountType);
        assertNull(newUser.self);
        assertNull(newUser.displayName);
        assertNull(newUser.applicationKeys);
    }

    @Test
    @DisplayName("Should handle special characters in name")
    void testSpecialCharactersInName() {
        // Arrange & Act
        user.name = "O'Brien, John-Paul";
        user.accountId = "acc-123";
        user.email = "john.obrien@example.com";
        user.accountType = "atlassian";

        // Assert
        assertEquals("O'Brien, John-Paul", user.name);
    }

    @Test
    @DisplayName("Should handle unicode characters in name")
    void testUnicodeCharactersInName() {
        // Arrange & Act
        user.name = "田中太郎";
        user.accountId = "acc-456";
        user.email = "tanaka@example.jp";
        user.accountType = "atlassian";

        // Assert
        assertEquals("田中太郎", user.name);
    }

    @Test
    @DisplayName("Should handle multiple application keys")
    void testMultipleApplicationKeys() {
        // Arrange
        List<String> appKeys = Arrays.asList("jira", "confluence", "bitbucket", "trello");

        // Act
        user.applicationKeys = appKeys;

        // Assert
        assertEquals(4, user.applicationKeys.size());
        assertTrue(user.applicationKeys.contains("jira"));
        assertTrue(user.applicationKeys.contains("confluence"));
        assertTrue(user.applicationKeys.contains("bitbucket"));
        assertTrue(user.applicationKeys.contains("trello"));
    }

    @Test
    @DisplayName("Should handle empty application keys list")
    void testEmptyApplicationKeys() {
        // Arrange & Act
        user.applicationKeys = Arrays.asList();

        // Assert
        assertNotNull(user.applicationKeys);
        assertEquals(0, user.applicationKeys.size());
    }

    @Test
    @DisplayName("Should handle long email address")
    void testLongEmailAddress() {
        // Arrange
        String longEmail = "very.long.email.address.with.many.dots@subdomain.example.company.com";

        // Act
        user.email = longEmail;

        // Assert
        assertEquals(longEmail, user.email);
    }

    @Test
    @DisplayName("toFields() should maintain field order")
    void testToFields_FieldOrder() {
        // Arrange
        user.name = "Test";
        user.accountId = "123";
        user.email = "test@test.com";
        user.accountType = "type";

        // Act
        Map<String, Object> fieldMap = user.toFields();
        String[] keys = fieldMap.keySet().toArray(new String[0]);

        // Assert
        assertEquals("Name", keys[0]);
        assertEquals("Account Id", keys[1]);
        assertEquals("Email", keys[2]);
        assertEquals("Account Type", keys[3]);
    }

    @Test
    @DisplayName("Should handle different account types")
    void testDifferentAccountTypes() {
        // Arrange & Act
        user.accountType = "atlassian";
        assertEquals("atlassian", user.accountType);

        user.accountType = "customer";
        assertEquals("customer", user.accountType);

        user.accountType = "app";
        assertEquals("app", user.accountType);
    }
}

