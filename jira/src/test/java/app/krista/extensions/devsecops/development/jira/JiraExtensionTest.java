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

package app.krista.extensions.devsecops.development.jira;

import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.authentication.JiraRequestAuthenticator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for JiraExtension class.
 * Tests extension entry point functionality and delegation to dependencies.
 */
@DisplayName("JiraExtension Tests")
class JiraExtensionTest {

    @Mock
    private JiraRequestAuthenticator jiraRequestAuthenticator;

    @Mock
    private Invoker invoker;

    @Mock
    private JiraAttributes jiraAttributes;

    private JiraExtension jiraExtension;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jiraExtension = new JiraExtension(jiraRequestAuthenticator, invoker, jiraAttributes);
    }

    @Test
    @DisplayName("Should return custom tabs with Documentation entry")
    void testCustomTabs_ReturnsDocumentationTab() {
        // Act
        Map<String, String> result = jiraExtension.customTabs();

        // Assert — SDK refactor added Authentication, Jira Audit, and AI Assistant tabs
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.containsKey("Documentation"));
        assertEquals("static/docs", result.get("Documentation"));
        assertTrue(result.containsKey("Authentication"));
        assertEquals("jiraextn/sdk/auth/", result.get("Authentication"));
        assertTrue(result.containsKey("Jira Audit"));
        assertEquals("jiraextn/sdk/audit/", result.get("Jira Audit"));
        assertTrue(result.containsKey("AI Assistant"));
        assertEquals("jiraextn/mcp-setup", result.get("AI Assistant"));
    }

    @Test
    @DisplayName("Should delegate testConnection to jiraAttributes")
    void testTestConnection_DelegatesToJiraAttributes() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attributes.put(JiraAttributes.USER_NAME, "test@example.com");
        attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        jiraExtension.testConnection();

        // Assert
        verify(jiraAttributes, times(1)).testConnection(attributes);
    }

    @Test
    @DisplayName("Should delegate validateAttributes to jiraAttributes")
    void testValidateAttributes_DelegatesToJiraAttributes() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attributes.put(JiraAttributes.USER_NAME, "test@example.com");
        attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");

        // Act
        jiraExtension.validateAttributes(attributes);

        // Assert
        verify(jiraAttributes, times(1)).setAttributes(attributes);
        verify(jiraAttributes, times(1)).validateAttributes(attributes);
    }

    @Test
    @DisplayName("Should return jiraRequestAuthenticator from getRequestAuthenticator")
    void testGetRequestAuthenticator_ReturnsAuthenticator() {
        // Act
        RequestAuthenticator result = jiraExtension.getRequestAuthenticator();

        // Assert
        assertNotNull(result);
        assertEquals(jiraRequestAuthenticator, result);
    }

    @Test
    @DisplayName("Should delegate attributesUpdated to jiraAttributes.update")
    void testAttributesUpdated_DelegatesToJiraAttributesUpdate() {
        // Arrange
        Map<String, Object> oldAttributes = new HashMap<>();
        oldAttributes.put(JiraAttributes.HOST_URL, "https://old.atlassian.net");
        oldAttributes.put(JiraAttributes.USER_NAME, "old@example.com");
        oldAttributes.put(JiraAttributes.AUTH_TOKEN, "old_token");

        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, "https://new.atlassian.net");
        newAttributes.put(JiraAttributes.USER_NAME, "new@example.com");
        newAttributes.put(JiraAttributes.AUTH_TOKEN, "new_token");

        // Act
        jiraExtension.attributesUpdated(oldAttributes, newAttributes);

        // Assert
        verify(jiraAttributes, times(1)).update(newAttributes);
    }

    @Test
    @DisplayName("Should handle null attributes in testConnection")
    void testTestConnection_NullAttributes_DelegatesToJiraAttributes() {
        // Arrange
        when(invoker.getAttributes()).thenReturn(null);

        // Act
        jiraExtension.testConnection();

        // Assert
        verify(jiraAttributes, times(1)).testConnection(null);
    }

    @Test
    @DisplayName("Should handle empty attributes in validateAttributes")
    void testValidateAttributes_EmptyAttributes_DelegatesToJiraAttributes() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();

        // Act
        jiraExtension.validateAttributes(attributes);

        // Assert
        verify(jiraAttributes, times(1)).setAttributes(attributes);
        verify(jiraAttributes, times(1)).validateAttributes(attributes);
    }

    @Test
    @DisplayName("Should handle null old attributes in attributesUpdated")
    void testAttributesUpdated_NullOldAttributes_DelegatesToUpdate() {
        // Arrange
        Map<String, Object> oldAttributes = null;
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, "https://new.atlassian.net");

        // Act
        jiraExtension.attributesUpdated(oldAttributes, newAttributes);

        // Assert
        verify(jiraAttributes, times(1)).update(newAttributes);
    }

    @Test
    @DisplayName("Should handle null new attributes in attributesUpdated")
    void testAttributesUpdated_NullNewAttributes_DelegatesToUpdate() {
        // Arrange
        Map<String, Object> oldAttributes = new HashMap<>();
        oldAttributes.put(JiraAttributes.HOST_URL, "https://old.atlassian.net");
        Map<String, Object> newAttributes = null;

        // Act
        jiraExtension.attributesUpdated(oldAttributes, newAttributes);

        // Assert
        verify(jiraAttributes, times(1)).update(newAttributes);
    }

    @Test
    @DisplayName("Should handle OAuth credentials in testConnection")
    void testTestConnection_OAuthCredentials_DelegatesToJiraAttributes() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.CLIENT_ID, "client_id_123");
        attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret_456");

        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        jiraExtension.testConnection();

        // Assert
        verify(jiraAttributes, times(1)).testConnection(attributes);
    }

    @Test
    @DisplayName("Should handle mixed credentials in validateAttributes")
    void testValidateAttributes_MixedCredentials_DelegatesToJiraAttributes() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attributes.put(JiraAttributes.USER_NAME, "test@example.com");
        attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
        attributes.put(JiraAttributes.CLIENT_ID, "client_id_123");
        attributes.put(JiraAttributes.CLIENT_SECRET, "client_secret_456");

        // Act
        jiraExtension.validateAttributes(attributes);

        // Assert
        verify(jiraAttributes, times(1)).setAttributes(attributes);
        verify(jiraAttributes, times(1)).validateAttributes(attributes);
    }

    @Test
    @DisplayName("Should handle exception from testConnection")
    void testTestConnection_ThrowsException_PropagatesException() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

        when(invoker.getAttributes()).thenReturn(attributes);
        doThrow(new RuntimeException("Connection failed")).when(jiraAttributes).testConnection(attributes);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jiraExtension.testConnection());
        verify(jiraAttributes, times(1)).testConnection(attributes);
    }

    @Test
    @DisplayName("Should handle exception from validateAttributes")
    void testValidateAttributes_ThrowsException_PropagatesException() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");

        doThrow(new RuntimeException("Validation failed")).when(jiraAttributes).validateAttributes(attributes);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> jiraExtension.validateAttributes(attributes));
        verify(jiraAttributes, times(1)).setAttributes(attributes);
        verify(jiraAttributes, times(1)).validateAttributes(attributes);
    }

    @Test
    @DisplayName("Should handle multiple calls to customTabs")
    void testCustomTabs_MultipleCalls_ReturnsSameResult() {
        // Act
        Map<String, String> result1 = jiraExtension.customTabs();
        Map<String, String> result2 = jiraExtension.customTabs();

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2);
        assertEquals("static/docs", result1.get("Documentation"));
        assertEquals("static/docs", result2.get("Documentation"));
    }

    @Test
    @DisplayName("Should handle multiple calls to getRequestAuthenticator")
    void testGetRequestAuthenticator_MultipleCalls_ReturnsSameInstance() {
        // Act
        RequestAuthenticator result1 = jiraExtension.getRequestAuthenticator();
        RequestAuthenticator result2 = jiraExtension.getRequestAuthenticator();

        // Assert
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1, result2);
        assertEquals(jiraRequestAuthenticator, result1);
        assertEquals(jiraRequestAuthenticator, result2);
    }

    @Test
    @DisplayName("Should handle rapid successive attributesUpdated calls")
    void testAttributesUpdated_RapidSuccessiveCalls_DelegatesToUpdate() {
        // Arrange
        Map<String, Object> oldAttributes = new HashMap<>();
        oldAttributes.put(JiraAttributes.HOST_URL, "https://old.atlassian.net");

        // Act
        for (int i = 0; i < 10; i++) {
            Map<String, Object> newAttributes = new HashMap<>();
            newAttributes.put(JiraAttributes.HOST_URL, "https://new" + i + ".atlassian.net");
            jiraExtension.attributesUpdated(oldAttributes, newAttributes);
        }

        // Assert
        verify(jiraAttributes, times(10)).update(any());
    }

    @Test
    @DisplayName("Should handle attributes with special characters")
    void testTestConnection_SpecialCharacters_DelegatesToJiraAttributes() {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net/special!@#");
        attributes.put(JiraAttributes.USER_NAME, "test+user@example.com");
        attributes.put(JiraAttributes.AUTH_TOKEN, "token!@#$%^&*()");

        when(invoker.getAttributes()).thenReturn(attributes);

        // Act
        jiraExtension.testConnection();

        // Assert
        verify(jiraAttributes, times(1)).testConnection(attributes);
    }
}

