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

import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketStatus;
import app.krista.extensions.devsecops.development.jira.impl.TicketInfoDao;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class StatusNamesValidatorTest {

    private StatusNamesValidator validator;
    private TicketInfoDao mockTicketInfoDao;
    private Gson gson = new Gson();

    @Before
    public void setUp() {
        mockTicketInfoDao = Mockito.mock(TicketInfoDao.class);
        validator = new StatusNamesValidator(mockTicketInfoDao);
        
        // Setup mock statuses
        TicketStatus status1 = new TicketStatus();
        status1.name = "To Do";
        status1.id = "1";
        
        TicketStatus status2 = new TicketStatus();
        status2.name = "In Progress";
        status2.id = "2";
        
        TicketStatus status3 = new TicketStatus();
        status3.name = "Done";
        status3.id = "3";
        
        when(mockTicketInfoDao.getStatuses()).thenReturn(Arrays.asList(status1, status2, status3));
    }

    @Test
    public void testValidate_ValidStatusNames_ReturnsTrue() {
        List<String> validStatuses = Arrays.asList("To Do", "In Progress", "Done");
        String statusNamesJson = gson.toJson(validStatuses);
        
        boolean result = validator.validate(statusNamesJson, Map.of());
        
        assertTrue("Should validate valid status names", result);
    }

    @Test
    public void testValidate_InvalidStatusName_ReturnsFalse() {
        List<String> invalidStatuses = Arrays.asList("To Do", "Invalid Status", "Done");
        String statusNamesJson = gson.toJson(invalidStatuses);
        
        boolean result = validator.validate(statusNamesJson, Map.of());
        
        assertFalse("Should reject invalid status names", result);
    }

    @Test
    public void testValidate_DuplicateStatusNames_ReturnsFalse() {
        List<String> duplicateStatuses = Arrays.asList("To Do", "In Progress", "to do");
        String statusNamesJson = gson.toJson(duplicateStatuses);
        
        boolean result = validator.validate(statusNamesJson, Map.of());
        
        assertFalse("Should reject duplicate status names (case insensitive)", result);
    }

    @Test
    public void testValidate_EmptyList_ReturnsFalse() {
        List<String> emptyStatuses = Arrays.asList();
        String statusNamesJson = gson.toJson(emptyStatuses);
        
        boolean result = validator.validate(statusNamesJson, Map.of());
        
        assertFalse("Should reject empty status list", result);
    }

    @Test
    public void testValidate_NullList_ReturnsFalse() {
        String statusNamesJson = gson.toJson(null);
        
        boolean result = validator.validate(statusNamesJson, Map.of());
        
        assertFalse("Should reject null status list", result);
    }

    @Test
    public void testGetFetchFieldName() {
        String fieldName = validator.getFetchFieldName();
        assertEquals("Status Names", fieldName);
    }

    @Test
    public void testGetFetchStepMessage() {
        String message = validator.getFetchStepMessage();
        assertTrue("Should contain available statuses", message.contains("To Do"));
        assertTrue("Should contain available statuses", message.contains("In Progress"));
        assertTrue("Should contain available statuses", message.contains("Done"));
    }

    @Test
    public void testGetConfirmationStepMessage_InvalidStatus() {
        List<String> invalidStatuses = Arrays.asList("To Do", "Invalid Status");
        String statusNamesJson = gson.toJson(invalidStatuses);
        
        String message = validator.getConfirmationStepMessage(statusNamesJson, Map.of());
        
        assertTrue("Should mention invalid status", message.contains("Invalid Status"));
    }

    @Test
    public void testGetErrMessage() {
        List<String> statuses = Arrays.asList("To Do", "Invalid");
        String statusNamesJson = gson.toJson(statuses);
        
        String message = validator.getErrMessage(statusNamesJson);
        
        assertTrue("Should contain status names", message.contains("To Do"));
        assertTrue("Should contain status names", message.contains("Invalid"));
    }
}
