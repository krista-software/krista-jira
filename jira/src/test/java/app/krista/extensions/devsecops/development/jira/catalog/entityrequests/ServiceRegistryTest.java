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

import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.util.JiraConstants;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceRegistryTest {

    @Test
    void constructor_registersAllEntityServices() {
        IssueDao issueDao = mock(IssueDao.class);
        ProjectDao projectDao = mock(ProjectDao.class);
        UserDao userDao = mock(UserDao.class);
        TicketInfoDao ticketInfoDao = mock(TicketInfoDao.class);

        ServiceRegistry registry = new ServiceRegistry(issueDao, projectDao, userDao, ticketInfoDao);
        Map<String, EntityImplementationService> map = registry.getServiceMap();

        assertNotNull(map);
        assertTrue(map.containsKey(JiraConstants.BUG));
        assertTrue(map.containsKey(JiraConstants.EPIC));
        assertTrue(map.containsKey(JiraConstants.ISSUE));
        assertTrue(map.containsKey(JiraConstants.STORY));
        assertTrue(map.containsKey(JiraConstants.TASK));
        assertTrue(map.containsKey(JiraConstants.SUBTASK_ENTITY));
        assertTrue(map.containsKey(JiraConstants.ISSUE_TYPE));
        assertTrue(map.containsKey(JiraConstants.TICKET_PRIORITY));
        assertTrue(map.containsKey(JiraConstants.TICKET_STATUS));
        assertTrue(map.containsKey(JiraConstants.TICKET_PROJECT));
        assertTrue(map.containsKey(JiraConstants.USER));
        assertTrue(map.containsKey(JiraConstants.VERSION));
        assertTrue(map.containsKey(JiraConstants.COMPONENT));
        assertTrue(map.containsKey(JiraConstants.LABEL));
    }

    @Test
    void getServiceMap_returnsNonEmptyMap() {
        IssueDao issueDao = mock(IssueDao.class);
        ProjectDao projectDao = mock(ProjectDao.class);
        UserDao userDao = mock(UserDao.class);
        TicketInfoDao ticketInfoDao = mock(TicketInfoDao.class);

        ServiceRegistry registry = new ServiceRegistry(issueDao, projectDao, userDao, ticketInfoDao);
        assertFalse(registry.getServiceMap().isEmpty());
    }
}
