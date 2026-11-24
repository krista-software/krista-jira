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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Issue, Label, TicketPriority, TicketProject, TicketStatus, IssueType, Version entities.
 */
class SmallEntitiesTest {

    // ========== Issue ==========

    @Test
    void testIssue_ToFields_AllFields() {
        Issue issue = new Issue();
        issue.summary = "Test issue";
        issue.comments = List.of("comment1", "comment2");
        issue.reporterName = "John Doe";
        issue.assigneeName = "Jane Smith";
        issue.key = "TEST-123";
        issue.id = "10001";
        issue.parentKey = "TEST-100";
        issue.projectKey = "TEST";
        issue.description = "A test description";
        issue.issueType = "Bug";
        issue.priority = "High";
        issue.status = "Open";
        issue.labels = List.of("backend", "urgent");
        issue.components = List.of("API", "Auth");
        issue.createdDate = 1700000000000L;
        issue.lastModifiedDate = 1700100000000L;

        Map<String, Object> result = issue.toFields();

        assertEquals(16, result.size());
        assertEquals("Test issue", result.get("Summary"));
        assertEquals(List.of("comment1", "comment2"), result.get("Comments"));
        assertEquals("John Doe", result.get("Reporter Name"));
        assertEquals("Jane Smith", result.get("Assignee Name"));
        assertEquals("TEST-123", result.get("Key"));
        assertEquals("10001", result.get("Id"));
        assertEquals("TEST-100", result.get("Parent Key"));
        assertEquals("TEST", result.get("Project Key"));
        assertEquals("A test description", result.get("Description"));
        assertEquals("Bug", result.get("Issue Type"));
        assertEquals("High", result.get("Priority"));
        assertEquals("Open", result.get("Status"));
        assertEquals(List.of("backend", "urgent"), result.get("Labels"));
        assertEquals(List.of("API", "Auth"), result.get("Components"));
        assertEquals(1700000000000L, result.get("Created Date"));
        assertEquals(1700100000000L, result.get("Last Modified Date"));
    }

    @Test
    void testIssue_ToFields_NullValues() {
        Issue issue = new Issue();
        Map<String, Object> result = issue.toFields();
        assertEquals(16, result.size());
        assertNull(result.get("Summary"));
        assertNull(result.get("Key"));
        assertNull(result.get("Created Date"));
    }

    // ========== Label ==========

    @Test
    void testLabel_ToFields() {
        Label label = new Label("backend");
        Map<String, Object> result = label.toFields();
        assertEquals(1, result.size());
        assertEquals("backend", result.get("Name"));
    }

    @Test
    void testLabel_NullName() {
        Label label = new Label(null);
        Map<String, Object> result = label.toFields();
        assertNull(result.get("Name"));
    }

    // ========== TicketPriority ==========

    @Test
    void testTicketPriority_ToFields() {
        TicketPriority tp = new TicketPriority("High");
        tp.id = "1";
        tp.description = "Critical priority";

        Map<String, Object> result = tp.toFields();

        assertEquals(3, result.size());
        assertEquals("1", result.get("Id"));
        assertEquals("High", result.get("Name"));
        assertEquals("Critical priority", result.get("Description"));
    }

    @Test
    void testTicketPriority_DefaultConstructor() {
        TicketPriority tp = new TicketPriority();
        Map<String, Object> result = tp.toFields();
        assertEquals(3, result.size());
        assertNull(result.get("Id"));
        assertNull(result.get("Name"));
    }

    // ========== TicketProject ==========

    @Test
    void testTicketProject_ToFields() {
        TicketProject tp = new TicketProject();
        tp.leadAccountId = "5b10a2844c20165700ede21g";
        tp.type = "software";
        tp.projectId = "10001";
        tp.style = "classic";
        tp.assigneeType = "PROJECT_LEAD";
        tp.key = "TEST";
        tp.name = "Test Project";
        tp.leadName = "John Lead";

        Map<String, Object> result = tp.toFields();

        assertEquals(8, result.size());
        assertEquals("5b10a2844c20165700ede21g", result.get("Lead Account Id"));
        assertEquals("software", result.get("Type"));
        assertEquals("10001", result.get("Project Id"));
        assertEquals("classic", result.get("Style"));
        assertEquals("PROJECT_LEAD", result.get("Assignee Type"));
        assertEquals("TEST", result.get("Key"));
        assertEquals("Test Project", result.get("Name"));
        assertEquals("John Lead", result.get("Lead Name"));
    }

    @Test
    void testTicketProject_ToFields_NullValues() {
        TicketProject tp = new TicketProject();
        Map<String, Object> result = tp.toFields();
        assertEquals(8, result.size());
        assertNull(result.get("Key"));
        assertNull(result.get("Name"));
    }

    // ========== TicketStatus ==========

    @Test
    void testTicketStatus_ToFields() {
        TicketStatus ts = new TicketStatus();
        ts.id = "1";
        ts.name = "Open";
        ts.description = "Issue is open";

        Map<String, Object> result = ts.toFields();

        assertEquals(3, result.size());
        assertEquals("1", result.get("Id"));
        assertEquals("Open", result.get("Name"));
        assertEquals("Issue is open", result.get("Description"));
    }

    @Test
    void testTicketStatus_ToFields_NullValues() {
        TicketStatus ts = new TicketStatus();
        Map<String, Object> result = ts.toFields();
        assertEquals(3, result.size());
        assertNull(result.get("Name"));
    }

    // ========== IssueType ==========

    @Test
    void testIssueType_ToFields() {
        IssueType it = new IssueType();
        it.subTask = "false";
        it.id = "10001";
        it.name = "Bug";
        it.description = "A bug in the software";

        Map<String, Object> result = it.toFields();

        assertEquals(4, result.size());
        assertEquals("false", result.get("Sub Task"));
        assertEquals("10001", result.get("Id"));
        assertEquals("Bug", result.get("Name"));
        assertEquals("A bug in the software", result.get("Description"));
    }

    @Test
    void testIssueType_ToFields_NullValues() {
        IssueType it = new IssueType();
        Map<String, Object> result = it.toFields();
        assertEquals(4, result.size());
        assertNull(result.get("Name"));
    }

    // ========== Version ==========

    @Test
    void testVersion_ToFields() {
        Version v = new Version();
        v.id = "10000";
        v.name = "v1.0";
        v.description = "First release";
        v.projectId = 10001.0;
        v.released = true;

        Map<String, Object> result = v.toFields();

        assertEquals(5, result.size());
        assertEquals("10000", result.get("Id"));
        assertEquals("v1.0", result.get("Name"));
        assertEquals("First release", result.get("Description"));
        assertEquals(10001.0, result.get("Project Id"));
        assertEquals(true, result.get("Released"));
    }

    @Test
    void testVersion_ToFields_NullValues() {
        Version v = new Version();
        Map<String, Object> result = v.toFields();
        assertEquals(5, result.size());
        assertNull(result.get("Name"));
        assertNull(result.get("Released"));
    }
}
