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

import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for JiraProject class.
 * Tests Jira project creation and conversion to TicketProject entity.
 */
@DisplayName("JiraProject Tests")
class JiraProjectTest {

    @Test
    @DisplayName("Should create JiraProject with all fields")
    void testConstructor_AllFields() {
        // Arrange
        JsonObject lead = new JsonObject();
        lead.addProperty("accountId", "lead-123");
        lead.addProperty("displayName", "Project Lead");

        // Act
        JiraProject project = new JiraProject(
            "Test Project",
            "software",
            "10001",
            "TEST",
            "classic",
            "PROJECT_LEAD",
            lead
        );

        // Assert
        assertEquals("Test Project", project.name);
        assertEquals("software", project.type);
        assertEquals("10001", project.id);
        assertEquals("TEST", project.key);
        assertEquals("classic", project.style);
        assertEquals("PROJECT_LEAD", project.assigneeType);
        assertNotNull(project.lead);
    }

    @Test
    @DisplayName("Should create JiraProject from JsonObject with all fields")
    void testCreate_AllFields() {
        // Arrange
        JsonObject leadObject = new JsonObject();
        leadObject.addProperty("accountId", "lead-456");
        leadObject.addProperty("displayName", "Jane Doe");

        JsonObject projectObject = new JsonObject();
        projectObject.addProperty("name", "My Project");
        projectObject.addProperty("projectTypeKey", "business");
        projectObject.addProperty("id", "10002");
        projectObject.addProperty("key", "MYPROJ");
        projectObject.addProperty("style", "next-gen");
        projectObject.addProperty("assigneeType", "UNASSIGNED");
        projectObject.add("lead", leadObject);

        // Act
        JiraProject project = JiraProject.create(projectObject);

        // Assert
        assertNotNull(project);
        assertEquals("My Project", project.name);
        assertEquals("business", project.type);
        assertEquals("10002", project.id);
        assertEquals("MYPROJ", project.key);
        assertEquals("next-gen", project.style);
        assertEquals("UNASSIGNED", project.assigneeType);
        assertNotNull(project.lead);
    }

    @Test
    @DisplayName("Should create JiraProject from JsonObject without assigneeType")
    void testCreate_WithoutAssigneeType() {
        // Arrange
        JsonObject leadObject = new JsonObject();
        leadObject.addProperty("accountId", "lead-789");
        leadObject.addProperty("displayName", "John Smith");

        JsonObject projectObject = new JsonObject();
        projectObject.addProperty("name", "Another Project");
        projectObject.addProperty("projectTypeKey", "software");
        projectObject.addProperty("id", "10003");
        projectObject.addProperty("key", "ANOTHER");
        projectObject.addProperty("style", "classic");
        projectObject.add("lead", leadObject);

        // Act
        JiraProject project = JiraProject.create(projectObject);

        // Assert
        assertNotNull(project);
        assertEquals("NA", project.assigneeType);
    }

    @Test
    @DisplayName("Should convert JiraProject to TicketProject entity")
    void testGetProject_AllFields() {
        // Arrange
        JsonObject leadObject = new JsonObject();
        leadObject.addProperty("accountId", "lead-111");
        leadObject.addProperty("displayName", "Lead Name");

        JiraProject jiraProject = new JiraProject(
            "Sample Project",
            "software",
            "10004",
            "SAMPLE",
            "classic",
            "PROJECT_LEAD",
            leadObject
        );

        // Act
        TicketProject ticketProject = jiraProject.getProject();

        // Assert
        assertNotNull(ticketProject);
        assertEquals("10004", ticketProject.projectId);
        assertEquals("SAMPLE", ticketProject.key);
        assertEquals("Sample Project", ticketProject.name);
        assertEquals("software", ticketProject.type);
        assertEquals("PROJECT_LEAD", ticketProject.assigneeType);
        assertEquals("classic", ticketProject.style);
        assertEquals("lead-111", ticketProject.leadAccountId);
        assertEquals("Lead Name", ticketProject.leadName);
    }

    @Test
    @DisplayName("Should handle null lead when converting to TicketProject")
    void testGetProject_NullLead() {
        // Arrange
        JiraProject jiraProject = new JiraProject(
            "No Lead Project",
            "software",
            "10005",
            "NOLEAD",
            "classic",
            "UNASSIGNED",
            null
        );

        // Act
        TicketProject ticketProject = jiraProject.getProject();

        // Assert
        assertNotNull(ticketProject);
        assertEquals("NA", ticketProject.leadAccountId);
        assertEquals("NA", ticketProject.leadName);
    }

    @Test
    @DisplayName("Should handle lead without displayName")
    void testGetProject_LeadWithoutDisplayName() {
        // Arrange
        JsonObject leadObject = new JsonObject();
        leadObject.addProperty("accountId", "lead-222");

        JiraProject jiraProject = new JiraProject(
            "Project",
            "software",
            "10006",
            "PROJ",
            "classic",
            "PROJECT_LEAD",
            leadObject
        );

        // Act
        TicketProject ticketProject = jiraProject.getProject();

        // Assert
        assertNotNull(ticketProject);
        assertEquals("lead-222", ticketProject.leadAccountId);
        assertEquals("NA", ticketProject.leadName);
    }

    @Test
    @DisplayName("Should handle lead without accountId")
    void testGetProject_LeadWithoutAccountId() {
        // Arrange
        JsonObject leadObject = new JsonObject();
        leadObject.addProperty("displayName", "Lead Name");

        JiraProject jiraProject = new JiraProject(
            "Project",
            "software",
            "10007",
            "PROJ2",
            "classic",
            "PROJECT_LEAD",
            leadObject
        );

        // Act
        TicketProject ticketProject = jiraProject.getProject();

        // Assert
        assertNotNull(ticketProject);
        assertEquals("NA", ticketProject.leadAccountId);
        assertEquals("Lead Name", ticketProject.leadName);
    }

    @Test
    @DisplayName("Should handle different project types")
    void testCreate_DifferentProjectTypes() {
        // Arrange
        String[] projectTypes = {"software", "business", "service_desk"};

        for (String projectType : projectTypes) {
            JsonObject leadObject = new JsonObject();
            leadObject.addProperty("accountId", "lead-" + projectType);
            leadObject.addProperty("displayName", "Lead");

            JsonObject projectObject = new JsonObject();
            projectObject.addProperty("name", "Project");
            projectObject.addProperty("projectTypeKey", projectType);
            projectObject.addProperty("id", "10000");
            projectObject.addProperty("key", "KEY");
            projectObject.addProperty("style", "classic");
            projectObject.add("lead", leadObject);

            // Act
            JiraProject project = JiraProject.create(projectObject);

            // Assert
            assertEquals(projectType, project.type);
        }
    }

    @Test
    @DisplayName("Should handle different project styles")
    void testCreate_DifferentStyles() {
        // Arrange
        String[] styles = {"classic", "next-gen"};

        for (String style : styles) {
            JsonObject leadObject = new JsonObject();
            leadObject.addProperty("accountId", "lead-123");
            leadObject.addProperty("displayName", "Lead");

            JsonObject projectObject = new JsonObject();
            projectObject.addProperty("name", "Project");
            projectObject.addProperty("projectTypeKey", "software");
            projectObject.addProperty("id", "10000");
            projectObject.addProperty("key", "KEY");
            projectObject.addProperty("style", style);
            projectObject.add("lead", leadObject);

            // Act
            JiraProject project = JiraProject.create(projectObject);

            // Assert
            assertEquals(style, project.style);
        }
    }

    @Test
    @DisplayName("Should handle special characters in project name")
    void testCreate_SpecialCharactersInName() {
        // Arrange
        JsonObject leadObject = new JsonObject();
        leadObject.addProperty("accountId", "lead-333");
        leadObject.addProperty("displayName", "Lead");

        JsonObject projectObject = new JsonObject();
        projectObject.addProperty("name", "Project & Development (2024)");
        projectObject.addProperty("projectTypeKey", "software");
        projectObject.addProperty("id", "10008");
        projectObject.addProperty("key", "PD24");
        projectObject.addProperty("style", "classic");
        projectObject.add("lead", leadObject);

        // Act
        JiraProject project = JiraProject.create(projectObject);

        // Assert
        assertEquals("Project & Development (2024)", project.name);
    }

    @Test
    @DisplayName("Should create multiple JiraProject instances independently")
    void testCreate_MultipleInstances() {
        // Arrange
        JsonObject lead1 = new JsonObject();
        lead1.addProperty("accountId", "lead-1");
        lead1.addProperty("displayName", "Lead 1");

        JsonObject project1 = new JsonObject();
        project1.addProperty("name", "Project 1");
        project1.addProperty("projectTypeKey", "software");
        project1.addProperty("id", "10001");
        project1.addProperty("key", "PROJ1");
        project1.addProperty("style", "classic");
        project1.add("lead", lead1);

        JsonObject lead2 = new JsonObject();
        lead2.addProperty("accountId", "lead-2");
        lead2.addProperty("displayName", "Lead 2");

        JsonObject project2 = new JsonObject();
        project2.addProperty("name", "Project 2");
        project2.addProperty("projectTypeKey", "business");
        project2.addProperty("id", "10002");
        project2.addProperty("key", "PROJ2");
        project2.addProperty("style", "next-gen");
        project2.add("lead", lead2);

        // Act
        JiraProject jiraProject1 = JiraProject.create(project1);
        JiraProject jiraProject2 = JiraProject.create(project2);

        // Assert
        assertNotEquals(jiraProject1.id, jiraProject2.id);
        assertNotEquals(jiraProject1.key, jiraProject2.key);
        assertNotEquals(jiraProject1.type, jiraProject2.type);
    }
}

