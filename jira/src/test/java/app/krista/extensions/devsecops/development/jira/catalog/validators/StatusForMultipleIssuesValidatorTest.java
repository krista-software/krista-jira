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

package app.krista.extensions.devsecops.development.jira.catalog.validators;

import app.krista.extensions.devsecops.development.jira.catalog.entities.TransitionStatus;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class StatusForMultipleIssuesValidatorTest {

    @Mock
    private TicketInfoDao ticketInfoDao;

    private StatusForMultipleIssuesValidator validator;
    private Gson gson;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new StatusForMultipleIssuesValidator(ticketInfoDao);
        gson = new Gson();
    }

    @Test
    public void testValidate_ValidStatusForAllIssues_ReturnsTrue() {
        String statusName = "In Progress";
        List<String> issueKeys = Arrays.asList("PROJ-123", "PROJ-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.ISSUE_KEYS, issueKeysJson
        );
        
        // Mock transitions for PROJ-123
        TransitionStatus transition1 = new TransitionStatus();
        transition1.toName = "In Progress";
        when(ticketInfoDao.getTransitions("PROJ-123")).thenReturn(Arrays.asList(transition1));
        
        // Mock transitions for PROJ-456
        TransitionStatus transition2 = new TransitionStatus();
        transition2.toName = "In Progress";
        when(ticketInfoDao.getTransitions("PROJ-456")).thenReturn(Arrays.asList(transition2));
        
        boolean result = validator.validate(statusName, context);
        
        assertTrue("Should accept valid status for all issues", result);
    }

    @Test
    public void testValidate_InvalidStatusForOneIssue_ReturnsFalse() {
        String statusName = "In Progress";
        List<String> issueKeys = Arrays.asList("PROJ-123", "PROJ-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.ISSUE_KEYS, issueKeysJson
        );
        
        // Mock transitions for PROJ-123 (valid)
        TransitionStatus transition1 = new TransitionStatus();
        transition1.toName = "In Progress";
        when(ticketInfoDao.getTransitions("PROJ-123")).thenReturn(Arrays.asList(transition1));
        
        // Mock transitions for PROJ-456 (invalid - no "In Progress" transition)
        TransitionStatus transition2 = new TransitionStatus();
        transition2.toName = "Done";
        when(ticketInfoDao.getTransitions("PROJ-456")).thenReturn(Arrays.asList(transition2));
        
        boolean result = validator.validate(statusName, context);
        
        assertFalse("Should reject status that cannot be applied to all issues", result);
    }

    @Test
    public void testValidate_EmptyStatusName_ReturnsFalse() {
        String statusName = "";
        List<String> issueKeys = Arrays.asList("PROJ-123");
        String issueKeysJson = gson.toJson(issueKeys);
        
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.ISSUE_KEYS, issueKeysJson
        );
        
        boolean result = validator.validate(statusName, context);
        
        assertFalse("Should reject empty status name", result);
    }

    @Test
    public void testValidate_NoIssueKeysInContext_ReturnsFalse() {
        String statusName = "In Progress";
        Map<Validator.ValidationResource, String> context = Map.of();
        
        boolean result = validator.validate(statusName, context);
        
        assertFalse("Should reject when no issue keys provided", result);
    }

    @Test
    public void testGetFetchFieldName() {
        String fieldName = validator.getFetchFieldName();
        assertEquals("Status Name", fieldName);
    }

    @Test
    public void testGetFetchStepMessage() {
        String message = validator.getFetchStepMessage();
        assertTrue("Should contain helpful message", message.contains("valid status name"));
        assertTrue("Should mention all issues", message.contains("all selected issues"));
    }

    @Test
    public void testGetConfirmationStepMessage_WithInvalidIssues() {
        String statusName = "In Progress";
        List<String> issueKeys = Arrays.asList("PROJ-123", "PROJ-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        Map<Validator.ValidationResource, String> context = Map.of(
            Validator.ValidationResource.ISSUE_KEYS, issueKeysJson
        );
        
        // Mock transitions for PROJ-123 (valid)
        TransitionStatus transition1 = new TransitionStatus();
        transition1.toName = "In Progress";
        when(ticketInfoDao.getTransitions("PROJ-123")).thenReturn(Arrays.asList(transition1));
        
        // Mock transitions for PROJ-456 (invalid)
        TransitionStatus transition2 = new TransitionStatus();
        transition2.toName = "Done";
        transition2.fromId = "1";
        when(ticketInfoDao.getTransitions("PROJ-456")).thenReturn(Arrays.asList(transition2));
        
        String message = validator.getConfirmationStepMessage(statusName, context);
        
        assertTrue("Should mention the status", message.contains("In Progress"));
        assertTrue("Should mention invalid issues", message.contains("PROJ-456"));
        assertTrue("Should mention available transitions", message.contains("Done"));
    }

    @Test
    public void testGetErrMessage() {
        String statusName = "Invalid Status";
        String message = validator.getErrMessage(statusName);
        
        assertTrue("Should contain error message", message.contains("Invalid status name"));
        assertTrue("Should contain the status", message.contains("Invalid Status"));
    }
}
