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

package app.krista.extensions.devsecops.development.jira.catalog;

import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.services.UserService;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Integration tests for OrganizationArea - User Account Information
 */
public class OrganizationAreaTest {

    private static final String USER_SEARCH_RESPONSE = "[\n" +
            "  {\n" +
            "    \"self\": \"https://kristasoft.atlassian.net/rest/api/2/user?accountId=712020:f540c563-d057-4be0-999a-4cbdb5fb8d84\",\n" +
            "    \"accountId\": \"712020:f540c563-d057-4be0-999a-4cbdb5fb8d84\",\n" +
            "    \"accountType\": \"atlassian\",\n" +
            "    \"emailAddress\": \"simran.sethi@kristasoft.com\",\n" +
            "    \"displayName\": \"simran.sethi\",\n" +
            "    \"active\": true,\n" +
            "    \"timeZone\": \"Asia/Kolkata\"\n" +
            "  }\n" +
            "]";

    private RestClientFactory restClientFactory;
    private RestClient restClient;
    private UserDao userDao;
    private UserDao mockedUserDao;
    private UserService userService;
    private UserService mockedUserService;
    private IssueDao mockedIssueDao;
    private KristaMediaClient mockedKristaMediaClient;
    private ExtensionResponseGenerator responseGenerator;
    private ErrorHandlingStateManager mockedInternalStateManager;
    private ValidationOrchestrator mockedValidationOrchestrator;
    private TelemetryMetrics mockedTelemetryMetrics;
    private Invoker mockedInvoker;
    private OrganizationArea organizationArea;
    private OrganizationArea mockedOrganizationArea;

    @Before
    public void setup() {
        restClientFactory = Mockito.mock(RestClientFactory.class);
        restClient = Mockito.mock(RestClient.class);
        userDao = new UserDao(restClientFactory, restClient);
        mockedUserDao = Mockito.mock(UserDao.class);
        mockedTelemetryMetrics = Mockito.mock(TelemetryMetrics.class);

        // Create real UserService for integration testing
        userService = new UserService(userDao, mockedTelemetryMetrics);

        // Create mocked UserService for unit testing
        mockedUserService = Mockito.mock(UserService.class);

        mockedIssueDao = Mockito.mock(IssueDao.class);
        mockedKristaMediaClient = Mockito.mock(KristaMediaClient.class);
        responseGenerator = new ExtensionResponseGenerator();
        mockedInternalStateManager = Mockito.mock(ErrorHandlingStateManager.class);
        mockedValidationOrchestrator = Mockito.mock(ValidationOrchestrator.class);
        mockedInvoker = Mockito.mock(Invoker.class);

        // Create OrganizationArea with real UserService for integration testing
        organizationArea = new OrganizationArea(
                mockedInvoker,
                mockedIssueDao,
                userDao,
                userService,
                mockedKristaMediaClient,
                responseGenerator,
                mockedInternalStateManager,
                mockedValidationOrchestrator,
                mockedTelemetryMetrics
        );

        // Create OrganizationArea with mocked UserService for unit testing
        mockedOrganizationArea = new OrganizationArea(
                mockedInvoker,
                mockedIssueDao,
                mockedUserDao,
                mockedUserService,
                mockedKristaMediaClient,
                responseGenerator,
                mockedInternalStateManager,
                mockedValidationOrchestrator,
                mockedTelemetryMetrics
        );
    }

    @Test
    public void testGetUserAccountInformation_Success() {
        // Given
        String searchQuery = "simran.sethi@kristasoft.com";
        when(restClient.fetch(eq("user/search"), anyMap(), eq(String.class)))
                .thenReturn(USER_SEARCH_RESPONSE);

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getResponseValue());
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertEquals("simran.sethi", user.name);
        Assert.assertEquals("712020:f540c563-d057-4be0-999a-4cbdb5fb8d84", user.accountId);
        Assert.assertEquals("simran.sethi@kristasoft.com", user.email);
        Assert.assertEquals("atlassian", user.accountType);
        Assert.assertEquals("https://kristasoft.atlassian.net/rest/api/2/user?accountId=712020:f540c563-d057-4be0-999a-4cbdb5fb8d84",
                user.self);
        Assert.assertEquals("simran.sethi", user.displayName);
    }

    @Test
    public void testGetUserAccountInformation_UserNotFound() {
        // Given
        String searchQuery = "nonexistent@example.com";
        when(restClient.fetch(eq("user/search"), anyMap(), eq(String.class)))
                .thenReturn("[]"); // Empty array - no users found

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertNull(user.name); // Empty user object
    }

    @Test
    public void testGetUserAccountInformation_NullQuery() {
        // Given
        String searchQuery = null;

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertNull(user.name); // Empty user object
    }

    @Test
    public void testGetUserAccountInformation_EmptyQuery() {
        // Given
        String searchQuery = "   ";

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("User"));

        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertNull(user.name); // Empty user object
    }

    @Test
    public void testGetUserAccountInformation_SearchByName() {
        // Given
        String searchQuery = "simran.sethi";
        when(restClient.fetch(eq("user/search"), anyMap(), eq(String.class)))
                .thenReturn(USER_SEARCH_RESPONSE);

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertEquals("simran.sethi", user.name);
    }

    @Test
    public void testGetUserAccountInformation_WithAllFields() {
        // Given
        String searchQuery = "test@example.com";
        String responseWithAllFields = "[\n" +
                "  {\n" +
                "    \"self\": \"https://example.atlassian.net/rest/api/2/user?accountId=123\",\n" +
                "    \"accountId\": \"123\",\n" +
                "    \"accountType\": \"atlassian\",\n" +
                "    \"emailAddress\": \"test@example.com\",\n" +
                "    \"displayName\": \"Test User\",\n" +
                "    \"active\": true,\n" +
                "    \"timeZone\": \"UTC\",\n" +
                "    \"applicationKeys\": [\"jira-software\", \"jira-servicedesk\"]\n" +
                "  }\n" +
                "]";

        when(restClient.fetch(eq("user/search"), anyMap(), eq(String.class)))
                .thenReturn(responseWithAllFields);

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertEquals("Test User", user.name);
        Assert.assertEquals("123", user.accountId);
        Assert.assertEquals("test@example.com", user.email);
        Assert.assertEquals("atlassian", user.accountType);
        Assert.assertEquals("https://example.atlassian.net/rest/api/2/user?accountId=123", user.self);
        Assert.assertEquals("Test User", user.displayName);
        Assert.assertNotNull(user.applicationKeys);
        Assert.assertEquals(2, user.applicationKeys.size());
        Assert.assertTrue(user.applicationKeys.contains("jira-software"));
        Assert.assertTrue(user.applicationKeys.contains("jira-servicedesk"));
    }

    @Test
    public void testGetUserAccountInformation_WithoutEmailField() {
        // Given - API response without email field (privacy settings)
        String searchQuery = "test@example.com";
        String responseWithoutEmail = "[\n" +
                "  {\n" +
                "    \"self\": \"https://example.atlassian.net/rest/api/2/user?accountId=456\",\n" +
                "    \"accountId\": \"456\",\n" +
                "    \"accountType\": \"atlassian\",\n" +
                "    \"displayName\": \"Private User\",\n" +
                "    \"active\": true\n" +
                "  }\n" +
                "]";

        when(restClient.fetch(eq("user/search"), anyMap(), eq(String.class)))
                .thenReturn(responseWithoutEmail);

        // When
        ExtensionResponse response = organizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        User user = (User) response.getResponseValue().get("User");
        Assert.assertNotNull(user);
        Assert.assertEquals("Private User", user.name);
        Assert.assertEquals("456", user.accountId);
        Assert.assertEquals("", user.email); // Email should be empty string, not null
        Assert.assertEquals("atlassian", user.accountType);
    }

    @Test
    public void testGetUserAccountInformation_DelegationToService() {
        // Given
        String searchQuery = "test@example.com";
        User expectedUser = new User();
        expectedUser.name = "Test User";
        expectedUser.accountId = "123";

        ExtensionResponse expectedResponse = ExtensionResponseFactory.create(Map.of("User", expectedUser));

        when(mockedUserService.getUserAccountInformation(searchQuery))
                .thenReturn(expectedResponse);

        // When
        ExtensionResponse response = mockedOrganizationArea.getUserAccountInformation(searchQuery);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(expectedResponse, response);
        verify(mockedUserService, times(1)).getUserAccountInformation(searchQuery);
    }
}

