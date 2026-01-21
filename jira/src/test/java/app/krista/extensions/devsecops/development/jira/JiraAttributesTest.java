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

import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.util.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
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
 * Comprehensive test suite for JiraAttributes class.
 * Tests attribute management, validation, and credential checking.
 */
@DisplayName("JiraAttributes Tests")
class JiraAttributesTest {

    @Mock
    private Invoker invoker;

    @Mock
    private RequestContext requestContext;

    @Mock
    private SessionManager sessionManager;

    @Mock
    private AuthorizationContext authorizationContext;

    private JiraAttributes jiraAttributes;
    private Map<String, Object> attributes;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        attributes = new HashMap<>();
        attributes.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attributes.put(JiraAttributes.USER_NAME, "test@example.com");
        attributes.put(JiraAttributes.AUTH_TOKEN, "test_token");
        attributes.put(JiraAttributes.CLIENT_ID, "");
        attributes.put(JiraAttributes.CLIENT_SECRET, "");
        
        when(invoker.getAttributes()).thenReturn(attributes);
        
        jiraAttributes = new JiraAttributes(invoker, requestContext, sessionManager, authorizationContext);
    }

    @Test
    @DisplayName("Should have correct constant values")
    void testConstants() {
        assertEquals("Host Url", JiraAttributes.HOST_URL);
        assertEquals("Admin User Name", JiraAttributes.USER_NAME);
        assertEquals("Admin Auth Token", JiraAttributes.AUTH_TOKEN);
        assertEquals("Client Id", JiraAttributes.CLIENT_ID);
        assertEquals("Client Secret", JiraAttributes.CLIENT_SECRET);
    }

    @Test
    @DisplayName("Should initialize with attributes from invoker")
    void testConstructor_InitializesFromInvoker() {
        // Assert
        assertEquals("https://test.atlassian.net", jiraAttributes.getHostUrl());
        assertEquals("test@example.com", jiraAttributes.getUserName());
        assertEquals("test_token", jiraAttributes.getAuthToken());
    }

    @Test
    @DisplayName("Should set attributes correctly")
    void testSetAttributes_UpdatesAllFields() {
        // Arrange
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, "https://new.atlassian.net");
        newAttributes.put(JiraAttributes.USER_NAME, "new@example.com");
        newAttributes.put(JiraAttributes.AUTH_TOKEN, "new_token");
        newAttributes.put(JiraAttributes.CLIENT_ID, "new_client_id");
        newAttributes.put(JiraAttributes.CLIENT_SECRET, "new_client_secret");

        // Act
        jiraAttributes.setAttributes(newAttributes);

        // Assert
        assertEquals("https://new.atlassian.net", jiraAttributes.getHostUrl());
        assertEquals("new@example.com", jiraAttributes.getUserName());
        assertEquals("new_token", jiraAttributes.getAuthToken());
        assertEquals("new_client_id", jiraAttributes.getClientId());
        assertEquals("new_client_secret", jiraAttributes.getClientSecret());
    }

    @Test
    @DisplayName("Should return true when basic credentials are present in attributes")
    void testBasicCredentialsPresentInAttributes_AllPresent_ReturnsTrue() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attrs.put(JiraAttributes.USER_NAME, "test@example.com");
        attrs.put(JiraAttributes.AUTH_TOKEN, "test_token");

        // Act
        boolean result = jiraAttributes.basicCredentialsPresentInAttributes(attrs);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when basic credentials are missing")
    void testBasicCredentialsPresentInAttributes_MissingCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attrs.put(JiraAttributes.USER_NAME, "test@example.com");
        // Missing AUTH_TOKEN

        // Act
        boolean result = jiraAttributes.basicCredentialsPresentInAttributes(attrs);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when basic credentials are blank")
    void testBasicCredentialsPresentInAttributes_BlankCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attrs.put(JiraAttributes.USER_NAME, "");
        attrs.put(JiraAttributes.AUTH_TOKEN, "test_token");

        // Act
        boolean result = jiraAttributes.basicCredentialsPresentInAttributes(attrs);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when OAuth credentials are present in attributes")
    void testOAuthCredentialsPresentInAttributes_AllPresent_ReturnsTrue() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.CLIENT_ID, "client_id_123");
        attrs.put(JiraAttributes.CLIENT_SECRET, "client_secret_456");

        // Act
        boolean result = jiraAttributes.oAuthCredentialsPresentInAttributes(attrs);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when OAuth credentials are missing")
    void testOAuthCredentialsPresentInAttributes_MissingCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.CLIENT_ID, "client_id_123");
        // Missing CLIENT_SECRET

        // Act
        boolean result = jiraAttributes.oAuthCredentialsPresentInAttributes(attrs);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when partial basic credentials are present")
    void testIsPartialBasicCredentialPresent_PartialCredentials_ReturnsTrue() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attrs.put(JiraAttributes.USER_NAME, "test@example.com");
        // Missing AUTH_TOKEN

        // Act
        boolean result = jiraAttributes.isPartialBasicCredentialPresent(attrs);

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when no basic credentials are present")
    void testIsPartialBasicCredentialPresent_NoCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();

        // Act
        boolean result = jiraAttributes.isPartialBasicCredentialPresent(attrs);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return false when all basic credentials are present")
    void testIsPartialBasicCredentialPresent_AllCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attrs.put(JiraAttributes.USER_NAME, "test@example.com");
        attrs.put(JiraAttributes.AUTH_TOKEN, "test_token");

        // Act
        boolean result = jiraAttributes.isPartialBasicCredentialPresent(attrs);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when basic credentials are present")
    void testAreBasicCredentialsPresent_AllPresent_ReturnsTrue() {
        // Arrange
        jiraAttributes.setAttributes(attributes);

        // Act
        boolean result = jiraAttributes.areBasicCredentialsPresent();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when basic credentials are missing")
    void testAreBasicCredentialsPresent_MissingCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net");
        attrs.put(JiraAttributes.USER_NAME, "");
        attrs.put(JiraAttributes.AUTH_TOKEN, "");
        jiraAttributes.setAttributes(attrs);

        // Act
        boolean result = jiraAttributes.areBasicCredentialsPresent();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should return true when OAuth credentials are present")
    void testAreOAuthCredentialsPresent_AllPresent_ReturnsTrue() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.CLIENT_ID, "client_id_123");
        attrs.put(JiraAttributes.CLIENT_SECRET, "client_secret_456");
        jiraAttributes.setAttributes(attrs);

        // Act
        boolean result = jiraAttributes.areOAuthCredentialsPresent();

        // Assert
        assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when OAuth credentials are missing")
    void testAreOAuthCredentialsPresent_MissingCredentials_ReturnsFalse() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.CLIENT_ID, "");
        attrs.put(JiraAttributes.CLIENT_SECRET, "");
        jiraAttributes.setAttributes(attrs);

        // Act
        boolean result = jiraAttributes.areOAuthCredentialsPresent();

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Should update attributes correctly")
    void testUpdate_UpdatesAllFields() {
        // Arrange
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, "https://updated.atlassian.net");
        newAttributes.put(JiraAttributes.USER_NAME, "updated@example.com");
        newAttributes.put(JiraAttributes.AUTH_TOKEN, "updated_token");
        newAttributes.put(JiraAttributes.CLIENT_ID, "updated_client_id");
        newAttributes.put(JiraAttributes.CLIENT_SECRET, "updated_client_secret");

        // Act
        jiraAttributes.update(newAttributes);

        // Assert
        assertEquals("https://updated.atlassian.net", jiraAttributes.getHostUrl());
        assertEquals("updated@example.com", jiraAttributes.getUserName());
        assertEquals("updated_token", jiraAttributes.getAuthToken());
        assertEquals("updated_client_id", jiraAttributes.getClientId());
        assertEquals("updated_client_secret", jiraAttributes.getClientSecret());
    }

    @Test
    @DisplayName("Should handle null values in update")
    void testUpdate_NullValues_HandlesGracefully() {
        // Arrange
        Map<String, Object> newAttributes = new HashMap<>();
        newAttributes.put(JiraAttributes.HOST_URL, null);
        newAttributes.put(JiraAttributes.USER_NAME, null);
        newAttributes.put(JiraAttributes.AUTH_TOKEN, null);
        newAttributes.put(JiraAttributes.CLIENT_ID, null);
        newAttributes.put(JiraAttributes.CLIENT_SECRET, null);

        // Act
        jiraAttributes.update(newAttributes);

        // Assert
        assertNull(jiraAttributes.getHostUrl());
        assertNull(jiraAttributes.getUserName());
        assertNull(jiraAttributes.getAuthToken());
        assertNull(jiraAttributes.getClientId());
        assertNull(jiraAttributes.getClientSecret());
    }

    @Test
    @DisplayName("Should handle empty map in setAttributes")
    void testSetAttributes_EmptyMap_HandlesGracefully() {
        // Arrange
        Map<String, Object> emptyAttributes = new HashMap<>();

        // Act
        jiraAttributes.setAttributes(emptyAttributes);

        // Assert
        assertNull(jiraAttributes.getHostUrl());
        assertNull(jiraAttributes.getUserName());
        assertNull(jiraAttributes.getAuthToken());
        assertNull(jiraAttributes.getClientId());
        assertNull(jiraAttributes.getClientSecret());
    }

    @Test
    @DisplayName("Should handle special characters in credentials")
    void testSetAttributes_SpecialCharacters_HandlesCorrectly() {
        // Arrange
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraAttributes.HOST_URL, "https://test.atlassian.net/special!@#");
        attrs.put(JiraAttributes.USER_NAME, "test+user@example.com");
        attrs.put(JiraAttributes.AUTH_TOKEN, "token!@#$%^&*()");
        attrs.put(JiraAttributes.CLIENT_ID, "client_id!@#");
        attrs.put(JiraAttributes.CLIENT_SECRET, "secret!@#$");

        // Act
        jiraAttributes.setAttributes(attrs);

        // Assert
        assertEquals("https://test.atlassian.net/special!@#", jiraAttributes.getHostUrl());
        assertEquals("test+user@example.com", jiraAttributes.getUserName());
        assertEquals("token!@#$%^&*()", jiraAttributes.getAuthToken());
        assertEquals("client_id!@#", jiraAttributes.getClientId());
        assertEquals("secret!@#$", jiraAttributes.getClientSecret());
    }
}

