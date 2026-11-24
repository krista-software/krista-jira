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

package app.krista.extensions.devsecops.development.jira.catalog.entityrequests.impl;

import app.krista.extension.executor.SearchCondition;
import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskImplTest {

    private IssueDao issueDao;
    private TaskImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        impl = new TaskImpl(issueDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Task");
    }

    private Issue createIssue(String key, String type) {
        Issue issue = new Issue();
        issue.key = key;
        issue.issueType = type;
        issue.summary = "Task summary";
        issue.status = "Open";
        issue.priority = "Medium";
        issue.assigneeName = "John";
        issue.reporterName = "Jane";
        issue.projectKey = "PROJ";
        issue.description = "Description";
        return issue;
    }

    @Test
    void create_basicTask_returnsEntityValue() {
        Issue created = createIssue("TASK-1", JiraConstants.TASK);
        when(issueDao.createIssue("PROJ", "Task title", "Task title", null, JiraConstants.TASK))
                .thenReturn(created);
        when(issueDao.getIssueFinal("TASK-1")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Task title");

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
    }

    @Test
    void create_withAllOptionalFields() {
        Issue created = createIssue("TASK-2", JiraConstants.TASK);
        when(issueDao.createIssue(any(), any(), any(), any(), any())).thenReturn(created);
        when(issueDao.getIssueFinal("TASK-2")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Task title");
        attrs.put(JiraConstants.DESCRIPTION, "Detailed description");
        attrs.put(JiraConstants.COMMENTS, List.of("note1"));
        attrs.put(JiraConstants.ASSIGNEE_NAME, "Alice");
        attrs.put(JiraConstants.PRIORITY, "High");
        attrs.put("Status", "In Progress");

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
        verify(issueDao).addComment(eq("TASK-2"), eq("note1"), any());
    }

    @Test
    void update_existingTask_returnsEntityValue() {
        Issue existing = createIssue("TASK-3", JiraConstants.TASK);
        when(issueDao.getIssueFinal("TASK-3")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Task Key", "TASK-3");
        fields.put(JiraConstants.SUMMARY, "Updated task");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "123");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Desc");
        fields.put("Task Type", "Task");
        fields.put(JiraConstants.PRIORITY, "Medium");
        fields.put(JiraConstants.STATUS, "Open");

        EntityValue entity = new EntityValue("Task", fields);
        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
    }

    @Test
    void update_nonExistingTask_throwsNoSuchElement() {
        when(issueDao.getIssueFinal("TASK-999")).thenReturn(null);
        Map<String, Object> fields = Map.of("Task Key", "TASK-999");
        assertThrows(NoSuchElementException.class,
                () -> impl.update(entityType, new EntityValue("Task", fields)));
    }

    @Test
    void search_byTaskKey_directLookup_found() {
        Issue issue = createIssue("TASK-4", JiraConstants.TASK);
        when(issueDao.getIssueFinal("TASK-4")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "TASK-4", "task key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byTaskKey_directLookup_wrongType() {
        Issue issue = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-1")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(null, "BUG-1", "task key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_byTaskKey_notFound() {
        when(issueDao.getIssueFinal("TASK-99")).thenReturn(null);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "TASK-99", "task key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_noConditions_returnsFromJQL() {
        Issue issue = createIssue("TASK-5", JiraConstants.TASK);
        when(issueDao.searchByJQL(any(), anyLong(), anyLong())).thenReturn(List.of(issue));

        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of());
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void get_existingTask_returnsEntityValue() {
        Issue issue = createIssue("TASK-6", JiraConstants.TASK);
        when(issueDao.getIssueFinal("TASK-6")).thenReturn(issue);

        EntityValue result = impl.get(entityType, "TASK-6");
        assertNotNull(result);
    }

    @Test
    void search_mixedResults_filtersNonTaskTypes() {
        Issue taskIssue = createIssue("TASK-8", JiraConstants.TASK);
        Issue bugIssue = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of(taskIssue, bugIssue));

        SearchCondition sc1 = new SearchCondition(SearchCondition.Operator.equals, "Open", "status");
        SearchCondition sc2 = new SearchCondition(SearchCondition.Operator.equals, "High", "priority");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc1, sc2));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void update_existingTask_withComments_coversCommentsBranch() {
        Issue existing = createIssue("TASK-7", JiraConstants.TASK);
        when(issueDao.getIssueFinal("TASK-7")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Task Key", "TASK-7");
        fields.put(JiraConstants.SUMMARY, "Updated task");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "99");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Desc");
        fields.put("Task Type", "Task");
        fields.put(JiraConstants.PRIORITY, "Low");
        fields.put(JiraConstants.STATUS, "Done");
        fields.put(JiraConstants.COMMENTS, "A task comment");

        EntityValue entity = new EntityValue("Task", fields);
        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
    }
}
