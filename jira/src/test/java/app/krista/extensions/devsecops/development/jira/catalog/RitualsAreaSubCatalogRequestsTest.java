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

import app.krista.extension.executor.impl.AskAPersonAction;
import app.krista.extension.executor.ExtensionResponse;
import app.krista.extension.executor.impl.InformAPersonAction;
import app.krista.extension.impl.anno.Field;
import app.krista.extensions.devsecops.development.jira.authentication.RestClient;
import app.krista.extensions.devsecops.development.jira.authentication.RestClientFactory;
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.extresp.JiraResources;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import app.krista.extensions.devsecops.development.jira.impl.UserDao;
import app.krista.extensions.util.EventHandler;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RequestContext;
import app.krista.model.field.NamedField;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;

public class RitualsAreaSubCatalogRequestsTest {

    RestClient restClient;
    RestClientFactory restClientFactory;
    IssueDao issueDao;
    IssueDao mockedIssueDao;
    ProjectDao mockedProjectDao;
    UserDao mockedUserDao;
    TicketInfoDao mockedTicketInfoDao;
    EventHandler eventHandler;
    RequestContext requestContext;
    AuthorizationContext authorizationContext;
    ValidationOrchestrator mockValidationOrchestrator;
    ValidationOrchestrator validationOrchestrator;
    ErrorHandlingStateManager mockedInternalStateManager;
    ExtensionResponseGenerator responseGenerator;
    RitualsAreaSubCatalogRequests mockedRitualsAreaSubCatalogRequests;
    RitualsArea ritualsArea;

    @Before
    public void setup() {
        restClientFactory = Mockito.mock(RestClientFactory.class);
        restClient = Mockito.mock(RestClient.class);
        issueDao = new IssueDao(restClientFactory, restClient);
        mockedIssueDao = Mockito.mock(IssueDao.class);
        mockedProjectDao = Mockito.mock(ProjectDao.class);
        mockedUserDao = Mockito.mock(UserDao.class);
        mockedTicketInfoDao = Mockito.mock(TicketInfoDao.class);
        mockValidationOrchestrator = Mockito.mock(ValidationOrchestrator.class);
        validationOrchestrator = new ValidationOrchestrator(mockedIssueDao, mockedProjectDao,
                mockedUserDao, mockedTicketInfoDao, requestContext, authorizationContext);
        mockedInternalStateManager = Mockito.mock(ErrorHandlingStateManager.class);
        responseGenerator = new ExtensionResponseGenerator();
        mockedRitualsAreaSubCatalogRequests = new RitualsAreaSubCatalogRequests(
                mockedIssueDao, requestContext, mockedUserDao, authorizationContext, eventHandler,
                mockedInternalStateManager, responseGenerator, mockValidationOrchestrator, ritualsArea, mockedTicketInfoDao, mockedProjectDao
        );
    }

    @Test
    public void test_confirmReenterForAddAComment_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";

        Map<String, Object> inputMap = Map.of(
                "re-enter", false,
                "stateId", stateId,
                "Ticket Id", ticketId,
                "Comment", "Some sample comment",
                "Comment Visibility", "Public" // or "Internal"
        );

        Mockito.when(mockedIssueDao.isIssueExists(anyString())).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterForAddAComment(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterForAddAComment_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";
        String comment = "Hello World";
        Map<String, Object> inputMap = Map.of("re-enter", true,
                "stateId", stateId, "Comment", comment, JiraResources.TICKET_ID, ticketId,
                "Comment Visibility", "Public");
        Mockito.when(mockedIssueDao.isIssueExists(anyString())).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));
        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterForAddAComment(inputMap);
        Assert.assertEquals("handleReenterForAddAComment",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertEquals("Please enter valid Ticket Id.\n",
                response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
        List<NamedField> fields = ((AskAPersonAction) response.getRemediationActions().getRemediationActionList().get(0)).getFields();
        List<String> fieldNames = fields.stream().map(NamedField::getName).collect(Collectors.toList());
        Assert.assertEquals(List.of(JiraResources.TICKET_ID), fieldNames);
    }

    @Test
    public void test_handleReenterForAddAComment_SUCCESS() throws NoSuchMethodException {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "SampleTicketId";
        String comment = "Hello World";
        Mockito.doNothing().when(mockedIssueDao).addComment(ticketId, comment, null);
        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForAddAComment(
                Map.of("stateId", stateId, JiraResources.TICKET_ID, ticketId, "Comment", comment));
        Assert.assertTrue(Arrays.stream(RitualsAreaSubCatalogRequests.class.getMethod("handleReenterForAddAComment", Map.class)
                        .getDeclaredAnnotations()).filter(a -> a instanceof Field.Desc)
                .map(a -> ((Field.Desc) a).name()).collect(Collectors.toList()).contains("Is successful"));
    }

    @Test
    public void test_confirmReenterForGetUserIssues_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "SampleAccountId";
        Map<String, Object> inputMap = Map.of("re-enter", false, "stateId", stateId);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));
        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterForGetUserIssues(inputMap);
        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        System.out.println(response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterForGetUserIssues_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "SampleAccountId";
        Map<String, Object> inputMap = Map.of("re-enter", true,
                "stateId", stateId, JiraResources.ACCOUNT_ID, accountId);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));
        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterForGetUserIssues(inputMap);
        Assert.assertEquals("handleReenterForGetUserIssues",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertEquals("Please enter valid Account Id.\n",
                response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
        List<NamedField> fields = ((AskAPersonAction) response.getRemediationActions().getRemediationActionList().get(0)).getFields();
        List<String> fieldNames = fields.stream().map(NamedField::getName).collect(Collectors.toList());
        Assert.assertEquals(List.of(JiraResources.ACCOUNT_ID), fieldNames);
    }

    @Test
    public void test_handleReenterForGetUserIssues_SUCCESS() throws NoSuchMethodException {
        String stateId = UUID.randomUUID().toString();
        String accountId = "SampleAccountId";
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(List.of());
        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForGetUserIssues(
                Map.of("stateId", stateId, JiraResources.ACCOUNT_ID, accountId));
        Assert.assertTrue(Arrays.stream(RitualsAreaSubCatalogRequests.class.getMethod("handleReenterForGetUserIssues", Map.class)
                        .getDeclaredAnnotations()).filter(a -> a instanceof Field.Desc)
                .map(a -> ((Field.Desc) a).name()).collect(Collectors.toList()).contains("User Issues"));
    }

    @Test
    public void test_confirmReenterForUpdateStatusOfIssue_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "SampleTicketId";
        String statusName = "SampleStatusName";
        Map<String, Object> inputMap = Map.of("re-enter", false, "stateId", stateId,
                JiraResources.TICKET_ID, ticketId, JiraResources.STATUS_NAME, statusName);
        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions(ticketId)).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAME, statusName));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));
        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterForUpdateStatusOfIssue(inputMap);
        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterForUpdateStatusOfIssue_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "SampleTicketId";
        String statusName = "SampleStatusName";
        Map<String, Object> inputMap = Map.of("re-enter", true,
                "stateId", stateId,
                JiraResources.TICKET_ID, ticketId, JiraResources.STATUS_NAME, statusName);
        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions(ticketId)).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAME, statusName));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));
        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterForUpdateStatusOfIssue(inputMap);
        Assert.assertEquals("handleReenterForUpdateStatusOfIssue",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertEquals("Please enter valid Status Name. The current one is not valid for this issue.\n",
                response.getRemediationActions().getRemediationActionList().get(0).getMessage());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
        List<NamedField> fields = ((AskAPersonAction) response.getRemediationActions().getRemediationActionList().get(0)).getFields();
        List<String> fieldNames = fields.stream().map(NamedField::getName).collect(Collectors.toList());
        Assert.assertTrue(fieldNames.contains(JiraResources.STATUS_NAME));
    }

    @Test
    public void test_handleReenterForUpdateStatusOfIssue_SUCCESS() throws NoSuchMethodException {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "SampleTicketId";
        String statusName = "SampleStatusName";
        Map<String, Object> inputMap = Map.of("re-enter", true,
                "stateId", stateId,
                JiraResources.TICKET_ID, ticketId, JiraResources.STATUS_NAME, statusName);
        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedTicketInfoDao.getTransitions(ticketId)).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAME, statusName));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));
        Mockito.doNothing().when(mockedIssueDao).updateStatus(ticketId, statusName);
        Mockito.when(mockedIssueDao.getIssueFinal(ticketId))
                .thenReturn(new Issue());
        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterForUpdateStatusOfIssue(inputMap);
        Assert.assertTrue(response.getResponseValue().containsKey("Updated Issue"));
        Assert.assertTrue(Arrays.stream(RitualsAreaSubCatalogRequests.class.getMethod("handleReenterForUpdateStatusOfIssue", Map.class)
                        .getDeclaredAnnotations()).filter(a -> a instanceof Field.Desc)
                .map(a -> ((Field.Desc) a).name()).collect(Collectors.toList()).contains("Updated Issue"));
    }

    // =========================================================================
    // confirmReenterAssignATaskToUser
    // =========================================================================

    @Test
    public void test_confirmReenterAssignATaskToUser_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";
        String accountId = "SampleAccountId";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterAssignATaskToUser(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterAssignATaskToUser_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";
        String accountId = "SampleAccountId";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterAssignATaskToUser(inputMap);

        Assert.assertEquals("handleReenterAssignATaskToUser",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterAssignATaskToUser_BothInvalid() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-1";
        String accountId = "unknown-acc";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignATaskToUser(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).assignTask(anyString(), anyString());
    }

    @Test
    public void test_handleReenterAssignATaskToUser_InvalidTicket_ValidUser() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-1";
        String accountId = "acc-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(true);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignATaskToUser(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).assignTask(anyString(), anyString());
    }

    @Test
    public void test_handleReenterAssignATaskToUser_ValidTicket_InvalidUser() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String accountId = "unknown-acc";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignATaskToUser(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).assignTask(anyString(), anyString());
    }

    @Test
    public void test_handleReenterAssignATaskToUser_Success() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String accountId = "acc-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).assignTask(ticketId, accountId);
        Mockito.when(mockedIssueDao.getIssueFinal(ticketId)).thenReturn(new Issue());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignATaskToUser(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).assignTask(eq(ticketId), eq(accountId));
    }

    // =========================================================================
    // confirmReenterGetIssueByKey
    // =========================================================================

    @Test
    public void test_confirmReenterGetIssueByKey_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterGetIssueByKey(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterGetIssueByKey_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterGetIssueByKey(inputMap);

        Assert.assertEquals("handleReenterGetIssueByKey",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterGetIssueByKey_InvalidTicketId() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssueByKey(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).getIssueFinal(anyString());
    }

    @Test
    public void test_handleReenterGetIssueByKey_Success() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedIssueDao.getIssueFinal(ticketId)).thenReturn(new Issue());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssueByKey(inputMap);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("Issue"));
    }

    // =========================================================================
    // confirmReenterUpdatePriorityOfIssue
    // =========================================================================

    @Test
    public void test_confirmReenterUpdatePriorityOfIssue_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterUpdatePriorityOfIssue(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterUpdatePriorityOfIssue_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterUpdatePriorityOfIssue(inputMap);

        Assert.assertEquals("handleReenterUpdatePriorityOfIssue",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterUpdatePriorityOfIssue_InvalidTicketId() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterUpdatePriorityOfIssue(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).updatePriority(any(), anyString());
    }

    @Test
    public void test_handleReenterUpdatePriorityOfIssue_Success() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String priorityName = "High";

        Map<String, Object> state = new HashMap<>();
        state.put("Priority Name", priorityName);

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedIssueDao.updatePriority(any(), eq(ticketId))).thenReturn(new Issue());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterUpdatePriorityOfIssue(inputMap);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("Updated Issue"));
    }

    // =========================================================================
    // confirmReenterGetIssues
    // =========================================================================

    @Test
    public void test_confirmReenterGetIssues_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String projectKey = "PROJ";

        Mockito.when(mockedProjectDao.isProjectExist(projectKey)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", projectKey);
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterGetIssues(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterGetIssues_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String projectKey = "PROJ";

        Mockito.when(mockedProjectDao.isProjectExist(projectKey)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.PROJECT_KEY, projectKey));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", projectKey);
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterGetIssues(inputMap);

        Assert.assertEquals("handleReenterGetIssues",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterGetIssues_Success() {
        String stateId = UUID.randomUUID().toString();

        Map<String, Object> state = new HashMap<>();
        state.put("Status", "In Progress");
        state.put("Assignee", "acc-123");
        state.put("Priority", "High");
        state.put("Summary", "test");
        state.put("Page Size", 10.0);
        state.put("Page Number", 1.0);

        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), eq(10L), eq(1L))).thenReturn(List.of(new Issue()));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("List of Filtered Issues"));
    }

    @Test
    public void test_handleReenterGetIssues_DefaultPageSize() {
        String stateId = UUID.randomUUID().toString();

        Map<String, Object> state = new HashMap<>();
        state.put("Status", null);
        state.put("Assignee", null);
        state.put("Priority", null);
        state.put("Summary", null);
        state.put("Page Size", null);
        state.put("Page Number", null);

        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), eq(20L), eq(1L))).thenReturn(new ArrayList<>());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).searchByJQL(anyString(), eq(20L), eq(1L));
    }

    @Test
    public void test_handleReenterGetIssues_ExceptionThrown() {
        String stateId = UUID.randomUUID().toString();

        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenThrow(new RuntimeException("State retrieval failed"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
    }

    // =========================================================================
    // confirmReenterAssignIssueToEpic
    // =========================================================================

    @Test
    public void test_confirmReenterAssignIssueToEpic_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";
        String epicKey = "KE-EPIC-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterAssignIssueToEpic(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterAssignIssueToEpic_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";
        String epicKey = "KE-EPIC-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.TICKET_ID, ticketId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.confirmReenterAssignIssueToEpic(inputMap);

        Assert.assertEquals("handleReenterAssignIssueToEpic",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterAssignIssueToEpic_BothKeysInvalid() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-1";
        String epicKey = "INVALID-EPIC";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignIssueToEpic(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).assignIssueToEpic(anyString(), anyString());
    }

    @Test
    public void test_handleReenterAssignIssueToEpic_IssueKeyInvalid_EpicValid() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-1";
        String epicKey = "EPIC-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(true);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignIssueToEpic(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).assignIssueToEpic(anyString(), anyString());
    }

    @Test
    public void test_handleReenterAssignIssueToEpic_Success() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String epicKey = "EPIC-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(true);
        Mockito.doNothing().when(mockedIssueDao).assignIssueToEpic(ticketId, epicKey);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignIssueToEpic(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).assignIssueToEpic(eq(ticketId), eq(epicKey));
    }

    // =========================================================================
    // confirmReenterGetMyIssuesByGivenStatuses
    // =========================================================================

    @Test
    public void test_confirmReenterGetMyIssuesByGivenStatuses_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String statusName = "InvalidStatus";

        Mockito.when(mockedTicketInfoDao.getTransitions(anyString())).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAMES,
                        "[\"" + statusName + "\"]"));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put("Status Names", List.of(statusName));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterGetMyIssuesByGivenStatuses(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterGetMyIssuesByGivenStatuses_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String statusName = "InvalidStatus";

        Mockito.when(mockedTicketInfoDao.getTransitions(anyString())).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAMES,
                        "[\"" + statusName + "\"]"));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put("Status Names", List.of(statusName));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterGetMyIssuesByGivenStatuses(inputMap);

        Assert.assertEquals("handleReenterGetMyIssuesByGivenStatuses",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterGetMyIssuesByGivenStatuses_ValidationFails() {
        List<ValidationOrchestrator.ValidationResult> failedValidation = List.of(
                new ValidationOrchestrator.ValidationResult(
                        "Re-enter status.", "Status Names", "Enter valid statuses.", "Invalid status."));
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("getMyIssuesByGivenStatuses")))
                .thenReturn(failedValidation);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status Names", List.of("InvalidStatus"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterGetMyIssuesByGivenStatuses(inputMap);

        Assert.assertNotNull(response);
        Assert.assertEquals("handleReenterGetMyIssuesByGivenStatuses",
                response.getRemediationActions().getSubCatalogRequestName());
        Mockito.verify(mockedIssueDao, Mockito.never()).searchByJQL(anyString(), anyLong(), anyLong());
    }

    @Test
    public void test_handleReenterGetMyIssuesByGivenStatuses_Success() {
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("getMyIssuesByGivenStatuses")))
                .thenReturn(new ArrayList<>());
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), eq(30L), eq(1L))).thenReturn(List.of(new Issue()));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status Names", List.of("In Progress", "Done"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterGetMyIssuesByGivenStatuses(inputMap);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("Issues"));
        Mockito.verify(mockedIssueDao).searchByJQL(anyString(), eq(30L), eq(1L));
    }

    @Test
    public void test_handleReenterGetMyIssuesByGivenStatuses_ExceptionThrown() {
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("getMyIssuesByGivenStatuses")))
                .thenThrow(new RuntimeException("Validation error"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status Names", List.of("In Progress"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterGetMyIssuesByGivenStatuses(inputMap);

        Assert.assertNotNull(response);
    }

    // =========================================================================
    // confirmReenterUpdateStatusOfMultipleIssues
    // =========================================================================

    @Test
    public void test_confirmReenterUpdateStatusOfMultipleIssues_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String statusName = "InvalidStatus";

        Mockito.when(mockedTicketInfoDao.getTransitions(anyString())).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAMES,
                        "[\"" + statusName + "\"]"));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put("Status Name", statusName);
        inputMap.put("Issue Key", List.of("PROJ-1", "PROJ-2"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterUpdateStatusOfMultipleIssues(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterUpdateStatusOfMultipleIssues_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String statusName = "InvalidStatus";

        Mockito.when(mockedTicketInfoDao.getTransitions(anyString())).thenReturn(List.of());
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.STATUS_NAMES,
                        "[\"" + statusName + "\"]"));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put("Status Name", statusName);
        inputMap.put("Issue Key", List.of("PROJ-1", "PROJ-2"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterUpdateStatusOfMultipleIssues(inputMap);

        Assert.assertEquals("handleReenterUpdateStatusOfMultipleIssues",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterUpdateStatusOfMultipleIssues_ValidationFails() {
        List<ValidationOrchestrator.ValidationResult> failedValidation = List.of(
                new ValidationOrchestrator.ValidationResult(
                        "Re-enter.", "Issue Key", "Enter valid issue keys.", "Invalid issue keys."));
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("updateStatusOfMultipleIssues")))
                .thenReturn(failedValidation);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status Name", "Done");
        inputMap.put("Issue Key", List.of("INVALID-1"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterUpdateStatusOfMultipleIssues(inputMap);

        Assert.assertNotNull(response);
        Assert.assertEquals("handleReenterUpdateStatusOfMultipleIssues",
                response.getRemediationActions().getSubCatalogRequestName());
        Mockito.verify(mockedIssueDao, Mockito.never()).updateStatus(anyString(), anyString());
    }

    @Test
    public void test_handleReenterUpdateStatusOfMultipleIssues_Success() {
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("updateStatusOfMultipleIssues")))
                .thenReturn(new ArrayList<>());
        Mockito.doNothing().when(mockedIssueDao).updateStatus(anyString(), anyString());
        Mockito.when(mockedIssueDao.getIssueFinal(anyString())).thenReturn(new Issue());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status Name", "Done");
        inputMap.put("Issue Key", List.of("PROJ-1", "PROJ-2"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterUpdateStatusOfMultipleIssues(inputMap);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("Updated issues"));
        Mockito.verify(mockedIssueDao).updateStatus(eq("PROJ-1"), eq("Done"));
        Mockito.verify(mockedIssueDao).updateStatus(eq("PROJ-2"), eq("Done"));
    }

    @Test
    public void test_handleReenterUpdateStatusOfMultipleIssues_ExceptionThrown() {
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("updateStatusOfMultipleIssues")))
                .thenThrow(new RuntimeException("Validation error"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("Status Name", "Done");
        inputMap.put("Issue Key", List.of("PROJ-1"));

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterUpdateStatusOfMultipleIssues(inputMap);

        Assert.assertNotNull(response);
    }

    // =========================================================================
    // confirmReenterGetUserIssueByIssueType
    // =========================================================================

    @Test
    public void test_confirmReenterGetUserIssueByIssueType_Reenter_False() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "SampleAccountId";

        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", false);
        inputMap.put("stateId", stateId);
        inputMap.put("User account Id", accountId);
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterGetUserIssueByIssueType(inputMap);

        Assert.assertNull(response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof InformAPersonAction);
    }

    @Test
    public void test_confirmReenterGetUserIssueByIssueType_Reenter_True() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "SampleAccountId";

        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);
        List<ValidationOrchestrator.ValidationResult> validationResults =
                validationOrchestrator.validate(Map.of(Validator.ValidationResource.USER_ID, accountId));
        Mockito.when(mockedInternalStateManager.get(stateId))
                .thenReturn(Map.of("Validation Results", validationResults));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("re-enter", true);
        inputMap.put("stateId", stateId);
        inputMap.put("User account Id", accountId);
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.confirmReenterGetUserIssueByIssueType(inputMap);

        Assert.assertEquals("handleReenterGetUserIssueByIssueType",
                response.getRemediationActions().getSubCatalogRequestName());
        Assert.assertTrue(
                response.getRemediationActions().getRemediationActionList().get(0) instanceof AskAPersonAction);
    }

    @Test
    public void test_handleReenterGetUserIssueByIssueType_ValidationFails() {
        List<ValidationOrchestrator.ValidationResult> failedValidation = List.of(
                new ValidationOrchestrator.ValidationResult(
                        "Re-enter.", "User account Id", "Enter valid user account ID.", "Invalid user."));
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("getUserIssueByIssueType")))
                .thenReturn(failedValidation);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("User account Id", "unknown-acc");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterGetUserIssueByIssueType(inputMap);

        Assert.assertNotNull(response);
        Assert.assertEquals("handleReenterGetUserIssueByIssueType",
                response.getRemediationActions().getSubCatalogRequestName());
        Mockito.verify(mockedIssueDao, Mockito.never()).searchByJQL(anyString(), anyLong(), anyLong());
    }

    @Test
    public void test_handleReenterGetUserIssueByIssueType_Success() {
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("getUserIssueByIssueType")))
                .thenReturn(new ArrayList<>());
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), eq(30L), eq(1L))).thenReturn(List.of(new Issue()));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("User account Id", "acc-123");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterGetUserIssueByIssueType(inputMap);

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponseValue().containsKey("Issues"));
        Mockito.verify(mockedIssueDao).searchByJQL(anyString(), eq(30L), eq(1L));
    }

    @Test
    public void test_handleReenterGetUserIssueByIssueType_ExceptionThrown() {
        Mockito.when(mockValidationOrchestrator.validate(any(), eq(RitualsArea.class), eq("getUserIssueByIssueType")))
                .thenThrow(new RuntimeException("Validation error"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", UUID.randomUUID().toString());
        inputMap.put("User account Id", "acc-123");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterGetUserIssueByIssueType(inputMap);

        Assert.assertNotNull(response);
    }

    // =========================================================================
    // handleReenterForGetUserIssues - failure path (user not exist)
    // =========================================================================

    @Test
    public void test_handleReenterForGetUserIssues_UserNotExist() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "unknown-acc";

        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForGetUserIssues(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).searchByJQL(anyString(), anyLong(), anyLong());
    }

    // =========================================================================
    // handleReenterForAddAComment - invalid ticket path
    // =========================================================================

    @Test
    public void test_handleReenterForAddAComment_InvalidTicket() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-99";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put("Comment", "A comment");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForAddAComment(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).addComment(anyString(), anyString(), any());
    }

    // =========================================================================
    // handleReenterForUpdateStatusOfIssue - invalid ticket path
    // =========================================================================

    @Test
    public void test_handleReenterForUpdateStatusOfIssue_InvalidTicket() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-99";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.STATUS_NAME, "Done");

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterForUpdateStatusOfIssue(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).updateStatus(anyString(), anyString());
    }

    // =========================================================================
    // Exception catch block coverage tests (L113-114, L160-161, L209-210,
    // L264-265, L310-311, L360-361, L479-480)
    // =========================================================================

    @Test
    public void test_handleReenterForAddAComment_Exception() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "KE-123";
        String comment = "Hello";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("DB error"))
                .when(mockedIssueDao).addComment(anyString(), anyString(), any());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put("Comment", comment);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForAddAComment(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterForGetUserIssues_Exception() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "acc-valid";

        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(true);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("JQL search failed"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForGetUserIssues(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterForUpdateStatusOfIssue_Exception() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String statusName = "Done";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("Update failed"))
                .when(mockedIssueDao).updateStatus(anyString(), anyString());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.STATUS_NAME, statusName);

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterForUpdateStatusOfIssue(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterAssignATaskToUser_Exception() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String accountId = "acc-123";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("Assign failed"))
                .when(mockedIssueDao).assignTask(anyString(), anyString());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignATaskToUser(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterGetIssueByKey_Exception() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedIssueDao.getIssueFinal(ticketId))
                .thenThrow(new RuntimeException("Get issue failed"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssueByKey(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterUpdatePriorityOfIssue_Exception() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String priorityName = "High";

        Map<String, Object> state = new HashMap<>();
        state.put("Priority Name", priorityName);

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedIssueDao.updatePriority(any(), eq(ticketId)))
                .thenThrow(new RuntimeException("Update priority failed"));

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);

        ExtensionResponse response =
                mockedRitualsAreaSubCatalogRequests.handleReenterUpdatePriorityOfIssue(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterAssignIssueToEpic_Exception() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String epicKey = "EPIC-1";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(true);
        Mockito.doThrow(new RuntimeException("Assign epic failed"))
                .when(mockedIssueDao).assignIssueToEpic(anyString(), anyString());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignIssueToEpic(inputMap);

        Assert.assertNotNull(response);
    }

    // =========================================================================
    // Null-guard early return coverage (L109, L157, L422, L476)
    // =========================================================================

    @Test
    public void test_handleReenterForAddAComment_InvalidTicket_EarlyReturn() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "INVALID-999";

        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put("Comment", "hello");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForAddAComment(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).addComment(anyString(), anyString(), any());
    }

    @Test
    public void test_handleReenterForGetUserIssues_InvalidAccount_EarlyReturn() {
        String stateId = UUID.randomUUID().toString();
        String accountId = "INVALID-ACC";

        Mockito.when(mockedUserDao.isUserExist(accountId)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.ACCOUNT_ID, accountId);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterForGetUserIssues(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).searchByJQL(anyString(), anyLong(), anyLong());
    }

    @Test
    public void test_handleReenterGetIssues_PositivePageSize() {
        String stateId = UUID.randomUUID().toString();

        Map<String, Object> state = new HashMap<>();
        state.put("Status", "In Progress");
        state.put("Assignee", null);
        state.put("Priority", null);
        state.put("Summary", null);
        state.put("Page Size", 5.0);
        state.put("Page Number", 2.0);

        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), eq(5L), eq(2L))).thenReturn(new ArrayList<>());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao).searchByJQL(anyString(), eq(5L), eq(2L));
    }

    @Test
    public void test_handleReenterAssignIssueToEpic_ValidTicket_InvalidEpicKey_EarlyReturn() {
        String stateId = UUID.randomUUID().toString();
        String ticketId = "PROJ-1";
        String epicKey = "INVALID-EPIC";

        // Both checks pass the "both invalid" guard (at least one exists)
        Mockito.when(mockedIssueDao.isIssueExists(ticketId)).thenReturn(true);
        Mockito.when(mockedIssueDao.isIssueExists(epicKey)).thenReturn(false);

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put(JiraResources.TICKET_ID, ticketId);
        inputMap.put(JiraResources.EPIC_KEY, epicKey);

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterAssignIssueToEpic(inputMap);

        Assert.assertNotNull(response);
        Mockito.verify(mockedIssueDao, Mockito.never()).assignIssueToEpic(anyString(), anyString());
    }

    // =========================================================================
    // Validation helper method coverage (L526-560)
    // =========================================================================

    @Test
    public void test_handleReenterGetIssues_InvalidProjectKey_projectKeyErrorResponse() {
        String stateId = UUID.randomUUID().toString();

        Map<String, Object> state = new HashMap<>();
        state.put("Status", null);
        state.put("Assignee", null);
        state.put("Priority", null);
        state.put("Summary", null);
        state.put("Page Size", null);
        state.put("Page Number", null);

        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedProjectDao.isProjectExist("BAD_PROJ")).thenReturn(false);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(new ArrayList<>());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "BAD_PROJ");
        inputMap.put("Issue Type", "Bug");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterGetIssues_InvalidIssueType_issueTypeValidator() {
        String stateId = UUID.randomUUID().toString();

        Map<String, Object> state = new HashMap<>();
        state.put("Status", null);
        state.put("Assignee", null);
        state.put("Priority", null);
        state.put("Summary", null);
        state.put("Page Size", null);
        state.put("Page Number", null);

        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedProjectDao.isProjectExist("PROJ")).thenReturn(true);
        Mockito.when(mockedIssueDao.validateIssueType("PROJ", "BadType")).thenReturn(false);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(new ArrayList<>());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "PROJ");
        inputMap.put("Issue Type", "BadType");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
    }

    @Test
    public void test_handleReenterGetIssues_BothInvalid_projectKeyIssueTypeValidator() {
        String stateId = UUID.randomUUID().toString();

        Map<String, Object> state = new HashMap<>();
        state.put("Status", null);
        state.put("Assignee", null);
        state.put("Priority", null);
        state.put("Summary", null);
        state.put("Page Size", null);
        state.put("Page Number", null);

        Mockito.when(mockedInternalStateManager.get(stateId)).thenReturn(state);
        Mockito.when(mockedProjectDao.isProjectExist("BAD_PROJ")).thenReturn(false);
        Mockito.when(mockedIssueDao.validateIssueType("BAD_PROJ", "BadType")).thenReturn(false);
        Mockito.when(mockedIssueDao.searchByJQL(anyString(), anyLong(), anyLong())).thenReturn(new ArrayList<>());

        Map<String, Object> inputMap = new HashMap<>();
        inputMap.put("stateId", stateId);
        inputMap.put("Project Key", "BAD_PROJ");
        inputMap.put("Issue Type", "BadType");

        ExtensionResponse response = mockedRitualsAreaSubCatalogRequests.handleReenterGetIssues(inputMap);

        Assert.assertNotNull(response);
    }

}