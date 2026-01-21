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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;

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

}