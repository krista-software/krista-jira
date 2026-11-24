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

import app.krista.extensions.devsecops.development.jira.impl.IssueDao;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class IssueKeysValidatorTest {

    @Mock
    private IssueDao issueDao;

    private IssueKeysValidator validator;
    private Gson gson;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new IssueKeysValidator(issueDao);
        gson = new Gson();
    }

    @Test
    public void testValidate_ValidIssueKeys_ReturnsTrue() {
        List<String> issueKeys = Arrays.asList("PROJ-123", "PROJ-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        when(issueDao.isIssueExists("PROJ-123")).thenReturn(true);
        when(issueDao.isIssueExists("PROJ-456")).thenReturn(true);
        
        boolean result = validator.validate(issueKeysJson, Map.of());
        
        assertTrue("Should accept valid issue keys", result);
        verify(issueDao).isIssueExists("PROJ-123");
        verify(issueDao).isIssueExists("PROJ-456");
    }

    @Test
    public void testValidate_InvalidIssueKey_ReturnsFalse() {
        List<String> issueKeys = Arrays.asList("PROJ-123", "INVALID-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        when(issueDao.isIssueExists("PROJ-123")).thenReturn(true);
        when(issueDao.isIssueExists("INVALID-456")).thenReturn(false);
        
        boolean result = validator.validate(issueKeysJson, Map.of());
        
        assertFalse("Should reject invalid issue keys", result);
    }

    @Test
    public void testValidate_DuplicateIssueKeys_ReturnsFalse() {
        List<String> issueKeys = Arrays.asList("PROJ-123", "PROJ-123");
        String issueKeysJson = gson.toJson(issueKeys);
        
        boolean result = validator.validate(issueKeysJson, Map.of());
        
        assertFalse("Should reject duplicate issue keys", result);
    }

    @Test
    public void testValidate_EmptyList_ReturnsFalse() {
        List<String> issueKeys = Collections.emptyList();
        String issueKeysJson = gson.toJson(issueKeys);
        
        boolean result = validator.validate(issueKeysJson, Map.of());
        
        assertFalse("Should reject empty issue keys list", result);
    }

    @Test
    public void testValidate_NullList_ReturnsFalse() {
        String issueKeysJson = gson.toJson(null);
        
        boolean result = validator.validate(issueKeysJson, Map.of());
        
        assertFalse("Should reject null issue keys list", result);
    }

    @Test
    public void testGetFetchFieldName() {
        String fieldName = validator.getFetchFieldName();
        assertEquals("Issue Key", fieldName);
    }

    @Test
    public void testGetFetchStepMessage() {
        String message = validator.getFetchStepMessage();
        assertTrue("Should contain helpful message", message.contains("valid issue keys"));
    }

    @Test
    public void testGetConfirmationStepMessage_WithInvalidKeys() {
        List<String> issueKeys = Arrays.asList("PROJ-123", "INVALID-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        when(issueDao.isIssueExists("PROJ-123")).thenReturn(true);
        when(issueDao.isIssueExists("INVALID-456")).thenReturn(false);
        
        String message = validator.getConfirmationStepMessage(issueKeysJson, Map.of());
        
        assertTrue("Should mention invalid keys", message.contains("INVALID-456"));
        assertTrue("Should contain helpful message", message.contains("could not be found"));
    }

    @Test
    public void testGetErrMessage() {
        List<String> issueKeys = Arrays.asList("PROJ-123", "INVALID-456");
        String issueKeysJson = gson.toJson(issueKeys);
        
        String message = validator.getErrMessage(issueKeysJson);
        
        assertTrue("Should contain error message", message.contains("Invalid issue keys"));
        assertTrue("Should contain the keys", message.contains("PROJ-123"));
        assertTrue("Should contain the keys", message.contains("INVALID-456"));
    }
}
