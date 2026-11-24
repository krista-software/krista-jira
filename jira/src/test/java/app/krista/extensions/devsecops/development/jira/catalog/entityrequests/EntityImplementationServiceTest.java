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

package app.krista.extensions.devsecops.development.jira.catalog.entityrequests;

import app.krista.extension.executor.SearchCondition;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.services.JiraUser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for non-abstract methods in EntityImplementationService.
 */
class EntityImplementationServiceTest {

    // Concrete subclass to test the abstract class's non-abstract methods
    private static class TestableService extends EntityImplementationService {
        TestableService(IssueDao issueDao) { super(issueDao); }
        TestableService() { super(mock(IssueDao.class)); }

        @Override public app.krista.model.base.EntityValue create(app.krista.model.entity.EntityType e, Map<String, Object> m) { return null; }
        @Override public app.krista.model.base.EntityValue update(app.krista.model.entity.EntityType e, app.krista.model.base.EntityValue v) { return null; }
        @Override public List<app.krista.model.base.EntityValue> search(app.krista.model.entity.EntityType e, app.krista.extension.executor.SearchQuery q, Long p, Integer s) { return null; }
        @Override public app.krista.model.base.EntityValue get(app.krista.model.entity.EntityType e, String pk) { return null; }
    }

    // ========== getConditionValue ==========

    @Test
    void testGetConditionValue_Found() {
        TestableService service = new TestableService();
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "TEST-123", "Project Key"),
                new SearchCondition(SearchCondition.Operator.contains, "login bug", "Summary")
        );
        assertEquals("TEST-123", service.getConditionValue(conditions, "Project Key"));
        assertEquals("login bug", service.getConditionValue(conditions, "Summary"));
    }

    @Test
    void testGetConditionValue_CaseInsensitive() {
        TestableService service = new TestableService();
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "HIGH", "Priority")
        );
        assertEquals("HIGH", service.getConditionValue(conditions, "priority"));
    }

    @Test
    void testGetConditionValue_NotFound() {
        TestableService service = new TestableService();
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, "val", "Name")
        );
        assertEquals("", service.getConditionValue(conditions, "Missing Field"));
    }

    @Test
    void testGetConditionValue_NullOperand() {
        TestableService service = new TestableService();
        List<SearchCondition> conditions = List.of(
                new SearchCondition(SearchCondition.Operator.equals, null, "Summary")
        );
        assertEquals("", service.getConditionValue(conditions, "Summary"));
    }

    @Test
    void testGetConditionValue_EmptyList() {
        TestableService service = new TestableService();
        assertEquals("", service.getConditionValue(new ArrayList<>(), "Summary"));
    }

    // ========== validateInputs ==========

    @Test
    void testValidateInputs_AllPresent() {
        TestableService service = new TestableService();
        Map<String, Object> fields = new HashMap<>();
        fields.put("Summary", "Test");
        fields.put("Project Key", "PROJ");
        assertDoesNotThrow(() -> service.validateInputs(fields, List.of("Summary", "Project Key")));
    }

    @Test
    void testValidateInputs_MissingKey_Throws() {
        TestableService service = new TestableService();
        Map<String, Object> fields = new HashMap<>();
        fields.put("Summary", "Test");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validateInputs(fields, List.of("Summary", "Project Key")));
        assertTrue(ex.getMessage().contains("Project Key"));
    }

    @Test
    void testValidateInputs_EmptyMap_Throws() {
        TestableService service = new TestableService();
        assertThrows(IllegalArgumentException.class,
                () -> service.validateInputs(new HashMap<>(), List.of("Summary")));
    }

    // ========== resolveUserConditions ==========

    @Test
    void testResolveUserConditions_NullConditions() {
        TestableService service = new TestableService();
        assertNull(service.resolveUserConditions(null));
    }

    @Test
    void testResolveUserConditions_NonUserField_PassesThrough() {
        TestableService service = new TestableService();
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "login", "Summary");
        List<SearchCondition> result = service.resolveUserConditions(List.of(sc));
        assertEquals(1, result.size());
        assertEquals("Summary", result.get(0).getFieldName());
        assertEquals("login", result.get(0).getOperand());
    }

    // ========== DAO getters ==========

    @Test
    void testGetIssueService_ReturnsDao() {
        IssueDao mockDao = mock(IssueDao.class);
        TestableService service = new TestableService(mockDao);
        assertSame(mockDao, service.getIssueService());
    }

    @Test
    void testGetProjectService_NullByDefault() {
        TestableService service = new TestableService();
        assertNull(service.getProjectService());
    }

    @Test
    void testGetTicketInfoService_NullByDefault() {
        TestableService service = new TestableService();
        assertNull(service.getTicketInfoService());
    }

    // ========== searchBySummary ==========

    @Test
    void testSearchBySummary_WithText() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(contains("summary ~ \"login\""), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        List<Issue> result = service.searchBySummary("login", 20, 1);
        assertNotNull(result);
        verify(mockDao).searchByJQL(contains("summary ~ \"login\""), eq(20L), eq(1L));
    }

    @Test
    void testSearchBySummary_NullText_FallsBackToCurrentUser() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(contains("assignee=currentUser()"), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        List<Issue> result = service.searchBySummary(null, 20, 1);
        assertNotNull(result);
        verify(mockDao).searchByJQL(contains("assignee=currentUser()"), eq(20L), eq(1L));
    }

    @Test
    void testSearchBySummary_BlankText_FallsBackToCurrentUser() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchBySummary("  ", 20, 1);
        verify(mockDao).searchByJQL(contains("assignee=currentUser()"), eq(20L), eq(1L));
    }

    // ========== searchBySummaryIssueType ==========

    @Test
    void testSearchBySummaryIssueType_WithBoth() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchBySummaryIssueType("login", "Bug", 20, 1);
        verify(mockDao).searchByJQL(contains("summary ~ \"login\" AND issuetype = Bug"), eq(20L), eq(1L));
    }

    @Test
    void testSearchBySummaryIssueType_NullIssueType_DelegatesToSearchBySummary() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchBySummaryIssueType("login", null, 20, 1);
        verify(mockDao).searchByJQL(contains("summary ~ \"login\""), eq(20L), eq(1L));
    }

    @Test
    void testSearchBySummaryIssueType_NullText_IssueTypeOnly() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchBySummaryIssueType(null, "Bug", 20, 1);
        verify(mockDao).searchByJQL(contains("issuetype = Bug"), eq(20L), eq(1L));
    }

    // ========== searchByTextIssueType ==========

    @Test
    void testSearchByTextIssueType() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchByTextIssueType("error", "Bug", 20, 1);
        verify(mockDao).searchByJQL(contains("text ~ \"error\" AND issuetype = Bug"), eq(20L), eq(1L));
    }

    // ========== searchBySummaryIssueTypeProjectKey ==========

    @Test
    void testSearchBySummaryIssueTypeProjectKey() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchBySummaryIssueTypeProjectKey("TEST", "login", "Bug", 20, 1);
        verify(mockDao).searchByJQL(argThat(jql ->
                jql.contains("project = TEST") && jql.contains("issuetype = Bug") && jql.contains("summary ~ \"login\"")),
                eq(20L), eq(1L));
    }

    @Test
    void testSearchBySummaryIssueTypeProjectKey_NullValues() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchBySummaryIssueTypeProjectKey(null, null, null, 20, 1);
        verify(mockDao).searchByJQL(anyString(), eq(20L), eq(1L));
    }

    // ========== searchByTextIssueTypeProjectKey ==========

    @Test
    void testSearchByTextIssueTypeProjectKey() {
        IssueDao mockDao = mock(IssueDao.class);
        when(mockDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());
        TestableService service = new TestableService(mockDao);

        service.searchByTextIssueTypeProjectKey("TEST", "error", "Bug", 20, 1);
        verify(mockDao).searchByJQL(argThat(jql ->
                jql.contains("project = TEST") && jql.contains("issuetype = Bug") && jql.contains("text ~ \"error\"")),
                eq(20L), eq(1L));
    }

    // ========== resolveUserConditions — with mocked UserDao ==========

    @Test
    void testResolveUserConditions_SingleUserMatch() {
        IssueDao mockIssueDao = mock(IssueDao.class);
        UserDao mockUserDao = mock(UserDao.class);
        when(mockIssueDao.getUserDao()).thenReturn(mockUserDao);

        JiraUser user = new JiraUser("acc123", "John", "atlassian", null, null, null);
        when(mockUserDao.searchUser("John")).thenReturn(List.of(user));

        TestableService service = new TestableService(mockIssueDao);
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "John", "Assignee Name");
        List<SearchCondition> result = service.resolveUserConditions(List.of(sc));

        assertEquals(1, result.size());
        assertEquals(SearchCondition.Operator.equals, result.get(0).getOperator());
        assertEquals("acc123", result.get(0).getOperand());
    }

    @Test
    void testResolveUserConditions_MultipleUserMatches() {
        IssueDao mockIssueDao = mock(IssueDao.class);
        UserDao mockUserDao = mock(UserDao.class);
        when(mockIssueDao.getUserDao()).thenReturn(mockUserDao);

        JiraUser user1 = new JiraUser("acc1", "John A", "atlassian", null, null, null);
        JiraUser user2 = new JiraUser("acc2", "John B", "atlassian", null, null, null);
        when(mockUserDao.searchUser("John")).thenReturn(List.of(user1, user2));

        TestableService service = new TestableService(mockIssueDao);
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "John", "Reporter Name");
        List<SearchCondition> result = service.resolveUserConditions(List.of(sc));

        assertEquals(1, result.size());
        assertEquals(SearchCondition.Operator.contains, result.get(0).getOperator());
        assertTrue(result.get(0).getOperand().toString().contains("acc1|acc2"));
    }

    @Test
    void testResolveUserConditions_NoUserMatch_KeepsOriginal() {
        IssueDao mockIssueDao = mock(IssueDao.class);
        UserDao mockUserDao = mock(UserDao.class);
        when(mockIssueDao.getUserDao()).thenReturn(mockUserDao);
        when(mockUserDao.searchUser("Nobody")).thenReturn(Collections.emptyList());

        TestableService service = new TestableService(mockIssueDao);
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "Nobody", "Assignee Name");
        List<SearchCondition> result = service.resolveUserConditions(List.of(sc));

        assertEquals(1, result.size());
        assertEquals("Nobody", result.get(0).getOperand());
    }
    // Additional concrete subclasses for other constructors
    private static class TestableServiceWithTicketInfo extends EntityImplementationService {
        TestableServiceWithTicketInfo(IssueDao issueDao, TicketInfoDao ticketInfoDao) {
            super(issueDao, ticketInfoDao);
        }
        @Override public app.krista.model.base.EntityValue create(app.krista.model.entity.EntityType e, Map<String, Object> m) { return null; }
        @Override public app.krista.model.base.EntityValue update(app.krista.model.entity.EntityType e, app.krista.model.base.EntityValue v) { return null; }
        @Override public List<app.krista.model.base.EntityValue> search(app.krista.model.entity.EntityType e, app.krista.extension.executor.SearchQuery q, Long p, Integer s) { return null; }
        @Override public app.krista.model.base.EntityValue get(app.krista.model.entity.EntityType e, String pk) { return null; }
    }

    private static class TestableServiceWithProject extends EntityImplementationService {
        TestableServiceWithProject(ProjectDao projectDao) {
            super(projectDao);
        }
        @Override public app.krista.model.base.EntityValue create(app.krista.model.entity.EntityType e, Map<String, Object> m) { return null; }
        @Override public app.krista.model.base.EntityValue update(app.krista.model.entity.EntityType e, app.krista.model.base.EntityValue v) { return null; }
        @Override public List<app.krista.model.base.EntityValue> search(app.krista.model.entity.EntityType e, app.krista.extension.executor.SearchQuery q, Long p, Integer s) { return null; }
        @Override public app.krista.model.base.EntityValue get(app.krista.model.entity.EntityType e, String pk) { return null; }
    }

    private static class TestableServiceWithUser extends EntityImplementationService {
        TestableServiceWithUser(UserDao userDao) {
            super(userDao);
        }
        @Override public app.krista.model.base.EntityValue create(app.krista.model.entity.EntityType e, Map<String, Object> m) { return null; }
        @Override public app.krista.model.base.EntityValue update(app.krista.model.entity.EntityType e, app.krista.model.base.EntityValue v) { return null; }
        @Override public List<app.krista.model.base.EntityValue> search(app.krista.model.entity.EntityType e, app.krista.extension.executor.SearchQuery q, Long p, Integer s) { return null; }
        @Override public app.krista.model.base.EntityValue get(app.krista.model.entity.EntityType e, String pk) { return null; }
    }

    private static class TestableServiceTicketOnly extends EntityImplementationService {
        TestableServiceTicketOnly(TicketInfoDao ticketInfoDao) {
            super(ticketInfoDao);
        }
        @Override public app.krista.model.base.EntityValue create(app.krista.model.entity.EntityType e, Map<String, Object> m) { return null; }
        @Override public app.krista.model.base.EntityValue update(app.krista.model.entity.EntityType e, app.krista.model.base.EntityValue v) { return null; }
        @Override public List<app.krista.model.base.EntityValue> search(app.krista.model.entity.EntityType e, app.krista.extension.executor.SearchQuery q, Long p, Integer s) { return null; }
        @Override public app.krista.model.base.EntityValue get(app.krista.model.entity.EntityType e, String pk) { return null; }
    }

    // ========== Constructor coverage ==========

    @Test
    void testConstructor_IssueDaoAndTicketInfoDao() {
        IssueDao mockIssue = mock(IssueDao.class);
        TicketInfoDao mockTicket = mock(TicketInfoDao.class);
        TestableServiceWithTicketInfo service = new TestableServiceWithTicketInfo(mockIssue, mockTicket);
        assertSame(mockIssue, service.getIssueService());
        assertSame(mockTicket, service.getTicketInfoService());
    }

    @Test
    void testConstructor_ProjectDao() {
        ProjectDao mockProject = mock(ProjectDao.class);
        TestableServiceWithProject service = new TestableServiceWithProject(mockProject);
        assertSame(mockProject, service.getProjectService());
        assertNull(service.getIssueService());
    }

    @Test
    void testConstructor_UserDao() {
        UserDao mockUser = mock(UserDao.class);
        TestableServiceWithUser service = new TestableServiceWithUser(mockUser);
        assertSame(mockUser, service.getUserService());
        assertNull(service.getIssueService());
    }

    @Test
    void testConstructor_TicketInfoDaoOnly() {
        TicketInfoDao mockTicket = mock(TicketInfoDao.class);
        TestableServiceTicketOnly service = new TestableServiceTicketOnly(mockTicket);
        assertSame(mockTicket, service.getTicketInfoService());
        assertNull(service.getIssueService());
    }

    // ========== getIssuesByStatus ==========

    @Test
    void testGetIssuesByStatus_MatchFound_ReturnsIssues() {
        IssueDao mockIssueDao = mock(IssueDao.class);
        TicketInfoDao mockTicketDao = mock(TicketInfoDao.class);

        TicketStatus status = new TicketStatus();
        status.name = "In Progress";

        when(mockTicketDao.getStatuses()).thenReturn(List.of(status));
        when(mockIssueDao.searchByJQL(contains("status = \"In Progress\""), eq(30L), eq(1L)))
                .thenReturn(Collections.emptyList());

        TestableServiceWithTicketInfo service = new TestableServiceWithTicketInfo(mockIssueDao, mockTicketDao);
        List<Issue> result = service.getIssuesByStatus("In Progress");

        assertNotNull(result);
        verify(mockIssueDao).searchByJQL(contains("status = \"In Progress\""), eq(30L), eq(1L));
    }

    @Test
    void testGetIssuesByStatus_NoMatch_ThrowsRuntimeException() {
        IssueDao mockIssueDao = mock(IssueDao.class);
        TicketInfoDao mockTicketDao = mock(TicketInfoDao.class);
        when(mockTicketDao.getStatuses()).thenReturn(Collections.emptyList());

        TestableServiceWithTicketInfo service = new TestableServiceWithTicketInfo(mockIssueDao, mockTicketDao);
        assertThrows(RuntimeException.class, () -> service.getIssuesByStatus("Unknown Status"));
    }

    // ========== resolveUserConditions — null issueDao path ==========

    @Test
    void testResolveUserConditions_NullIssueDao_ReturnsConditionsUnchanged() {
        ProjectDao mockProject = mock(ProjectDao.class);
        TestableServiceWithProject service = new TestableServiceWithProject(mockProject);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "John", "Assignee Name");
        List<SearchCondition> input = List.of(sc);
        // issueDao is null -> resolveUserConditions early-returns
        List<SearchCondition> result = service.resolveUserConditions(input);
        assertSame(input, result);
    }

    @Test
    void testResolveUserConditions_UserField_EmptyOperand_KeepsOriginal() {
        IssueDao mockIssueDao = mock(IssueDao.class);
        UserDao mockUserDao = mock(UserDao.class);
        when(mockIssueDao.getUserDao()).thenReturn(mockUserDao);

        TestableService service = new TestableService(mockIssueDao);
        // Operand is blank whitespace -> nameQuery.trim() is "" -> !isEmpty() is false -> keep original
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "  ", "Assignee Name");
        List<SearchCondition> result = service.resolveUserConditions(List.of(sc));

        assertEquals(1, result.size());
        // Original condition kept
        assertSame(sc, result.get(0));
        verify(mockUserDao, never()).searchUser(anyString());
    }

}