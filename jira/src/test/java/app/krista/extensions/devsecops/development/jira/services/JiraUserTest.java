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

import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JiraUser class.
 * Tests Jira user creation and conversion to User entity.
 */
@DisplayName("JiraUser Tests")
class JiraUserTest {

    @Test
    @DisplayName("Should create JiraUser with all fields")
    void testConstructor_AllFields() {
        // Arrange & Act
        JiraUser jiraUser = new JiraUser(
            "acc-123",
            "John Doe",
            "atlassian",
            "john.doe@example.com",
            "https://example.atlassian.net/rest/api/3/user?accountId=acc-123",
            new java.util.ArrayList<>()
        );

        // Assert
        assertEquals("acc-123", jiraUser.accountId);
        assertEquals("John Doe", jiraUser.displayName);
        assertEquals("atlassian", jiraUser.accountType);
        assertEquals("john.doe@example.com", jiraUser.emailId);
        assertEquals("https://example.atlassian.net/rest/api/3/user?accountId=acc-123", jiraUser.self);
        assertNotNull(jiraUser.applicationKeys);
        assertTrue(jiraUser.applicationKeys.isEmpty());
    }

    @Test
    @DisplayName("Should create JiraUser from JsonObject with email")
    void testCreate_WithEmail() {
        // Arrange
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountId", "acc-456");
        jsonObject.addProperty("displayName", "Jane Smith");
        jsonObject.addProperty("accountType", "customer");
        jsonObject.addProperty("emailAddress", "jane.smith@example.com");

        // Act
        JiraUser jiraUser = JiraUser.create(jsonObject);

        // Assert
        assertNotNull(jiraUser);
        assertEquals("acc-456", jiraUser.accountId);
        assertEquals("Jane Smith", jiraUser.displayName);
        assertEquals("customer", jiraUser.accountType);
        assertEquals("jane.smith@example.com", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should create JiraUser from JsonObject without email")
    void testCreate_WithoutEmail() {
        // Arrange
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountId", "acc-789");
        jsonObject.addProperty("displayName", "Bob Johnson");
        jsonObject.addProperty("accountType", "app");

        // Act
        JiraUser jiraUser = JiraUser.create(jsonObject);

        // Assert
        assertNotNull(jiraUser);
        assertEquals("acc-789", jiraUser.accountId);
        assertEquals("Bob Johnson", jiraUser.displayName);
        assertEquals("app", jiraUser.accountType);
        assertEquals("", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should return display name as summary")
    void testSummary() {
        // Arrange
        JiraUser jiraUser = new JiraUser(
            "acc-111",
            "Alice Wonder",
            "atlassian",
            "alice@example.com",
            "https://example.atlassian.net/rest/api/3/user?accountId=acc-111",
            new java.util.ArrayList<>()
        );

        // Act
        String summary = jiraUser.summary();

        // Assert
        assertEquals("Alice Wonder", summary);
    }

    @Test
    @DisplayName("Should convert JiraUser to User entity")
    void testGetUser() {
        // Arrange
        JiraUser jiraUser = new JiraUser(
            "acc-222",
            "Charlie Brown",
            "atlassian",
            "charlie@example.com",
            "https://example.atlassian.net/rest/api/3/user?accountId=acc-222",
            java.util.Arrays.asList("jira-software", "jira-core")
        );

        // Act
        User user = jiraUser.getUser();

        // Assert
        assertNotNull(user);
        assertEquals("acc-222", user.accountId);
        assertEquals("Charlie Brown", user.name);
        assertEquals("atlassian", user.accountType);
        assertEquals("charlie@example.com", user.email);
        assertEquals("Charlie Brown", user.displayName);
        assertEquals("https://example.atlassian.net/rest/api/3/user?accountId=acc-222", user.self);
        assertNotNull(user.applicationKeys);
        assertEquals(2, user.applicationKeys.size());
        assertTrue(user.applicationKeys.contains("jira-software"));
        assertTrue(user.applicationKeys.contains("jira-core"));
    }

    @Test
    @DisplayName("Should handle empty strings in constructor")
    void testConstructor_EmptyStrings() {
        // Arrange & Act
        JiraUser jiraUser = new JiraUser("", "", "", "", "", new java.util.ArrayList<>());

        // Assert
        assertEquals("", jiraUser.accountId);
        assertEquals("", jiraUser.displayName);
        assertEquals("", jiraUser.accountType);
        assertEquals("", jiraUser.emailId);
        assertEquals("", jiraUser.self);
        assertNotNull(jiraUser.applicationKeys);
        assertTrue(jiraUser.applicationKeys.isEmpty());
    }

    @Test
    @DisplayName("Should handle special characters in display name")
    void testCreate_SpecialCharacters() {
        // Arrange
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountId", "acc-333");
        jsonObject.addProperty("displayName", "O'Brien, John (Jr.)");
        jsonObject.addProperty("accountType", "atlassian");
        jsonObject.addProperty("emailAddress", "john.obrien@example.com");

        // Act
        JiraUser jiraUser = JiraUser.create(jsonObject);

        // Assert
        assertEquals("O'Brien, John (Jr.)", jiraUser.displayName);
    }

    @Test
    @DisplayName("Should handle long email addresses")
    void testCreate_LongEmail() {
        // Arrange
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountId", "acc-444");
        jsonObject.addProperty("displayName", "User Name");
        jsonObject.addProperty("accountType", "atlassian");
        jsonObject.addProperty("emailAddress", "very.long.email.address.with.many.dots@subdomain.example.com");

        // Act
        JiraUser jiraUser = JiraUser.create(jsonObject);

        // Assert
        assertEquals("very.long.email.address.with.many.dots@subdomain.example.com", jiraUser.emailId);
    }

    @Test
    @DisplayName("Should handle different account types")
    void testCreate_DifferentAccountTypes() {
        // Arrange
        String[] accountTypes = {"atlassian", "customer", "app"};

        for (String accountType : accountTypes) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("accountId", "acc-" + accountType);
            jsonObject.addProperty("displayName", "Test User");
            jsonObject.addProperty("accountType", accountType);
            jsonObject.addProperty("emailAddress", "test@example.com");

            // Act
            JiraUser jiraUser = JiraUser.create(jsonObject);

            // Assert
            assertEquals(accountType, jiraUser.accountType);
        }
    }

    @Test
    @DisplayName("Should convert to User entity with all fields populated")
    void testGetUser_AllFieldsPopulated() {
        // Arrange
        JiraUser jiraUser = new JiraUser(
            "acc-555",
            "David Lee",
            "customer",
            "david.lee@company.com",
            "https://example.atlassian.net/rest/api/3/user?accountId=acc-555",
            java.util.Arrays.asList("jira-servicedesk")
        );

        // Act
        User user = jiraUser.getUser();

        // Assert
        assertNotNull(user);
        assertEquals("acc-555", user.accountId);
        assertEquals("David Lee", user.name);
        assertEquals("customer", user.accountType);
        assertEquals("david.lee@company.com", user.email);
        assertEquals("David Lee", user.displayName);
        assertEquals("https://example.atlassian.net/rest/api/3/user?accountId=acc-555", user.self);
        assertNotNull(user.applicationKeys);
        assertEquals(1, user.applicationKeys.size());
        assertEquals("jira-servicedesk", user.applicationKeys.get(0));
    }

    @Test
    @DisplayName("Should handle unicode characters in display name")
    void testCreate_UnicodeCharacters() {
        // Arrange
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountId", "acc-666");
        jsonObject.addProperty("displayName", "José García");
        jsonObject.addProperty("accountType", "atlassian");
        jsonObject.addProperty("emailAddress", "jose.garcia@example.com");

        // Act
        JiraUser jiraUser = JiraUser.create(jsonObject);

        // Assert
        assertEquals("José García", jiraUser.displayName);
    }

    @Test
    @DisplayName("Should create multiple JiraUser instances independently")
    void testCreate_MultipleInstances() {
        // Arrange
        JsonObject json1 = new JsonObject();
        json1.addProperty("accountId", "acc-1");
        json1.addProperty("displayName", "User 1");
        json1.addProperty("accountType", "atlassian");
        json1.addProperty("emailAddress", "user1@example.com");

        JsonObject json2 = new JsonObject();
        json2.addProperty("accountId", "acc-2");
        json2.addProperty("displayName", "User 2");
        json2.addProperty("accountType", "customer");
        json2.addProperty("emailAddress", "user2@example.com");

        // Act
        JiraUser user1 = JiraUser.create(json1);
        JiraUser user2 = JiraUser.create(json2);

        // Assert
        assertNotEquals(user1.accountId, user2.accountId);
        assertNotEquals(user1.displayName, user2.displayName);
        assertNotEquals(user1.accountType, user2.accountType);
        assertNotEquals(user1.emailId, user2.emailId);
    }
}

