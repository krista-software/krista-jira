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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketProjectImplTest {

    private ProjectDao projectDao;
    private TicketProjectImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        projectDao = mock(ProjectDao.class);
        impl = new TicketProjectImpl(projectDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Ticket Project");
    }

    private TicketProject makeTicketProject(String key, String name) {
        TicketProject tp = new TicketProject();
        tp.key = key;
        tp.name = name;
        tp.projectId = "pid-" + key;
        tp.type = "software";
        tp.style = "classic";
        tp.assigneeType = "PROJECT_LEAD";
        tp.leadAccountId = "lead-1";
        tp.leadName = "Lead Name";
        return tp;
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("Ticket Project", Map.of())));
    }

    @Test
    void search_byName_returnsMatchingProjects() {
        TicketProject p1 = makeTicketProject("ALPHA", "Alpha Project");
        TicketProject p2 = makeTicketProject("BETA", "Beta Project");
        when(projectDao.getAllProjects()).thenReturn(List.of(p1, p2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "alpha", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byKey_returnsMatchingProjects() {
        TicketProject p1 = makeTicketProject("ALPHA", "Alpha Project");
        TicketProject p2 = makeTicketProject("BETA", "Beta Project");
        when(projectDao.getAllProjects()).thenReturn(List.of(p1, p2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "beta", "Key");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_singleConditionEmptyResult_fallsBackToGetAll() {
        TicketProject p1 = makeTicketProject("ALPHA", "Alpha Project");
        when(projectDao.getAllProjects()).thenReturn(List.of(p1));

        // Name condition that won't match → falls back to getAllProjects
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
    void search_unknownFieldSingleCondition_fallsBackToGetAll() {
        TicketProject p1 = makeTicketProject("PROJ", "My Project");
        when(projectDao.getAllProjects()).thenReturn(List.of(p1));

        // Unknown field → no match in switch → list stays empty → single condition → fallback
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "anything", "UnknownField");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void get_existingProject_returnsEntityValue() {
        TicketProject p = makeTicketProject("PROJ", "My Project");
        when(projectDao.getProjectByIdOrKey("PROJ")).thenReturn(p);

        EntityValue result = impl.get(entityType, "PROJ");
        assertNotNull(result);
    }
}
