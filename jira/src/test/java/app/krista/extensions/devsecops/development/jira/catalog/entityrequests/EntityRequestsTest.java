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

import app.krista.extension.executor.SearchQuery;
import app.krista.extensions.devsecops.development.jira.catalog.entityrequests.impl.BugImpl;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EntityRequestsTest {

    private ServiceRegistry serviceRegistry;
    private EntityImplementationService mockService;
    private EntityRequests entityRequests;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        serviceRegistry = mock(ServiceRegistry.class);
        mockService = mock(BugImpl.class);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Bug");
        when(serviceRegistry.getServiceMap()).thenReturn(Map.of("Bug", mockService));
        entityRequests = new EntityRequests(serviceRegistry);
    }

    @Test
    void create_delegatesToService() {
        EntityValue expected = new EntityValue("Bug", Map.of("Key", "BUG-1"));
        when(mockService.create(eq(entityType), any())).thenReturn(expected);

        EntityValue result = entityRequests.create(entityType, Map.of());
        assertNotNull(result);
        verify(mockService).create(eq(entityType), any());
    }

    @Test
    void update_delegatesToService() {
        EntityValue entity = new EntityValue("Bug", Map.of("Key", "BUG-1"));
        when(mockService.update(eq(entityType), eq(entity))).thenReturn(entity);

        EntityValue result = entityRequests.update(entityType, entity);
        assertNotNull(result);
        verify(mockService).update(eq(entityType), eq(entity));
    }

    @Test
    void search_delegatesToService() {
        SearchQuery query = mock(SearchQuery.class);
        EntityValue ev = new EntityValue("Bug", Map.of());
        when(mockService.search(eq(entityType), eq(query), eq(1L), eq(10))).thenReturn(List.of(ev));

        List<EntityValue> result = entityRequests.search(entityType, query, 1L, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(mockService).search(eq(entityType), eq(query), eq(1L), eq(10));
    }

    @Test
    void get_delegatesToService() {
        EntityValue expected = new EntityValue("Bug", Map.of("Key", "BUG-1"));
        when(mockService.get(eq(entityType), eq("BUG-1"))).thenReturn(expected);

        EntityValue result = entityRequests.get(entityType, "BUG-1");
        assertNotNull(result);
        verify(mockService).get(eq(entityType), eq("BUG-1"));
    }
}
