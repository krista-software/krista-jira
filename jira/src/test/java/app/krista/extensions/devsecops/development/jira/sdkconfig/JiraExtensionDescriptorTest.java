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

import app.krista.sdk.atlassian.registry.AtlassianExtensionDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 100% coverage tests for {@link JiraExtensionDescriptor}.
 */
@DisplayName("JiraExtensionDescriptor Tests")
class JiraExtensionDescriptorTest {

    private JiraExtensionDescriptor descriptor;

    @BeforeEach
    void setUp() {
        descriptor = new JiraExtensionDescriptor();
    }

    // ── Interface implementation ─────────────────────────────────────────────

    @Test
    @DisplayName("Should implement AtlassianExtensionDescriptor")
    void testImplementsAtlassianExtensionDescriptor() {
        assertTrue(descriptor instanceof AtlassianExtensionDescriptor);
    }

    // ── getConfigClass ───────────────────────────────────────────────────────

    @Test
    @DisplayName("getConfigClass returns JiraExtensionConfig.class")
    void testGetConfigClass_ReturnsJiraExtensionConfig() {
        Class<?> result = descriptor.getConfigClass();
        assertEquals(JiraExtensionConfig.class, result);
    }

    @Test
    @DisplayName("getConfigClass returns non-null value")
    void testGetConfigClass_NonNull() {
        assertNotNull(descriptor.getConfigClass());
    }

    @Test
    @DisplayName("getConfigClass is stable across multiple calls")
    void testGetConfigClass_StableAcrossMultipleCalls() {
        Class<?> first = descriptor.getConfigClass();
        Class<?> second = descriptor.getConfigClass();
        assertSame(first, second);
    }

    // ── Instantiation ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Can instantiate JiraExtensionDescriptor with no-arg constructor")
    void testInstantiation_NoArgConstructor() {
        JiraExtensionDescriptor newDescriptor = new JiraExtensionDescriptor();
        assertNotNull(newDescriptor);
    }

    @Test
    @DisplayName("Two instances return the same config class")
    void testTwoInstances_ReturnSameConfigClass() {
        JiraExtensionDescriptor d1 = new JiraExtensionDescriptor();
        JiraExtensionDescriptor d2 = new JiraExtensionDescriptor();
        assertSame(d1.getConfigClass(), d2.getConfigClass());
    }
}
