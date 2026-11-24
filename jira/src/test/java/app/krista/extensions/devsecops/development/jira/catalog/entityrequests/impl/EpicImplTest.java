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

class EpicImplTest {

    private IssueDao issueDao;
    private EpicImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        impl = new EpicImpl(issueDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Epic");
    }

    private Issue createIssue(String key, String type) {
        Issue issue = new Issue();
        issue.key = key;
        issue.issueType = type;
        issue.summary = "Epic summary";
        issue.status = "Open";
        issue.priority = "High";
        issue.assigneeName = "John";
        issue.reporterName = "Jane";
        issue.projectKey = "PROJ";
        issue.description = "Description";
        return issue;
    }

    @Test
    void create_basicEpic_returnsEntityValue() {
        Issue created = createIssue("EPIC-1", JiraConstants.EPIC);
        when(issueDao.createIssue("PROJ", "Epic title", "Epic title", null, JiraConstants.EPIC))
                .thenReturn(created);
        when(issueDao.getIssueFinal("EPIC-1")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Epic title");

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
    }

    @Test
    void create_withAllOptionalFields() {
        Issue created = createIssue("EPIC-2", JiraConstants.EPIC);
        when(issueDao.createIssue(any(), any(), any(), any(), any())).thenReturn(created);
        when(issueDao.getIssueFinal("EPIC-2")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Epic title");
        attrs.put(JiraConstants.DESCRIPTION, "Detailed description");
        attrs.put(JiraConstants.COMMENTS, List.of("note1"));
        attrs.put(JiraConstants.ASSIGNEE_NAME, "Alice");
        attrs.put(JiraConstants.PRIORITY, "High");
        attrs.put("Status", "Open");

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
        verify(issueDao).addComment(eq("EPIC-2"), eq("note1"), any());
    }

    @Test
    void update_existingEpic_returnsEntityValue() {
        Issue existing = createIssue("EPIC-3", JiraConstants.EPIC);
        when(issueDao.getIssueFinal("EPIC-3")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Epic Key", "EPIC-3");
        fields.put(JiraConstants.SUMMARY, "Updated epic");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "123");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Desc");
        fields.put(JiraConstants.TYPE, "Epic");
        fields.put(JiraConstants.PRIORITY, "Medium");
        fields.put(JiraConstants.STATUS, "In Progress");

        EntityValue entity = new EntityValue("Epic", fields);
        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
    }

    @Test
    void update_nonExistingEpic_throwsNoSuchElement() {
        when(issueDao.getIssueFinal("EPIC-999")).thenReturn(null);
        Map<String, Object> fields = Map.of("Epic Key", "EPIC-999");
        assertThrows(NoSuchElementException.class,
                () -> impl.update(entityType, new EntityValue("Epic", fields)));
    }

    @Test
    void search_byEpicKey_directLookup_found() {
        Issue issue = createIssue("EPIC-4", JiraConstants.EPIC);
        when(issueDao.getIssueFinal("EPIC-4")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "EPIC-4", "epic key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byEpicKey_directLookup_wrongType() {
        Issue issue = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.getIssueFinal("BUG-1")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(null, "BUG-1", "epic key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_byEpicKey_notFound() {
        when(issueDao.getIssueFinal("EPIC-99")).thenReturn(null);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "EPIC-99", "epic key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertTrue(result.isEmpty());
    }

    @Test
    void search_noConditions_returnsFromJQL() {
        Issue issue = createIssue("EPIC-5", JiraConstants.EPIC);
        when(issueDao.searchByJQL(any(), anyLong(), anyLong())).thenReturn(List.of(issue));

        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of());
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void get_existingEpic_returnsEntityValue() {
        Issue issue = createIssue("EPIC-6", JiraConstants.EPIC);
        when(issueDao.getIssueFinal("EPIC-6")).thenReturn(issue);

        EntityValue result = impl.get(entityType, "EPIC-6");
        assertNotNull(result);
    }

    @Test
    void search_mixedResults_filtersNonEpicTypes() {
        Issue epicIssue = createIssue("EPIC-8", JiraConstants.EPIC);
        Issue bugIssue = createIssue("BUG-1", JiraConstants.BUG);
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of(epicIssue, bugIssue));

        SearchCondition sc1 = new SearchCondition(SearchCondition.Operator.equals, "Open", "status");
        SearchCondition sc2 = new SearchCondition(SearchCondition.Operator.equals, "High", "priority");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc1, sc2));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void update_existingEpic_withComments_coversCommentsBranch() {
        Issue existing = createIssue("EPIC-7", JiraConstants.EPIC);
        when(issueDao.getIssueFinal("EPIC-7")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Epic Key", "EPIC-7");
        fields.put(JiraConstants.SUMMARY, "Updated epic");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "99");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Updated desc");
        fields.put(JiraConstants.TYPE, "Epic");
        fields.put(JiraConstants.PRIORITY, "Low");
        fields.put(JiraConstants.STATUS, "Done");
        fields.put(JiraConstants.COMMENTS, "An epic comment");

        EntityValue entity = new EntityValue("Epic", fields);
        EntityValue result = impl.update(entityType, entity);
        assertNotNull(result);
    }
}
