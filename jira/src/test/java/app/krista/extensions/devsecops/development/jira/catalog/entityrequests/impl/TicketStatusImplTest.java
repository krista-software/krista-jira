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
import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TicketStatusImplTest {

    private TicketInfoDao ticketInfoDao;
    private TicketStatusImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        ticketInfoDao = mock(TicketInfoDao.class);
        impl = new TicketStatusImpl(ticketInfoDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Ticket Status");
    }

    private TicketStatus makeTicketStatus(String id, String name) {
        TicketStatus ts = new TicketStatus();
        ts.id = id;
        ts.name = name;
        ts.description = "Description";
        return ts;
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("Ticket Status", Map.of())));
    }

    @Test
    void search_byName_returnsMatchingStatuses() {
        TicketStatus s1 = makeTicketStatus("1", "Open");
        TicketStatus s2 = makeTicketStatus("2", "Closed");
        when(ticketInfoDao.getStatuses()).thenReturn(List.of(s1, s2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "open", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_singleConditionEmptyResult_fallsBackToGetAll() {
        TicketStatus s1 = makeTicketStatus("1", "Open");
        when(ticketInfoDao.getStatuses()).thenReturn(List.of(s1));

        // Name condition that won't match → falls back to all statuses
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
        TicketStatus s1 = makeTicketStatus("1", "Open");
        when(ticketInfoDao.getStatuses()).thenReturn(List.of(s1));

        // Unknown field → no match → single condition → fallback
        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "anything", "UnknownField");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void get_existingStatus_returnsEntityValue() {
        TicketStatus s = makeTicketStatus("status-1", "Open");
        when(ticketInfoDao.getStatuses()).thenReturn(List.of(s));

        EntityValue result = impl.get(entityType, "status-1");
        assertNotNull(result);
    }

    @Test
    void get_nonExistingStatus_throwsRuntimeException() {
        when(ticketInfoDao.getStatuses()).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> impl.get(entityType, "nonexistent-id"));
    }
}
