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

package app.krista.extensions.devsecops.development.jira.sdkconfig;

import app.krista.sdk.atlassian.config.AtlassianProduct;
import app.krista.sdk.atlassian.registry.annotations.AtlassianExtension;
import app.krista.sdk.atlassian.registry.annotations.OAuthScope;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 100% coverage tests for {@link JiraExtensionConfig}.
 *
 * <p>This is a pure marker/annotation class; tests verify that the annotations
 * carry the expected metadata consumed by the atlassian-sdk extension registry.</p>
 */
@DisplayName("JiraExtensionConfig Tests")
class JiraExtensionConfigTest {

    // ── @AtlassianExtension ─────────────────────────────────────────────────

    @Test
    @DisplayName("Should have @AtlassianExtension annotation")
    void testHasAtlassianExtensionAnnotation() {
        assertTrue(JiraExtensionConfig.class.isAnnotationPresent(AtlassianExtension.class));
    }

    @Test
    @DisplayName("@AtlassianExtension.extensionType should be 'jira'")
    void testAtlassianExtension_ExtensionType() {
        AtlassianExtension ann = JiraExtensionConfig.class.getAnnotation(AtlassianExtension.class);
        assertEquals("jira", ann.extensionType());
    }

    @Test
    @DisplayName("@AtlassianExtension.product should be JIRA")
    void testAtlassianExtension_Product() {
        AtlassianExtension ann = JiraExtensionConfig.class.getAnnotation(AtlassianExtension.class);
        assertEquals(AtlassianProduct.JIRA, ann.product());
    }

    @Test
    @DisplayName("@AtlassianExtension.jaxrsId should be 'jiraextn'")
    void testAtlassianExtension_JaxrsId() {
        AtlassianExtension ann = JiraExtensionConfig.class.getAnnotation(AtlassianExtension.class);
        assertEquals("jiraextn", ann.jaxrsId());
    }

    @Test
    @DisplayName("@AtlassianExtension.displayName should be 'Jira Project Management'")
    void testAtlassianExtension_DisplayName() {
        AtlassianExtension ann = JiraExtensionConfig.class.getAnnotation(AtlassianExtension.class);
        assertEquals("Jira Project Management", ann.displayName());
    }

    // ── @OAuthScope annotations ─────────────────────────────────────────────

    @Test
    @DisplayName("Should have exactly 7 @OAuthScope annotations")
    void testHasSevenOAuthScopeAnnotations() {
        OAuthScope[] scopes = JiraExtensionConfig.class.getAnnotationsByType(OAuthScope.class);
        assertEquals(7, scopes.length);
    }

    @Test
    @DisplayName("Should have 'read:jira-user' scope")
    void testHasReadJiraUserScope() {
        boolean found = hasScopeWithValue("read:jira-user");
        assertTrue(found, "Expected OAuthScope with value 'read:jira-user'");
    }

    @Test
    @DisplayName("Should have 'read:jira-work' scope")
    void testHasReadJiraWorkScope() {
        assertTrue(hasScopeWithValue("read:jira-work"));
    }

    @Test
    @DisplayName("Should have 'write:jira-work' scope")
    void testHasWriteJiraWorkScope() {
        assertTrue(hasScopeWithValue("write:jira-work"));
    }

    @Test
    @DisplayName("Should have 'manage:jira-data-provider' scope")
    void testHasManageJiraDataProviderScope() {
        assertTrue(hasScopeWithValue("manage:jira-data-provider"));
    }

    @Test
    @DisplayName("Should have 'manage:jira-project' scope")
    void testHasManageJiraProjectScope() {
        assertTrue(hasScopeWithValue("manage:jira-project"));
    }

    @Test
    @DisplayName("'read:jira-user' scope has correct description")
    void testReadJiraUserScope_Description() {
        OAuthScope scope = getScopeByValue("read:jira-user");
        assertNotNull(scope);
        assertEquals("Read Jira user information", scope.description());
    }

    @Test
    @DisplayName("'read:jira-user' scope contains expected catalogRequests")
    void testReadJiraUserScope_CatalogRequests() {
        OAuthScope scope = getScopeByValue("read:jira-user");
        assertNotNull(scope);
        assertArrayContains(scope.catalogRequests(), "Get Users Account Id");
        assertArrayContains(scope.catalogRequests(), "Get User Account Information");
    }

    @Test
    @DisplayName("'read:jira-work' scope contains expected catalogRequests")
    void testReadJiraWorkScope_CatalogRequests() {
        OAuthScope scope = getScopeByValue("read:jira-work");
        assertNotNull(scope);
        assertArrayContains(scope.catalogRequests(), "Get Issue By Key");
        assertArrayContains(scope.catalogRequests(), "Fetch by JQL query");
    }

    @Test
    @DisplayName("'write:jira-work' scope contains expected catalogRequests")
    void testWriteJiraWorkScope_CatalogRequests() {
        OAuthScope scope = getScopeByValue("write:jira-work");
        assertNotNull(scope);
        assertArrayContains(scope.catalogRequests(), "Create an issue");
        assertArrayContains(scope.catalogRequests(), "Report a bug");
        assertArrayContains(scope.catalogRequests(), "Attach File to Ticket");
    }

    @Test
    @DisplayName("'manage:jira-project' scope contains expected catalogRequests")
    void testManageJiraProjectScope_CatalogRequests() {
        OAuthScope scope = getScopeByValue("manage:jira-project");
        assertNotNull(scope);
        assertArrayContains(scope.catalogRequests(), "Get release summary");
        assertArrayContains(scope.catalogRequests(), "Assign Issue to Epic");
    }

    // ── Private constructor ─────────────────────────────────────────────────

    @Test
    @DisplayName("Private constructor should be inaccessible (utility/marker class)")
    void testPrivateConstructor() throws Exception {
        Constructor<JiraExtensionConfig> ctor = JiraExtensionConfig.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(ctor.getModifiers()));
    }

    @Test
    @DisplayName("Private constructor can be invoked via reflection (coverage)")
    void testPrivateConstructor_InvokeViaReflection() throws Exception {
        Constructor<JiraExtensionConfig> ctor = JiraExtensionConfig.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        JiraExtensionConfig instance = ctor.newInstance();
        assertNotNull(instance);
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private boolean hasScopeWithValue(String value) {
        for (OAuthScope scope : JiraExtensionConfig.class.getAnnotationsByType(OAuthScope.class)) {
            if (value.equals(scope.value())) return true;
        }
        return false;
    }

    private OAuthScope getScopeByValue(String value) {
        for (OAuthScope scope : JiraExtensionConfig.class.getAnnotationsByType(OAuthScope.class)) {
            if (value.equals(scope.value())) return scope;
        }
        return null;
    }

    private void assertArrayContains(String[] array, String expected) {
        for (String item : array) {
            if (expected.equals(item)) return;
        }
        fail("Expected '" + expected + "' not found in array");
    }
}
