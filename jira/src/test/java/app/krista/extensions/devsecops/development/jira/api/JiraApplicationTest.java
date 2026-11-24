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

package app.krista.extensions.devsecops.development.jira.api;

import app.krista.extensions.devsecops.development.jira.mcp.JiraMcpSetupResource;
import app.krista.sdk.atlassian.extension.authentication.AtlassianManagedCallbackResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JiraApplication Tests")
class JiraApplicationTest {

    @Test
    @DisplayName("getClasses returns all 6 required resource classes")
    void testGetClasses_ReturnsAllResources() {
        JiraApplication app = new JiraApplication();
        Set<Class<?>> classes = app.getClasses();

        assertNotNull(classes);
        assertEquals(6, classes.size());
        assertTrue(classes.contains(AuthenticationResource.class));
        assertTrue(classes.contains(ListenerResource.class));
        assertTrue(classes.contains(JiraAuthUiResource.class));
        assertTrue(classes.contains(JiraAuditResource.class));
        assertTrue(classes.contains(JiraMcpSetupResource.class));
        assertTrue(classes.contains(AtlassianManagedCallbackResource.class));
    }

    @Test
    @DisplayName("JiraApplication has @Service annotation")
    void testHasServiceAnnotation() {
        assertTrue(JiraApplication.class.isAnnotationPresent(org.jvnet.hk2.annotations.Service.class));
    }

    @Test
    @DisplayName("JiraApplication has @ApplicationPath annotation")
    void testHasApplicationPathAnnotation() {
        assertTrue(JiraApplication.class.isAnnotationPresent(javax.ws.rs.ApplicationPath.class));
    }
}
