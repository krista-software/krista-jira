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

package app.krista.extensions.devsecops.development.jira.services;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for UserService
 */
public class UserServiceTest {

    private UserDao mockedUserDao;
    private TelemetryMetrics mockedTelemetryMetrics;
    private UserService userService;

    @Before
    public void setup() {
        mockedUserDao = Mockito.mock(UserDao.class);
        mockedTelemetryMetrics = Mockito.mock(TelemetryMetrics.class);
        userService = new UserService(mockedUserDao, mockedTelemetryMetrics);
    }

    @Test
    public void testGetUserAccountInformation_Success() {
        // Given
        String searchQuery = "simran.sethi@kristasoft.com";
        User expectedUser = createTestUser();

        when(mockedUserDao.getUserByEmailOrName(searchQuery)).thenReturn(expectedUser);

        // When
        ExtensionResponse response = userService.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue());
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User returnedUser = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(returnedUser);
        Assert.assertEquals("simran.sethi", returnedUser.name);
        Assert.assertEquals("712020:f540c563-d057-4be0-999a-4cbdb5fb8d84", returnedUser.accountId);
        Assert.assertEquals("simran.sethi@kristasoft.com", returnedUser.email);
        Assert.assertEquals("atlassian", returnedUser.accountType);

        // Verify telemetry (called twice: once for count, once for success)
        verify(mockedTelemetryMetrics, times(2)).incrementCounter(anyString(), eq(1L));
        verify(mockedUserDao, times(1)).getUserByEmailOrName(searchQuery);
    }

    @Test
    public void testGetUserAccountInformation_NullSearchQuery() {
        // Given
        String searchQuery = null;

        // When
        ExtensionResponse response = userService.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User returnedUser = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(returnedUser);
        Assert.assertNull(returnedUser.name); // Empty user object

        // Verify DAO was not called
        verify(mockedUserDao, never()).getUserByEmailOrName(anyString());

        // Verify failure telemetry
        verify(mockedTelemetryMetrics, times(2)).incrementCounter(anyString(), eq(1L));
    }

    @Test
    public void testGetUserAccountInformation_EmptySearchQuery() {
        // Given
        String searchQuery = "   ";

        // When
        ExtensionResponse response = userService.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User returnedUser = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(returnedUser);
        Assert.assertNull(returnedUser.name); // Empty user object

        // Verify DAO was not called
        verify(mockedUserDao, never()).getUserByEmailOrName(anyString());
    }

    @Test
    public void testGetUserAccountInformation_UserNotFound() {
        // Given
        String searchQuery = "nonexistent@example.com";
        when(mockedUserDao.getUserByEmailOrName(searchQuery)).thenReturn(null);

        // When
        ExtensionResponse response = userService.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User returnedUser = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(returnedUser);
        Assert.assertNull(returnedUser.name); // Empty user object

        // Verify DAO was called
        verify(mockedUserDao, times(1)).getUserByEmailOrName(searchQuery);

        // Verify success telemetry (no user found is still a successful query)
        verify(mockedTelemetryMetrics, times(2)).incrementCounter(anyString(), eq(1L));
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserAccountInformation_IllegalCallerException() {
        // Given
        String searchQuery = "test@example.com";
        when(mockedUserDao.getUserByEmailOrName(searchQuery))
                .thenThrow(new IllegalCallerException("Test exception"));

        // When
        userService.getUserAccountInformation(searchQuery);

        // Then - exception is thrown
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetUserAccountInformation_MustAuthorizeException() {
        // Given
        String searchQuery = "test@example.com";
        when(mockedUserDao.getUserByEmailOrName(searchQuery))
                .thenThrow(new MustAuthorizeException("Test auth exception"));

        // When
        userService.getUserAccountInformation(searchQuery);

        // Then - exception is thrown
    }

    private User createTestUser() {
        User user = new User();
        user.name = "simran.sethi";
        user.accountId = "712020:f540c563-d057-4be0-999a-4cbdb5fb8d84";
        user.email = "simran.sethi@kristasoft.com";
        user.accountType = "atlassian";
        return user;
    }
}

