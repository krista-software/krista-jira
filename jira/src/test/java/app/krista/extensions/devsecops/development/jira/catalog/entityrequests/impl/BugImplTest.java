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

class BugImplTest {

    private IssueDao issueDao;
    private BugImpl bugImpl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        bugImpl = new BugImpl(issueDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Bug");
    }

    private Issue createIssue(String key, String type) {
        Issue issue = new Issue();
        issue.key = key;
        issue.issueType = type;
        issue.summary = "Test summary";
        issue.status = "Open";
        issue.priority = "High";
        issue.assigneeName = "John";
        issue.reporterName = "Jane";
        issue.projectKey = "PROJ";
        issue.description = "Description";
        return issue;
    }

    @Test
    void create_minimalFields_createsAndReturns() {
        Issue created = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.createIssue("PROJ", "Login fails", "Login fails", null, JiraConstants.BUG))
                .thenReturn(created);
        when(issueDao.getIssueFinal("BUG-1")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Login fails");

        EntityValue result = bugImpl.create(entityType, attrs);
        assertNotNull(result);
    }

    @Test
    void create_withAllOptionalFields_setsAllFields() {
        Issue created = createIssue("BUG-2", JiraConstants.BUG);
        when(issueDao.createIssue(any(), any(), any(), any(), any())).thenReturn(created);
        when(issueDao.getIssueFinal("BUG-2")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Bug with all fields");
        attrs.put(JiraConstants.DESCRIPTION, "Detailed description");
        attrs.put(JiraConstants.COMMENTS, List.of("comment1", "comment2"));
        attrs.put(JiraConstants.ASSIGNEE_NAME, "Alice");
        attrs.put(JiraConstants.PRIORITY, "Critical");
        attrs.put("Status", "In Progress");

        EntityValue result = bugImpl.create(entityType, attrs);
        assertNotNull(result);
        verify(issueDao, times(2)).addComment(eq("BUG-2"), any(), any());
        verify(issueDao).updateIssue(any());
    }

    @Test
    void create_missingRequiredField_throwsIllegalArgument() {
        Map<String, Object> attrs = Map.of(JiraConstants.PROJECT_KEY, "PROJ");
        assertThrows(IllegalArgumentException.class, () -> bugImpl.create(entityType, attrs));
    }

    @Test
    void update_existingBug_returnsUpdatedValue() {
        Issue existing = createIssue("BUG-3", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-3")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Bug Key", "BUG-3");
        fields.put(JiraConstants.SUMMARY, "Updated summary");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "12345");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Updated desc");
        fields.put(JiraConstants.TYPE, "Bug");
        fields.put(JiraConstants.PRIORITY, "Medium");
        fields.put(JiraConstants.STATUS, "Open");

        EntityValue entity = new EntityValue("Bug", fields);
        EntityValue result = bugImpl.update(entityType, entity);
        assertNotNull(result);
        verify(issueDao).updateIssue(any());
    }

    @Test
    void update_nonExistingBug_throwsNoSuchElement() {
        when(issueDao.getIssueFinal("BUG-999")).thenReturn(null);
        Map<String, Object> fields = new HashMap<>();
        fields.put("Bug Key", "BUG-999");
        EntityValue entity = new EntityValue("Bug", fields);
        assertThrows(NoSuchElementException.class, () -> bugImpl.update(entityType, entity));
    }

    @Test
    void search_byBugKey_directLookup_found() {
        Issue issue = createIssue("BUG-4", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-4")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "BUG-4", "bug key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = bugImpl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byBugKey_directLookup_wrongType() {
        Issue issue = createIssue("STORY-1", JiraConstants.STORY);
        when(issueDao.getIssueFinal("STORY-1")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "STORY-1", "bug key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = bugImpl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_byBugKey_directLookup_notFound() {
        when(issueDao.getIssueFinal("BUG-99")).thenReturn(null);

        SearchCondition sc = new SearchCondition(null, "BUG-99", "bug key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = bugImpl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_noConditions_returnsFromJQL() {
        Issue issue = createIssue("BUG-5", JiraConstants.BUG);
        when(issueDao.searchByJQL(any(), anyLong(), anyLong())).thenReturn(List.of(issue));

        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of());
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = bugImpl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void get_existingBug_returnsEntityValue() {
        Issue issue = createIssue("BUG-6", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-6")).thenReturn(issue);

        EntityValue result = bugImpl.get(entityType, "BUG-6");
        assertNotNull(result);
    }

    @Test
    void search_mixedResults_filtersNonBugTypes() {
        Issue bugIssue = createIssue("BUG-8", JiraConstants.BUG);
        Issue storyIssue = createIssue("STORY-1", JiraConstants.STORY);
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of(bugIssue, storyIssue));

        SearchCondition sc1 = new SearchCondition(SearchCondition.Operator.equals, "Open", "status");
        SearchCondition sc2 = new SearchCondition(SearchCondition.Operator.equals, "High", "priority");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc1, sc2));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = bugImpl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void update_existingBug_withComments_coversCommentsBranch() {
        Issue existing = createIssue("BUG-7", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-7")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Bug Key", "BUG-7");
        fields.put(JiraConstants.SUMMARY, "Updated summary");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "99");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Updated desc");
        fields.put(JiraConstants.TYPE, "Bug");
        fields.put(JiraConstants.PRIORITY, "Low");
        fields.put(JiraConstants.STATUS, "Done");
        fields.put(JiraConstants.COMMENTS, "A bug comment");

        EntityValue entity = new EntityValue("Bug", fields);
        EntityValue result = bugImpl.update(entityType, entity);
        assertNotNull(result);
    }
}
