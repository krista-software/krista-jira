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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Version;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VersionImplTest {

    private ProjectDao projectDao;
    private VersionImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        projectDao = mock(ProjectDao.class);
        impl = new VersionImpl(projectDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Version");
    }

    private Version makeVersion(String id, String name, Double projectId) {
        Version v = new Version();
        v.id = id;
        v.name = name;
        v.projectId = projectId;
        v.description = "Desc";
        v.released = false;
        return v;
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("Version", Map.of())));
    }

    @Test
    void search_byName_returnsMatchingVersions() {
        Version v1 = makeVersion("1", "v1.0", 10.0);
        Version v2 = makeVersion("2", "v2.0", 10.0);
        when(projectDao.getVersionsForAllProjects(anyInt(), anyLong())).thenReturn(List.of(v1, v2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "v1.0", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_byProjectId_returnsVersions() {
        Version v1 = makeVersion("1", "v1.0", 10.0);
        when(projectDao.getVersions(anyString(), any(), anyLong(), anyLong())).thenReturn(List.of(v1));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "10.", "Project Id");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
    }

    @Test
    void search_singleConditionEmptyResult_fallsBackToGetAll() {
        Version v1 = makeVersion("1", "v1.0", 10.0);
        when(projectDao.getVersionsForAllProjects(anyInt(), anyLong())).thenReturn(List.of(v1));

        // Name condition that won't match → empty → getAllVersions
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "xyz-no-match", "Name");
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
    void get_existingVersion_returnsEntityValue() {
        Version v = makeVersion("ver-1", "v1.0", 10.0);
        when(projectDao.getVersionFromId("ver-1")).thenReturn(v);

        EntityValue result = impl.get(entityType, "ver-1");
        assertNotNull(result);
    }
}
