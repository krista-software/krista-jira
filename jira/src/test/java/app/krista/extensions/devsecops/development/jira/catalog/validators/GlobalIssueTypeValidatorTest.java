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

import app.krista.extensions.devsecops.development.jira.catalog.entities.TicketProject;
import app.krista.extensions.devsecops.development.jira.impl.ProjectDao;
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

public class GlobalIssueTypeValidatorTest {

    @Mock
    private ProjectDao projectDao;

    private GlobalIssueTypeValidator validator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new GlobalIssueTypeValidator(projectDao);
    }

    @Test
    public void testValidate_ValidIssueType_ReturnsTrue() {
        // Setup projects
        TicketProject project1 = new TicketProject();
        project1.key = "PROJ1";
        TicketProject project2 = new TicketProject();
        project2.key = "PROJ2";
        
        when(projectDao.getAllProjects()).thenReturn(Arrays.asList(project1, project2));
        
        // Setup issue types for projects
        String issueTypesJson1 = "[{\"name\":\"Bug\",\"id\":\"1\"},{\"name\":\"Story\",\"id\":\"2\"}]";
        String issueTypesJson2 = "[{\"name\":\"Task\",\"id\":\"3\"},{\"name\":\"Epic\",\"id\":\"4\"}]";
        
        when(projectDao.getIssueTypeFromProject("PROJ1")).thenReturn(issueTypesJson1);
        when(projectDao.getIssueTypeFromProject("PROJ2")).thenReturn(issueTypesJson2);
        
        boolean result = validator.validate("Bug", Map.of());
        
        assertTrue("Should accept valid issue type", result);
    }

    @Test
    public void testValidate_ValidIssueTypeCaseInsensitive_ReturnsTrue() {
        // Setup projects
        TicketProject project1 = new TicketProject();
        project1.key = "PROJ1";
        
        when(projectDao.getAllProjects()).thenReturn(Arrays.asList(project1));
        
        // Setup issue types
        String issueTypesJson = "[{\"name\":\"Bug\",\"id\":\"1\"},{\"name\":\"Story\",\"id\":\"2\"}]";
        when(projectDao.getIssueTypeFromProject("PROJ1")).thenReturn(issueTypesJson);
        
        boolean result = validator.validate("bug", Map.of()); // lowercase
        
        assertTrue("Should accept valid issue type regardless of case", result);
    }

    @Test
    public void testValidate_InvalidIssueType_ReturnsFalse() {
        // Setup projects
        TicketProject project1 = new TicketProject();
        project1.key = "PROJ1";
        
        when(projectDao.getAllProjects()).thenReturn(Arrays.asList(project1));
        
        // Setup issue types
        String issueTypesJson = "[{\"name\":\"Bug\",\"id\":\"1\"},{\"name\":\"Story\",\"id\":\"2\"}]";
        when(projectDao.getIssueTypeFromProject("PROJ1")).thenReturn(issueTypesJson);
        
        boolean result = validator.validate("InvalidType", Map.of());
        
        assertFalse("Should reject invalid issue type", result);
    }

    @Test
    public void testValidate_EmptyIssueType_ReturnsFalse() {
        boolean result = validator.validate("", Map.of());
        
        assertFalse("Should reject empty issue type", result);
    }

    @Test
    public void testValidate_WhitespaceIssueType_ReturnsFalse() {
        boolean result = validator.validate("   ", Map.of());
        
        assertFalse("Should reject whitespace-only issue type", result);
    }

    @Test
    public void testValidate_NoProjects_ReturnsFalse() {
        when(projectDao.getAllProjects()).thenReturn(Collections.emptyList());
        
        boolean result = validator.validate("Bug", Map.of());
        
        assertFalse("Should reject when no projects are available", result);
    }

    @Test
    public void testGetFetchFieldName() {
        String fieldName = validator.getFetchFieldName();
        assertEquals("Issue Type", fieldName);
    }

    @Test
    public void testGetFetchStepMessage() {
        String message = validator.getFetchStepMessage();
        assertTrue("Should contain helpful message", message.contains("valid issue type"));
        assertTrue("Should mention accessible projects", message.contains("accessible projects"));
    }

    @Test
    public void testGetConfirmationStepMessage_WithAvailableTypes() {
        // Setup projects
        TicketProject project1 = new TicketProject();
        project1.key = "PROJ1";
        
        when(projectDao.getAllProjects()).thenReturn(Arrays.asList(project1));
        
        // Setup issue types
        String issueTypesJson = "[{\"name\":\"Bug\",\"id\":\"1\"},{\"name\":\"Story\",\"id\":\"2\"},{\"name\":\"Task\",\"id\":\"3\"}]";
        when(projectDao.getIssueTypeFromProject("PROJ1")).thenReturn(issueTypesJson);
        
        String message = validator.getConfirmationStepMessage("InvalidType", Map.of());
        
        assertTrue("Should mention the invalid type", message.contains("InvalidType"));
        assertTrue("Should mention available types", message.contains("Bug"));
        assertTrue("Should mention available types", message.contains("Story"));
        assertTrue("Should mention available types", message.contains("Task"));
        assertTrue("Should contain helpful message", message.contains("does not exist"));
    }

    @Test
    public void testGetErrMessage() {
        String message = validator.getErrMessage("InvalidType");
        
        assertTrue("Should contain error message", message.contains("Invalid issue type"));
        assertTrue("Should contain the type", message.contains("InvalidType"));
    }

    @Test
    public void testValidate_ProjectWithNoIssueTypes_ContinuesWithOtherProjects() {
        // Setup projects
        TicketProject project1 = new TicketProject();
        project1.key = "PROJ1";
        TicketProject project2 = new TicketProject();
        project2.key = "PROJ2";
        
        when(projectDao.getAllProjects()).thenReturn(Arrays.asList(project1, project2));
        
        // First project throws exception, second project has valid issue types
        when(projectDao.getIssueTypeFromProject("PROJ1")).thenThrow(new RuntimeException("Project not accessible"));
        String issueTypesJson2 = "[{\"name\":\"Bug\",\"id\":\"1\"},{\"name\":\"Story\",\"id\":\"2\"}]";
        when(projectDao.getIssueTypeFromProject("PROJ2")).thenReturn(issueTypesJson2);
        
        boolean result = validator.validate("Bug", Map.of());
        
        assertTrue("Should find issue type in accessible project even if other projects fail", result);
    }
}
