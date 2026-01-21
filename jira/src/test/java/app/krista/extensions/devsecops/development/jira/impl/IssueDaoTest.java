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

package app.krista.extensions.devsecops.development.jira.impl;

import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class IssueDaoTest {

    private IssueDao issueDao;

    @Mock
    private RestClientFactory restClientFactory;
    @Mock
    private RestClient restClient;
    @Mock
    Response response;
    @Mock
    private Invocation.Builder builder;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(restClientFactory.getInstance()).thenReturn(restClient);
        when(restClient.getResourceInvoker(anyString(), any())).thenReturn(builder);
        issueDao = new IssueDao(restClientFactory, restClient);
        when(response.getStatus()).thenReturn(201);
    }

    @Test
    public void getAccountIdByEmail_Success() {
        // Given
        String email = "test@example.com";
        String accountId = "testAccountId";
        RestClientFactory restClientFactoryMock = Mockito.mock(RestClientFactory.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("accountId", accountId);
        JsonArray jsonArray = new JsonArray();
        jsonArray.add(jsonObject);
        String jsonResponse = jsonArray.toString();

        when(restClientFactoryMock.getInstance()).thenReturn(restClientMock);
        when(restClientMock.fetch(anyString(), any(), any())).thenReturn(jsonResponse);

        IssueDao issueDao = new IssueDao(restClientFactoryMock, restClientMock);

        // When
        String result = issueDao.getAccountIdByEmail(email);

        // Then
        assertEquals(accountId, result);
        verify(restClientMock).fetch("user/search", Map.of("query", email), String.class);
    }

    @Test
    public void getAccountIdByEmail_NoUser() {
        // Given
        String email = "test@example.com";
        RestClientFactory restClientFactoryMock = Mockito.mock(RestClientFactory.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);

        JsonArray jsonArray = new JsonArray();
        String jsonResponse = jsonArray.toString();

        when(restClientFactoryMock.getInstance()).thenReturn(restClientMock);
        when(restClientMock.fetch(anyString(), any(), any())).thenReturn(jsonResponse);

        IssueDao issueDao = new IssueDao(restClientFactoryMock, restClientMock);

        // When
        String result = issueDao.getAccountIdByEmail(email);

        // Then
        assertEquals("No user found with email " + email, result);
        verify(restClientMock).fetch("user/search", Map.of("query", email), String.class);
    }


    @Test
    public void createUser() {
        // Given
        String testEmail = "test@test.com";
        String testDisplayName = "Test User";
        String testResponse = "{\"self\":\"http://test.self.url\",\"accountId\":\"testAccountId\",\"emailAddress\":\"" + testEmail + "\",\"displayName\":\"" + testDisplayName + "\"}";

        when(response.readEntity(String.class)).thenReturn(testResponse);
        when(builder.post(any())).thenReturn(response);

        // When
        Map<String, Object> actualUserDetails = issueDao.createUser(testEmail);

        // Then
        Map<String, Object> expectedUserDetails = Map.of(
                "Self", "http://test.self.url",
                "Account Id", "testAccountId",
                "Email Address", testEmail,
                "Display Name", testDisplayName
        );
        assertEquals(expectedUserDetails, actualUserDetails);
    }

    @Test
    public void updateUser() {
        // Given
        String testUserKey = "testUserKey";
        List<String> addGroupNames = Arrays.asList("group1", "group2");

        // Mock RestClientFactory and RestClient
        RestClientFactory restClientFactoryMock = Mockito.mock(RestClientFactory.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);
        Invocation.Builder invokerMock = Mockito.mock(Invocation.Builder.class); // Mock the invoker

        when(restClientFactoryMock.getInstance()).thenReturn(restClientMock);
        when(restClientFactoryMock.getInstance(anyBoolean())).thenReturn(restClientMock);
        when(restClientMock.getResourceInvoker(anyString(), any())).thenReturn(invokerMock); // Stub the getResourceInvoker() call
        when(invokerMock.post(any())).thenReturn(response); // Stub the post() call on invoker

        // Create mocks for other dependencies
        ProjectDao projectDaoMock = Mockito.mock(ProjectDao.class);
        UserDao userDaoMock = Mockito.mock(UserDao.class);
        TicketInfoDao mockTicketInfoDao = Mockito.mock(TicketInfoDao.class);

        // Create IssueDao with mock dependencies
        IssueDao issueDao = new IssueDao(projectDaoMock, userDaoMock, mockTicketInfoDao, restClientFactoryMock);

        // When
        Boolean result = issueDao.updateUser(testUserKey, addGroupNames);

        // Then
        assertTrue(result);

        // Verify that getResourceInvoker is called for each group
        verify(restClientMock, times(addGroupNames.size())).getResourceInvoker(anyString(), any());

        // Verify that post is called for each group
        verify(invokerMock, times(addGroupNames.size())).post(any());
    }

    @Test
    public void deleteUser_Success() {
        // Given
        String userKey = "testUserKey";
        RestClientFactory restClientFactoryMock = Mockito.mock(RestClientFactory.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);
        Invocation.Builder invokerMock = Mockito.mock(Invocation.Builder.class);

        when(restClientFactoryMock.getInstance()).thenReturn(restClientMock);
        when(restClientMock.getResourceInvoker(anyString(), any())).thenReturn(invokerMock);
        when(response.getStatus()).thenReturn(204);
        when(invokerMock.delete()).thenReturn(response); // Success

        IssueDao issueDao = new IssueDao(restClientFactoryMock, restClientMock);

        // When
        boolean result = issueDao.deleteUser(userKey);

        // Then
        assertTrue(result);
        verify(restClientMock).getResourceInvoker("user", Map.of("accountId", userKey));
        verify(invokerMock).delete();
    }

    @Test
    public void deleteUser_Failure() {
        // Given
        String userKey = "testUserKey";
        RestClientFactory restClientFactoryMock = Mockito.mock(RestClientFactory.class);
        RestClient restClientMock = Mockito.mock(RestClient.class);
        Invocation.Builder invokerMock = Mockito.mock(Invocation.Builder.class);

        when(restClientFactoryMock.getInstance()).thenReturn(restClientMock);
        when(restClientMock.getResourceInvoker(anyString(), any())).thenReturn(invokerMock);
        when(response.getStatus()).thenReturn(400);
        when(invokerMock.delete()).thenReturn(response); // Failure

        IssueDao issueDao = new IssueDao(restClientFactoryMock, restClientMock);

        // When
        boolean result = issueDao.deleteUser(userKey);

        // Then
        assertFalse(result);
        verify(restClientMock).getResourceInvoker("user", Map.of("accountId", userKey));
        verify(invokerMock).delete();
    }


}
