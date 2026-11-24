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

class SubtaskImplTest {

    private IssueDao issueDao;
    private SubtaskImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        impl = new SubtaskImpl(issueDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Subtask");
    }

    private Issue createIssue(String key, String type) {
        Issue issue = new Issue();
        issue.key = key;
        issue.issueType = type;
        issue.summary = "Subtask summary";
        issue.status = "Open";
        issue.priority = "Medium";
        issue.assigneeName = "John";
        issue.reporterName = "Jane";
        issue.projectKey = "PROJ";
        issue.description = "Description";
        issue.parentKey = "STORY-1";
        return issue;
    }

    @Test
    void create_basicSubtask_returnsEntityValue() {
        Issue created = createIssue("SUB-1", JiraConstants.SUBTASK);
        when(issueDao.createIssue("PROJ", "Sub title", "Sub title", "STORY-1", "Sub-task"))
                .thenReturn(created);
        when(issueDao.getIssueFinal("SUB-1")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Sub title");
        attrs.put(JiraConstants.PARENT_KEY, "STORY-1");

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
    }

    @Test
    void create_withAllOptionalFields() {
        Issue created = createIssue("SUB-2", JiraConstants.SUBTASK);
        when(issueDao.createIssue(any(), any(), any(), any(), any())).thenReturn(created);
        when(issueDao.getIssueFinal("SUB-2")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Sub title");
        attrs.put(JiraConstants.PARENT_KEY, "STORY-1");
        attrs.put(JiraConstants.DESCRIPTION, "Detail");
        attrs.put(JiraConstants.COMMENTS, List.of("comment1"));
        attrs.put(JiraConstants.ASSIGNEE_NAME, "Alice");
        attrs.put(JiraConstants.PRIORITY, "Low");
        attrs.put("Status", "Open");

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
        verify(issueDao).addComment(eq("SUB-2"), eq("comment1"), any());
    }

    @Test
    void update_existingSubtask_returnsEntityValue() {
        Issue existing = createIssue("SUB-3", JiraConstants.SUBTASK);
        when(issueDao.getIssueFinal("SUB-3")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Key", "SUB-3");
        fields.put(JiraConstants.SUMMARY, "Updated subtask");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "456");
        fields.put(JiraConstants.PARENT_KEY, "STORY-1");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Desc");
        fields.put("Task Type", "Sub-task");
        fields.put(JiraConstants.PRIORITY, "Medium");
        fields.put(JiraConstants.STATUS, "Open");

        EntityValue entity = new EntityValue("Subtask", fields);
        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
    }

    @Test
    void update_nonExistingSubtask_throwsNoSuchElement() {
        when(issueDao.getIssueFinal("SUB-999")).thenReturn(null);
        Map<String, Object> fields = Map.of("Key", "SUB-999");
        assertThrows(NoSuchElementException.class,
                () -> impl.update(entityType, new EntityValue("Subtask", fields)));
    }

    @Test
    void search_byKey_directLookup_found() {
        Issue issue = createIssue("SUB-4", JiraConstants.SUBTASK);
        when(issueDao.getIssueFinal("SUB-4")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "SUB-4", "key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byKey_directLookup_wrongType() {
        Issue issue = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-1")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(null, "BUG-1", "key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_byKey_notFound() {
        when(issueDao.getIssueFinal("SUB-99")).thenReturn(null);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "SUB-99", "key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_noConditions_returnsFromJQL() {
        Issue issue = createIssue("SUB-5", JiraConstants.SUBTASK);
        when(issueDao.searchByJQL(any(), anyLong(), anyLong())).thenReturn(List.of(issue));

        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of());
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void get_existingSubtask_returnsEntityValue() {
        Issue issue = createIssue("SUB-6", JiraConstants.SUBTASK);
        when(issueDao.getIssueFinal("SUB-6")).thenReturn(issue);

        EntityValue result = impl.get(entityType, "SUB-6");
        assertNotNull(result);
    }

    @Test
    void search_mixedResults_filtersNonSubtaskTypes() {
        Issue subtaskIssue = createIssue("SUB-8", JiraConstants.SUBTASK);
        Issue bugIssue = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of(subtaskIssue, bugIssue));

        SearchCondition sc1 = new SearchCondition(SearchCondition.Operator.equals, "Open", "status");
        SearchCondition sc2 = new SearchCondition(SearchCondition.Operator.equals, "Medium", "priority");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc1, sc2));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void update_existingSubtask_withComments_coversCommentsBranch() {
        Issue existing = createIssue("SUB-7", JiraConstants.SUBTASK);
        when(issueDao.getIssueFinal("SUB-7")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Key", "SUB-7");
        fields.put(JiraConstants.SUMMARY, "Updated subtask");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "99");
        fields.put(JiraConstants.PARENT_KEY, "STORY-1");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Desc");
        fields.put("Task Type", "Sub-task");
        fields.put(JiraConstants.PRIORITY, "Low");
        fields.put(JiraConstants.STATUS, "Done");
        fields.put(JiraConstants.COMMENTS, "A subtask comment");

        EntityValue entity = new EntityValue("Subtask", fields);
        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
    }
}
