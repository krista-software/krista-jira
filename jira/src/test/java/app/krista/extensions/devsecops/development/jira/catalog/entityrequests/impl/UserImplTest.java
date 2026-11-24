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
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import app.krista.model.base.EntityValue;
import app.krista.model.entity.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserImplTest {

    private UserDao userDao;
    private UserImpl impl;
    private EntityType entityType;

    @BeforeEach
    void setUp() {
        userDao = mock(UserDao.class);
        impl = new UserImpl(userDao);
        entityType = mock(EntityType.class);
        when(entityType.getEntityName()).thenReturn("User");
    }

    private User makeUser(String accountId, String name, String email) {
        User user = new User();
        user.accountId = accountId;
        user.name = name;
        user.email = email;
        user.accountType = "atlassian";
        user.displayName = name;
        return user;
    }

    @Test
    void create_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.create(entityType, Map.of()));
    }

    @Test
    void update_alwaysThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> impl.update(entityType, new EntityValue("User", Map.of())));
    }

    @Test
    void search_withNonLogicalNameCondition_returnsUsers() {
        User u = makeUser("acc-1", "Alice", "alice@example.com");
        when(userDao.getUsersByQuery("alice")).thenReturn(List.of(u));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.contains, "alice", "Name");
        SearchQuery query = mock(SearchQuery.class);
        when(query.getSearchConditions()).thenReturn(List.of(sc));
        when(query.getQueryClauses()).thenReturn(List.of());

        List<EntityValue> result = impl.search(entityType, query, 1L, 10);
        assertEquals(1, result.size());
    }

    @Test
    void search_withLogicalNameCondition_skipsQueryAndFallsBackToEmpty() {
        // LOGICAL_NAME_FIELD condition is skipped (list remains empty)
        // then single condition empty → calls getUsersByQuery("")
        User u = makeUser("acc-1", "Alice", "alice@example.com");
        when(userDao.getUsersByQuery("")).thenReturn(List.of(u));

        SearchCondition sc = new SearchCondition(SearchCondition.Operator.equals, "Alice", JiraConstants.LOGICAL_NAME_FIELD);
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
    void get_existingUser_returnsEntityValue() {
        User u = makeUser("acc-1", "Alice", "alice@example.com");
        when(userDao.getUserByAccountId("acc-1")).thenReturn(u);

        EntityValue result = impl.get(entityType, "acc-1");
        assertNotNull(result);
    }
}
