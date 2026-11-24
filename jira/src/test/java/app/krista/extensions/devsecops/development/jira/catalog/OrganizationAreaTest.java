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

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Bug;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.entities.User;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.sdk.atlassian.extresp.ExtensionResponseFactory;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.api.JiraAuditService;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.KristaMediaClient;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.devsecops.development.jira.services.UserService;
import app.krista.extensions.devsecops.development.jira.util.TelemetryConstants;
import app.krista.ksdk.context.RequestContext;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import app.krista.model.base.File;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
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
    private RequestContext mockedRequestContext;
    private JiraAuditService mockedJiraAuditService;
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
        mockedRequestContext = Mockito.mock(RequestContext.class);
        mockedJiraAuditService = Mockito.mock(JiraAuditService.class);

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
                mockedTelemetryMetrics,
                mockedRequestContext,
                mockedJiraAuditService
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
                mockedTelemetryMetrics,
                mockedRequestContext,
                mockedJiraAuditService
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

    // -------------------------------------------------------------------------
    // getUserAccountInformation – exception paths (45% -> higher coverage)
    // -------------------------------------------------------------------------

    @Test(expected = RuntimeException.class)
    public void testGetUserAccountInformation_GenericException() {
        // userService throws a plain RuntimeException -> method re-throws it
        when(mockedUserService.getUserAccountInformation(Mockito.any()))
                .thenThrow(new RuntimeException("service failure"));

        mockedOrganizationArea.getUserAccountInformation("query");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetUserAccountInformation_MustAuthorizeException() {
        // userService throws MustAuthorizeException -> method re-throws it
        when(mockedUserService.getUserAccountInformation(Mockito.any()))
                .thenThrow(new MustAuthorizeException("not authorised"));

        mockedOrganizationArea.getUserAccountInformation("query");
    }

    @Test
    public void testGetUserAccountInformation_GenericExceptionCallsAuditFailure() {
        // Verify jiraAuditService.failure() is called when a generic exception occurs
        RuntimeException cause = new RuntimeException("service failure");
        when(mockedUserService.getUserAccountInformation(Mockito.any())).thenThrow(cause);
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");

        try {
            mockedOrganizationArea.getUserAccountInformation("query");
        } catch (RuntimeException ignored) {
            // expected re-throw
        }

        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Get User Account Information"), Mockito.any(Exception.class));
    }

    // -------------------------------------------------------------------------
    // attachFileToTicket – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testAttachFileToTicket_Success() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.attachFileToTicket(anyList(), Mockito.anyString())).thenReturn(true);

        ExtensionResponse result = mockedOrganizationArea.attachFileToTicket("PROJ-1", Collections.emptyList());

        Assert.assertNotNull(result);
    }

    @Test
    public void testAttachFileToTicket_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("validation error"));

        ExtensionResponse result = mockedOrganizationArea.attachFileToTicket("PROJ-1", Collections.emptyList());

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Attach File to Ticket"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testAttachFileToTicket_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.attachFileToTicket("PROJ-1", Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // createAnIssue – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testCreateAnIssue_Success() {
        Issue issue = new Issue();
        issue.key = "PROJ-10";
        issue.summary = "Test summary";

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createIssue(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.isNull(), Mockito.anyString(), Mockito.isNull()))
                .thenReturn(issue);

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", null, null);

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateAnIssue_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("dao error"));

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", null, null);

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Create an issue"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testCreateAnIssue_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", null, null);
    }

    @Test
    public void testCreateAnIssue_InvalidReporterAccountId() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedUserDao.isUserExist("bad-account-id")).thenReturn(false);

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", "bad-account-id", null);

        Assert.assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // reportABug – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testReportABug_Success() {
        Issue issue = new Issue();
        issue.key = "PROJ-20";
        issue.summary = "A bug summary";
        issue.issueType = "Bug";

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createIssue(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.isNull(), Mockito.anyString()))
                .thenReturn(issue);

        ExtensionResponse result = mockedOrganizationArea.reportABug("PROJ", "A bug summary");

        Assert.assertNotNull(result);
    }

    @Test
    public void testReportABug_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("validation error"));

        ExtensionResponse result = mockedOrganizationArea.reportABug("PROJ", "A bug summary");

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Report a bug"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testReportABug_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.reportABug("PROJ", "A bug summary");
    }

    // -------------------------------------------------------------------------
    // createUser – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testCreateUser_Success() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Self", "https://example.atlassian.net/rest/api/2/user?accountId=abc");
        userMap.put("Account Id", "abc123");
        userMap.put("Email Address", "new.user@example.com");
        userMap.put("Display Name", "New User");

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createUser("new.user@example.com")).thenReturn(userMap);

        ExtensionResponse result = mockedOrganizationArea.createUser("new.user@example.com");

        Assert.assertNotNull(result);
    }

    @Test
    public void testCreateUser_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("dao error"));

        ExtensionResponse result = mockedOrganizationArea.createUser("new.user@example.com");

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Create User"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testCreateUser_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.createUser("new.user@example.com");
    }

    // -------------------------------------------------------------------------
    // updateUser – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testUpdateUser_Success() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.updateUser(Mockito.anyString(), Mockito.anyList())).thenReturn(true);

        ExtensionResponse result = mockedOrganizationArea.updateUser(
                "account-id-123", List.of("jira-software-users"));

        Assert.assertNotNull(result);
    }

    @Test
    public void testUpdateUser_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("dao error"));

        ExtensionResponse result = mockedOrganizationArea.updateUser(
                "account-id-123", List.of("jira-software-users"));

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Update User"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testUpdateUser_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.updateUser("account-id-123", List.of("jira-software-users"));
    }

    // -------------------------------------------------------------------------
    // deleteUser – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testDeleteUser_Success() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.deleteUser(Mockito.anyString())).thenReturn(true);

        ExtensionResponse result = mockedOrganizationArea.deleteUser("account-id-123");

        Assert.assertNotNull(result);
    }

    @Test
    public void testDeleteUser_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("dao error"));

        ExtensionResponse result = mockedOrganizationArea.deleteUser("account-id-123");

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Delete User"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testDeleteUser_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.deleteUser("account-id-123");
    }

    // -------------------------------------------------------------------------
    // getUsersAccountId – happy path and exception path
    // -------------------------------------------------------------------------

    @Test
    public void testGetUsersAccountId_Success() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.getAccountIdByEmail("user@example.com")).thenReturn("account-id-xyz");

        ExtensionResponse result = mockedOrganizationArea.getUsersAccountId("user@example.com");

        Assert.assertNotNull(result);
    }

    @Test
    public void testGetUsersAccountId_Exception() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException("dao error"));

        ExtensionResponse result = mockedOrganizationArea.getUsersAccountId("user@example.com");

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Get Users Account Id"), Mockito.any(Exception.class));
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetUsersAccountId_MustAuthorizeException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.getUsersAccountId("user@example.com");
    }

    @Test
    public void testGetUsersAccountId_GetAccountIdByEmailException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.getAccountIdByEmail(Mockito.anyString()))
                .thenThrow(new RuntimeException("account lookup failed"));

        ExtensionResponse result = mockedOrganizationArea.getUsersAccountId("user@example.com");

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Get Users Account Id"), Mockito.any(Exception.class));
    }

    // -------------------------------------------------------------------------
    // IllegalCallerException tests – covers the missing catch blocks
    // -------------------------------------------------------------------------

    @Test(expected = RuntimeException.class)
    public void testAttachFileToTicket_IllegalCallerException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.attachFileToTicket("PROJ-1", Collections.emptyList());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateAnIssue_IllegalCallerException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", null, null);
    }

    @Test(expected = RuntimeException.class)
    public void testReportABug_IllegalCallerException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.reportABug("PROJ", "A bug summary");
    }

    @Test(expected = RuntimeException.class)
    public void testUpdateUser_IllegalCallerException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.updateUser("account-id-123", List.of("jira-software-users"));
    }

    @Test(expected = RuntimeException.class)
    public void testDeleteUser_IllegalCallerException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.deleteUser("account-id-123");
    }

    @Test(expected = RuntimeException.class)
    public void testGetUsersAccountId_IllegalCallerException() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new IllegalCallerException("illegal"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.getUsersAccountId("user@example.com");
    }

    @Test(expected = MustAuthorizeException.class)
    public void testGetUserAccountInformation_IllegalCallerViaInvokeAsUser_MustAuthorize() {
        // getUserAccountInformation re-checks invokeAsUser and calls issueDao.validateCredentials()
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        Mockito.doThrow(new MustAuthorizeException("not authorised"))
                .when(mockedIssueDao).validateCredentials();

        mockedOrganizationArea.getUserAccountInformation("query");
    }

    // -------------------------------------------------------------------------
    // invokeAsUser=true SUCCESS path tests - covers issueDao.validateCredentials()
    // without throwing (the void mock does nothing by default)
    // -------------------------------------------------------------------------

    @Test
    public void testAttachFileToTicket_InvokeAsUser_ValidateCredentialsSuccess() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        // validateCredentials() is void - default mock does nothing (no throw = success)
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.attachFileToTicket(anyList(), Mockito.anyString())).thenReturn(true);

        ExtensionResponse result = mockedOrganizationArea.attachFileToTicket("KE-123", Collections.emptyList());

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testCreateAnIssue_InvokeAsUser_ValidateCredentialsSuccess() {
        Issue issue = new Issue();
        issue.key = "PROJ-10";
        issue.summary = "Test summary";

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        // validateCredentials() is void - default mock does nothing
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createIssue(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.isNull(), Mockito.anyString(), Mockito.isNull()))
                .thenReturn(issue);

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", null, null);

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testReportABug_InvokeAsUser_ValidateCredentialsSuccess() {
        Issue issue = new Issue();
        issue.key = "PROJ-20";
        issue.summary = "A bug summary";
        issue.issueType = "Bug";

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createIssue(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.isNull(), Mockito.anyString()))
                .thenReturn(issue);

        ExtensionResponse result = mockedOrganizationArea.reportABug("PROJ", "A bug summary");

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testCreateUser_InvokeAsUser_ValidateCredentialsSuccess() {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("Self", "https://example.atlassian.net/rest/api/2/user?accountId=abc");
        userMap.put("Account Id", "abc123");
        userMap.put("Email Address", "new.user@example.com");
        userMap.put("Display Name", "New User");

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createUser("new.user@example.com")).thenReturn(userMap);

        ExtensionResponse result = mockedOrganizationArea.createUser("new.user@example.com");

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testUpdateUser_InvokeAsUser_ValidateCredentialsSuccess() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.updateUser(Mockito.anyString(), Mockito.anyList())).thenReturn(true);

        ExtensionResponse result = mockedOrganizationArea.updateUser(
                "account-id-123", List.of("jira-software-users"));

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testDeleteUser_InvokeAsUser_ValidateCredentialsSuccess() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.deleteUser(Mockito.anyString())).thenReturn(true);

        ExtensionResponse result = mockedOrganizationArea.deleteUser("account-id-123");

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetUsersAccountId_InvokeAsUser_ValidateCredentialsSuccess() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.getAccountIdByEmail("user@example.com")).thenReturn("account-id-xyz");

        ExtensionResponse result = mockedOrganizationArea.getUsersAccountId("user@example.com");

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    @Test
    public void testGetUserAccountInformation_InvokeAsUser_ValidateCredentialsSuccess() {
        String searchQuery = "test@example.com";
        User expectedUser = new User();
        expectedUser.name = "Test User";
        expectedUser.accountId = "123";
        ExtensionResponse expectedResponse = ExtensionResponseFactory.create(Map.of("User", expectedUser));

        when(mockedRequestContext.invokeAsUser()).thenReturn(true);
        // validateCredentials() does nothing (success path)
        when(mockedUserService.getUserAccountInformation(searchQuery)).thenReturn(expectedResponse);

        ExtensionResponse result = mockedOrganizationArea.getUserAccountInformation(searchQuery);

        Assert.assertNotNull(result);
        Assert.assertEquals(expectedResponse, result);
        Mockito.verify(mockedIssueDao).validateCredentials();
    }

    // -------------------------------------------------------------------------
    // Validation failure / retry path tests
    // Covers the else-branch: validationOrchestrator returns non-empty results
    // -------------------------------------------------------------------------

    private ValidationOrchestrator.ValidationResult makeValidationResult(String field, String errMsg) {
        return new ValidationOrchestrator.ValidationResult(
                "Please confirm: " + field,
                field,
                "Please re-enter " + field,
                errMsg
        );
    }

    @Test
    public void testAttachFileToTicket_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Ticket Id", "Invalid ticket id"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.attachFileToTicket("bad-ticket", Collections.emptyList());

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.ATTACH_FILE_TO_TICKET_RETRY_ATTEMPT, 1);
    }

    @Test
    public void testCreateAnIssue_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Project Key", "Invalid project key"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "BAD-PROJ", "Test summary", "Bug", "Test description", null, null);

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.CREATE_AN_ISSUE_RETRY_ATTEMPT, 1);
    }

    @Test
    public void testReportABug_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Project Key", "Invalid project key"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.reportABug("BAD-PROJ", "A bug summary");

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.REPORT_A_BUG_RETRY_ATTEMPT, 1);
    }

    @Test
    public void testCreateUser_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Email", "Invalid email format"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.createUser("not-an-email");

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.CREATE_USER_RETRY_ATTEMPT, 1);
    }

    @Test
    public void testUpdateUser_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Account Id", "User account id not found"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.updateUser(
                "bad-account-id", List.of("jira-software-users"));

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.UPDATE_USER_RETRY_ATTEMPT, 1);
    }

    @Test
    public void testDeleteUser_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Account Id", "User account id not found"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.deleteUser("bad-account-id");

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.DELETE_USER_RETRY_ATTEMPT, 1);
    }

    @Test
    public void testGetUsersAccountId_ValidationFailure_RetryPath() {
        List<ValidationOrchestrator.ValidationResult> results = List.of(
                makeValidationResult("Email", "Invalid email format"));

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(results);

        ExtensionResponse result = mockedOrganizationArea.getUsersAccountId("not-an-email");

        Assert.assertNotNull(result);
        Mockito.verify(mockedTelemetryMetrics).incrementCounter(
                TelemetryConstants.OrganizationArea.GET_USERS_ACCOUNT_ID_RETRY_ATTEMPT, 1);
    }

    // -------------------------------------------------------------------------
    // attachFileToTicket – file loop body and IOException handling
    // -------------------------------------------------------------------------

    @Test
    public void testAttachFileToTicket_WithFileProcessing_Success() {
        // Use a real File instance so GSON can serialize/deserialize it properly
        File realFile = new File(1024L, "test.txt", "txt", "media-id-001");

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());

        java.io.File javaFile = Mockito.mock(java.io.File.class);
        try {
            Mockito.when(mockedKristaMediaClient.toJavaFile(Mockito.any())).thenReturn(javaFile);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        when(mockedIssueDao.attachFileToTicket(anyList(), Mockito.anyString())).thenReturn(true);

        List<File> files = new ArrayList<>();
        files.add(realFile);

        ExtensionResponse result = mockedOrganizationArea.attachFileToTicket("KE-123", files);

        Assert.assertNotNull(result);
        try {
            Mockito.verify(mockedKristaMediaClient).toJavaFile(Mockito.any());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAttachFileToTicket_FileProcessingIOException_Continues() {
        // IOException during toJavaFile should be swallowed and processing continues
        File realFile = new File(512L, "bad.txt", "txt", "media-id-002");

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());

        try {
            Mockito.when(mockedKristaMediaClient.toJavaFile(Mockito.any()))
                    .thenThrow(new java.io.IOException("Cannot read file"));
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        // attachFileToTicket still calls issueDao even with an empty ticketFiles list
        when(mockedIssueDao.attachFileToTicket(anyList(), Mockito.anyString())).thenReturn(false);

        List<File> files = new ArrayList<>();
        files.add(realFile);

        ExtensionResponse result = mockedOrganizationArea.attachFileToTicket("KE-123", files);

        // Should not throw; IOException is caught internally and execution continues
        Assert.assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // Custom Fields mapping + create-issue custom-field path
    // -------------------------------------------------------------------------

    @Test
    public void testToCustomFieldMap_AllBranches() throws Exception {
        java.lang.reflect.Method m =
                OrganizationArea.class.getDeclaredMethod("toCustomFieldMap", List.class);
        m.setAccessible(true);

        // null list -> empty map
        @SuppressWarnings("unchecked")
        Map<String, Object> fromNull = (Map<String, Object>) m.invoke(null, (Object) null);
        Assert.assertTrue(fromNull.isEmpty());

        List<Map<String, Object>> rows = new ArrayList<>();
        rows.add(null);                                              // null row -> skipped
        Map<String, Object> nullKey = new HashMap<>();
        nullKey.put("Key", null);
        rows.add(nullKey);                                           // null Key -> skipped
        Map<String, Object> blankKey = new HashMap<>();
        blankKey.put("Key", "   ");
        rows.add(blankKey);                                          // blank Key -> skipped
        Map<String, Object> valid = new HashMap<>();
        valid.put("Key", "Category");
        valid.put("Value", "R&D Task");
        rows.add(valid);                                            // mapped

        @SuppressWarnings("unchecked")
        Map<String, Object> mapped = (Map<String, Object>) m.invoke(null, rows);
        Assert.assertEquals(1, mapped.size());
        Assert.assertEquals("R&D Task", mapped.get("Category"));
    }

    @Test
    public void testCreateAnIssue_WithCustomFields_Success() {
        Issue issue = new Issue();
        issue.key = "PROJ-77";

        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        when(mockedValidationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(mockedIssueDao.createIssue(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.isNull(), Mockito.anyString(), Mockito.isNull(), Mockito.anyMap()))
                .thenReturn(issue);

        List<Map<String, Object>> customFields = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        row.put("Key", "Category");
        row.put("Value", "R&D Task");
        customFields.add(row);

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Task", "Test description", null, customFields);

        Assert.assertNotNull(result);
        Mockito.verify(mockedIssueDao).createIssue(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.isNull(), Mockito.anyString(), Mockito.isNull(), Mockito.anyMap());
    }

    @Test
    public void testCreateAnIssue_NullMessageException_UsesClassNameInResponse() {
        when(mockedRequestContext.getInvokerId()).thenReturn("invoker-1");
        when(mockedRequestContext.invokeAsUser()).thenReturn(false);
        // Exception with a null message -> exercises the ternary's class-name fallback branch.
        when(mockedValidationOrchestrator.validate(anyMap()))
                .thenThrow(new RuntimeException());

        ExtensionResponse result = mockedOrganizationArea.createAnIssue(
                "PROJ", "Test summary", "Bug", "Test description", null, null);

        Assert.assertNotNull(result);
        Mockito.verify(mockedJiraAuditService).failure(
                Mockito.any(), Mockito.eq("Create an issue"), Mockito.any(Exception.class));
    }
}

