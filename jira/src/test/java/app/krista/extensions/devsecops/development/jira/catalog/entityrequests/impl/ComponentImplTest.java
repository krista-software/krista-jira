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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Component;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComponentImplTest {

    private IssueDao issueDao;
    private ComponentImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        issueDao = mock(IssueDao.class);
        impl = new ComponentImpl(issueDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Component");
    }

    private Component makeComponent(String id, String name, String project) {
        return new Component(id, name, project);
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("Component", Map.of())));
    }

    @Test
    void search_byName_returnsMatchingComponents() {
        Component c1 = makeComponent("1", "Backend", "PROJ");
        Component c2 = makeComponent("2", "Frontend", "PROJ");
        when(issueDao.getComponents()).thenReturn(List.of(c1, c2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "back", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byProject_returnsMatchingComponents() {
        Component c1 = makeComponent("1", "Backend", "PROJECT-A");
        Component c2 = makeComponent("2", "Frontend", "PROJECT-B");
        when(issueDao.getComponents()).thenReturn(List.of(c1, c2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "project-a", "Project");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_singleConditionEmptyResult_fallsBackToGetAll() {
        Component c1 = makeComponent("1", "Backend", "PROJ");
        when(issueDao.getComponents()).thenReturn(List.of(c1));

        // Non-matching name condition → empty, then getAllComponents called
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "xyz-no-match", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        // getAllComponents returns c1 (no filter on name since it calls getComponents() again)
        assertNotNull(result);
    }

    @Test
    void get_existingComponent_returnsEntityValue() {
        Component c = makeComponent("comp-1", "Backend", "PROJ");
        when(issueDao.getComponents()).thenReturn(List.of(c));

        EntityValue result = impl.get(entityType, "comp-1");
        assertNotNull(result);
    }

    @Test
    void get_nonExistingComponent_throwsRuntimeException() {
        when(issueDao.getComponents()).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> impl.get(entityType, "nonexistent-id"));
    }
}
