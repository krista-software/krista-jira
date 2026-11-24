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
import app.krista.extensions.devsecops.development.jira.catalog.entities.IssueType;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IssueTypeImplTest {

    private TicketInfoDao ticketInfoDao;
    private IssueTypeImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        ticketInfoDao = mock(TicketInfoDao.class);
        impl = new IssueTypeImpl(ticketInfoDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("Issue Type");
    }

    private IssueType makeIssueType(String id, String name) {
        IssueType it = new IssueType();
        it.id = id;
        it.name = name;
        it.description = "Description";
        it.subTask = "false";
        return it;
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("Issue Type", Map.of())));
    }

    @Test
    void search_byName_returnsMatchingIssueTypes() {
        IssueType it1 = makeIssueType("1", "Bug");
        IssueType it2 = makeIssueType("2", "Story");
        when(ticketInfoDao.getIssueTypes()).thenReturn(List.of(it1, it2));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "bug", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_singleConditionEmptyResult_fallsBackToGetAll() {
        IssueType it1 = makeIssueType("1", "Bug");
        when(ticketInfoDao.getIssueTypes()).thenReturn(List.of(it1));

        // Name condition that won't match → falls back to getAllIssueTypes
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
        IssueType it1 = makeIssueType("1", "Bug");
        when(ticketInfoDao.getIssueTypes()).thenReturn(List.of(it1));

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
    void get_existingIssueType_returnsEntityValue() {
        IssueType it = makeIssueType("type-1", "Bug");
        when(ticketInfoDao.getIssueTypes()).thenReturn(List.of(it));

        EntityValue result = impl.get(entityType, "type-1");
        assertNotNull(result);
    }

    @Test
    void get_nonExistingIssueType_throwsRuntimeException() {
        when(ticketInfoDao.getIssueTypes()).thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> impl.get(entityType, "nonexistent-id"));
    }
}
