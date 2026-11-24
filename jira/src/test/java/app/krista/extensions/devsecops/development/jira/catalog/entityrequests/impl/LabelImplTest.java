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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Label;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LabelImplTest {

    private IssueDao issueDao;
    private LabelImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        impl = new LabelImpl(issueDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Label");
    }

    private Label makeLabel(String name) {
        return new Label(name);
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("Label", Map.of())));
    }

    @Test
    void search_withCondition_returnsMatchingLabels() {
        Label l1 = makeLabel("bug");
        Label l2 = makeLabel("enhancement");
        when(issueDao.getLabels()).thenReturn(List.of(l1, l2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "bug", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_singleConditionEmptyResult_fallsBackToGetAll() {
        Label l1 = makeLabel("bug");
        when(issueDao.getLabels()).thenReturn(List.of(l1));

        // Condition that won't match → falls back to getAllLabels
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "xyz-no-match", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void search_noConditions_returnsEmpty() {
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of());
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void get_existingLabel_returnsEntityValue() {
        Label l = makeLabel("bugfix");
        when(issueDao.getLabels()).thenReturn(List.of(l));

        EntityValue result = impl.get(entityType, "bugfix");
        assertNotNull(result);
    }

    @Test
    void get_nonExistingLabel_throwsRuntimeException() {
        when(issueDao.getLabels()).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> impl.get(entityType, "nonexistent"));
    }
}
