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

import app.krista.extensions.util.KeyValueStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for SessionManager class.
 * Tests session token storage and retrieval functionality.
 */
@DisplayName("SessionManager Tests")
class SessionManagerTest {

    private SessionManager sessionManager;
    private TestKeyValueStore keyValueStore;

    /**
     * Simple in-memory implementation of KeyValueStore for testing.
     */
    private static class TestKeyValueStore implements KeyValueStore {
        private final Map<String, Object> store = new HashMap<>();

        @Override
        public void put(String key, Object value) {
            store.put(key, value);
        }

        @Override
        public <T> T get(String key, Class<T> type) {
            Object value = store.get(key);
            if (value == null) {
                return null;
            }
            return type.cast(value);
        }

        @Override
        public Object get(String key) {
            return store.get(key);
        }

        @Override
        public java.util.Set<String> getKeys() {
            return store.keySet();
        }

        @Override
        public boolean supports(Class<?> type) {
            return true;
        }

        @Override
        public boolean contains(String key) {
            return store.containsKey(key);
        }

        @Override
        public void remove(String key) {
            store.remove(key);
        }

        @Override
        public java.util.Iterator<java.util.Map.Entry<String, Object>> iterator() {
            return store.entrySet().iterator();
        }

        public void clear() {
            store.clear();
        }

        public boolean containsKey(String key) {
            return store.containsKey(key);
        }

        public int size() {
            return store.size();
        }
    }

    @BeforeEach
    void setUp() {
        keyValueStore = new TestKeyValueStore();
        sessionManager = new SessionManager(keyValueStore);
    }

    @Test
    @DisplayName("Should store refresh token successfully")
    void testStoreRefToken_ValidToken_StoresSuccessfully() {
        // Arrange
        String key = "user123";
        String refToken = "refresh_token_abc123";

        // Act
        sessionManager.storeRefToken(key, refToken);

        // Assert
        assertTrue(keyValueStore.containsKey(key));
        assertEquals(refToken, keyValueStore.get(key, String.class));
    }

    @Test
    @DisplayName("Should retrieve stored refresh token")
    void testGetRefToken_StoredToken_RetrievesSuccessfully() {
        // Arrange
        String key = "user456";
        String refToken = "refresh_token_xyz789";
        sessionManager.storeRefToken(key, refToken);

        // Act
        String result = sessionManager.getRefToken(key);

        // Assert
        assertNotNull(result);
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should return null for non-existent token")
    void testGetRefToken_NonExistentKey_ReturnsNull() {
        // Arrange
        String key = "nonexistent";

        // Act
        String result = sessionManager.getRefToken(key);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should overwrite existing token with new value")
    void testStoreRefToken_ExistingKey_OverwritesValue() {
        // Arrange
        String key = "user789";
        String oldToken = "old_token_123";
        String newToken = "new_token_456";
        sessionManager.storeRefToken(key, oldToken);

        // Act
        sessionManager.storeRefToken(key, newToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(newToken, result);
        assertNotEquals(oldToken, result);
    }

    @Test
    @DisplayName("Should handle empty string as key")
    void testStoreRefToken_EmptyKey_StoresSuccessfully() {
        // Arrange
        String key = "";
        String refToken = "token_for_empty_key";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle empty string as token value")
    void testStoreRefToken_EmptyToken_StoresSuccessfully() {
        // Arrange
        String key = "user_with_empty_token";
        String refToken = "";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals("", result);
    }

    @Test
    @DisplayName("Should handle very long token values")
    void testStoreRefToken_VeryLongToken_StoresSuccessfully() {
        // Arrange
        String key = "user_long_token";
        String refToken = "a".repeat(10000); // 10,000 character token

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
        assertEquals(10000, result.length());
    }

    @Test
    @DisplayName("Should handle special characters in key")
    void testStoreRefToken_SpecialCharactersInKey_StoresSuccessfully() {
        // Arrange
        String key = "user@example.com#123!";
        String refToken = "token_special_key";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle special characters in token")
    void testStoreRefToken_SpecialCharactersInToken_StoresSuccessfully() {
        // Arrange
        String key = "user_special_token";
        String refToken = "token!@#$%^&*()_+-=[]{}|;':\",./<>?`~";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle multiple tokens for different users")
    void testMultipleTokens_DifferentUsers_StoresIndependently() {
        // Arrange
        String key1 = "user1";
        String token1 = "token1";
        String key2 = "user2";
        String token2 = "token2";
        String key3 = "user3";
        String token3 = "token3";

        // Act
        sessionManager.storeRefToken(key1, token1);
        sessionManager.storeRefToken(key2, token2);
        sessionManager.storeRefToken(key3, token3);

        // Assert
        assertEquals(token1, sessionManager.getRefToken(key1));
        assertEquals(token2, sessionManager.getRefToken(key2));
        assertEquals(token3, sessionManager.getRefToken(key3));
        assertEquals(3, keyValueStore.size());
    }

    @Test
    @DisplayName("Should handle null token value")
    void testStoreRefToken_NullToken_StoresNull() {
        // Arrange
        String key = "user_null_token";
        String refToken = null;

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Should handle whitespace in key")
    void testStoreRefToken_WhitespaceKey_StoresSuccessfully() {
        // Arrange
        String key = "   user with spaces   ";
        String refToken = "token_whitespace_key";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle whitespace in token")
    void testStoreRefToken_WhitespaceToken_StoresSuccessfully() {
        // Arrange
        String key = "user_whitespace_token";
        String refToken = "   token with spaces   ";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle Unicode characters in key")
    void testStoreRefToken_UnicodeKey_StoresSuccessfully() {
        // Arrange
        String key = "用户123";
        String refToken = "token_unicode_key";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle Unicode characters in token")
    void testStoreRefToken_UnicodeToken_StoresSuccessfully() {
        // Arrange
        String key = "user_unicode_token";
        String refToken = "令牌_token_🔑";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
    }

    @Test
    @DisplayName("Should handle rapid successive store operations")
    void testStoreRefToken_RapidSuccessiveStores_HandlesCorrectly() {
        // Arrange
        String key = "rapid_user";

        // Act
        for (int i = 0; i < 100; i++) {
            sessionManager.storeRefToken(key, "token_" + i);
        }
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals("token_99", result);
    }

    @Test
    @DisplayName("Should maintain token isolation between different keys")
    void testTokenIsolation_DifferentKeys_MaintainsIsolation() {
        // Arrange
        String key1 = "user_a";
        String key2 = "user_b";
        String token1 = "token_a";
        String token2 = "token_b";

        // Act
        sessionManager.storeRefToken(key1, token1);
        sessionManager.storeRefToken(key2, token2);

        // Modify one token
        sessionManager.storeRefToken(key1, "modified_token_a");

        // Assert
        assertEquals("modified_token_a", sessionManager.getRefToken(key1));
        assertEquals(token2, sessionManager.getRefToken(key2)); // Should remain unchanged
    }

    @Test
    @DisplayName("Should handle newline characters in token")
    void testStoreRefToken_NewlineInToken_StoresSuccessfully() {
        // Arrange
        String key = "user_newline";
        String refToken = "token\nwith\nnewlines";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
        assertTrue(result.contains("\n"));
    }

    @Test
    @DisplayName("Should handle tab characters in token")
    void testStoreRefToken_TabInToken_StoresSuccessfully() {
        // Arrange
        String key = "user_tab";
        String refToken = "token\twith\ttabs";

        // Act
        sessionManager.storeRefToken(key, refToken);
        String result = sessionManager.getRefToken(key);

        // Assert
        assertEquals(refToken, result);
        assertTrue(result.contains("\t"));
    }
}

