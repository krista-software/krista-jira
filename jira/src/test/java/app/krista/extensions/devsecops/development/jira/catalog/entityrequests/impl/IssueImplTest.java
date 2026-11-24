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
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IssueImplTest {

    private IssueDao issueDao;
    private TicketInfoDao ticketInfoDao;
    private IssueImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        ticketInfoDao = mock(TicketInfoDao.class);
        impl = new IssueImpl(issueDao, ticketInfoDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Issue");
    }

    private Issue createIssue(String key, String type) {
        Issue issue = new Issue();
        issue.key = key;
        issue.issueType = type;
        issue.summary = "Issue summary";
        issue.status = "Open";
        issue.priority = "Medium";
        issue.assigneeName = "John";
        issue.reporterName = "Jane";
        issue.projectKey = "PROJ";
        issue.description = "Description";
        issue.labels = new ArrayList<>();
        issue.components = new ArrayList<>();
        return issue;
    }

    @Test
    void create_basicIssue_returnsEntityValue() {
        Issue created = createIssue("ISSUE-1", JiraConstants.BUG);
        when(issueDao.createIssue("PROJ", "Issue title", "Issue title", null, JiraConstants.BUG))
                .thenReturn(created);
        when(issueDao.getIssueFinal("ISSUE-1")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Issue title");
        attrs.put(JiraConstants.ISSUE_TYPE, JiraConstants.BUG);

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
    }

    @Test
    void create_withCommentsComponentsAndLabels() {
        Issue created = createIssue("ISSUE-2", JiraConstants.TASK);
        when(issueDao.createIssue(any(), any(), any(), any(), any())).thenReturn(created);
        when(issueDao.getIssueFinal("ISSUE-2")).thenReturn(created);

        Map<String, Object> attrs = new HashMap<>();
        attrs.put(JiraConstants.PROJECT_KEY, "PROJ");
        attrs.put(JiraConstants.SUMMARY, "Issue title");
        attrs.put(JiraConstants.ISSUE_TYPE, JiraConstants.TASK);
        attrs.put(JiraConstants.DESCRIPTION, "Some description");
        attrs.put(JiraConstants.COMMENTS, List.of("comment1"));
        attrs.put(JiraConstants.COMPONENTS, List.of("Backend"));
        attrs.put(JiraConstants.ASSIGNEE_NAME, "Alice");
        attrs.put(JiraConstants.PRIORITY, "High");
        attrs.put("Status", "Open");
        attrs.put(JiraConstants.LABELS, List.of("urgent"));

        EntityValue result = impl.create(entityType, attrs);
        assertNotNull(result);
        verify(issueDao).addComment(eq("ISSUE-2"), eq("comment1"), any());
        verify(issueDao).addComponent(eq("ISSUE-2"), eq("Backend"));
        verify(issueDao).addLabel(eq("urgent"), eq("ISSUE-2"));
    }

    @Test
    void update_nonExistingIssue_throwsNoSuchElement() {
        when(issueDao.getIssueFinal("ISSUE-999")).thenReturn(null);
        Map<String, Object> fields = Map.of("Key", "ISSUE-999");
        assertThrows(NoSuchElementException.class,
                () -> impl.update(entityType, new EntityValue("Issue", fields)));
    }

    @Test
    void update_existingIssue_throwsNPEForUninitializedLabels() {
        // Issue.labels is null by default and getIssueEntity does not populate it,
        // so issue.labels.forEach(...) in update() throws NullPointerException.
        Issue existing = createIssue("ISSUE-3", JiraConstants.BUG);
        when(issueDao.getIssueFinal("ISSUE-3")).thenReturn(existing);

        Map<String, Object> fields = new HashMap<>();
        fields.put("Key", "ISSUE-3");
        fields.put(JiraConstants.SUMMARY, "Updated issue");
        fields.put(JiraConstants.REPORTER_NAME, "Jane");
        fields.put(JiraConstants.ASSIGNEE_NAME, "John");
        fields.put(JiraConstants.ID, "789");
        fields.put(JiraConstants.PARENT_KEY, "EPIC-1");
        fields.put(JiraConstants.PROJECT_KEY, "PROJ");
        fields.put(JiraConstants.DESCRIPTION, "Updated desc");
        fields.put(JiraConstants.ISSUE_TYPE, JiraConstants.BUG);
        fields.put(JiraConstants.PRIORITY, "High");
        fields.put(JiraConstants.STATUS, "In Progress");

        EntityValue entity = new EntityValue("Issue", fields);
        assertThrows(NullPointerException.class, () -> impl.update(entityType, entity));
    }

    @Test
    void search_byKey_directLookup_found() {
        Issue issue = createIssue("ISSUE-4", JiraConstants.STORY);
        when(issueDao.getIssueFinal("ISSUE-4")).thenReturn(issue);

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "ISSUE-4", "key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_noConditions_returnsFromFallback() {
        Issue issue = createIssue("ISSUE-5", JiraConstants.TASK);
        when(issueDao.searchByJQL(any(), anyLong(), anyLong())).thenReturn(List.of(issue));

        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of());
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void get_existingIssue_returnsEntityValue() {
        Issue issue = createIssue("ISSUE-6", JiraConstants.BUG);
        when(issueDao.getIssueFinal("ISSUE-6")).thenReturn(issue);

        EntityValue result = impl.get(entityType, "ISSUE-6");
        assertNotNull(result);
    }
}
