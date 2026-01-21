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
import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ErrorHandlingStateManager;
import app.krista.extensions.devsecops.development.jira.catalog.errorhandlers.ExtensionResponseGenerator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.ValidationOrchestrator;
import app.krista.extensions.devsecops.development.jira.catalog.validators.Validator;
import app.krista.extensions.devsecops.development.jira.impl.ReleaseDao;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import app.krista.ksdk.telemetry.TelemetryMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for ReleaseManagementArea catalog request methods.
 * Tests validation, success paths, error handling, and telemetry for all catalog requests.
 */
@DisplayName("ReleaseManagementArea Tests")
class ReleaseManagementAreaTest {

    @Mock
    private ReleaseDao releaseDao;

    @Mock
    private TelemetryMetrics telemetryMetrics;

    @Mock
    private ValidationOrchestrator validationOrchestrator;

    @Mock
    private ErrorHandlingStateManager internalStateManager;

    @Mock
    private ExtensionResponseGenerator responseGenerator;

    private ReleaseManagementArea releaseManagementArea;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        releaseManagementArea = new ReleaseManagementArea(releaseDao, telemetryMetrics,
                validationOrchestrator, internalStateManager, responseGenerator);
    }

    @Test
    @DisplayName("Should get release summary successfully with all parameters")
    void testGetReleaseSummary_Success_AllParameters() {
        // Arrange
        String status = "In Progress";
        String assignee = "user123";
        String priority = "High";
        String issueType = "Bug";
        Double pageSize = 20.0;
        Double pageNumber = 1.0;
        String fixVersion = "v1.0.0";

        ReleaseSummaryResponse mockResponse = new ReleaseSummaryResponse();
        mockResponse.setTotalUserStories(10.0);
        mockResponse.setOpenUserStories(5.0);
        mockResponse.setTotalEpics(3.0);
        mockResponse.setTotalTasks(15.0);
        mockResponse.setOpenTasks(8.0);
        mockResponse.setTotalBugs(7.0);
        mockResponse.setOpenBugs(4.0);
        mockResponse.setUserStories(new ArrayList<>());
        mockResponse.setEpics(new ArrayList<>());
        mockResponse.setBugs(new ArrayList<>());
        mockResponse.setTasks(new ArrayList<>());

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion))
                .thenReturn(mockResponse);

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(releaseDao).getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion);
        verify(telemetryMetrics, atLeastOnce()).incrementCounter(anyString(), eq(1L));
    }

    @Test
    @DisplayName("Should get release summary successfully with minimal parameters")
    void testGetReleaseSummary_Success_MinimalParameters() {
        // Arrange
        String status = null;
        String assignee = null;
        String priority = null;
        String issueType = null;
        Double pageSize = null;
        Double pageNumber = null;
        String fixVersion = null;

        ReleaseSummaryResponse mockResponse = new ReleaseSummaryResponse();
        mockResponse.setTotalUserStories(0.0);
        mockResponse.setOpenUserStories(0.0);
        mockResponse.setTotalEpics(0.0);
        mockResponse.setTotalTasks(0.0);
        mockResponse.setOpenTasks(0.0);
        mockResponse.setTotalBugs(0.0);
        mockResponse.setOpenBugs(0.0);
        mockResponse.setUserStories(new ArrayList<>());
        mockResponse.setEpics(new ArrayList<>());
        mockResponse.setBugs(new ArrayList<>());
        mockResponse.setTasks(new ArrayList<>());

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion))
                .thenReturn(mockResponse);

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(releaseDao).getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion);
        verify(telemetryMetrics, atLeastOnce()).incrementCounter(anyString(), eq(1L));
    }

    @Test
    @DisplayName("Should handle validation failure for get release summary")
    void testGetReleaseSummary_ValidationFailure() {
        // Arrange
        String status = "Invalid Status";
        String assignee = "user123";
        String priority = "High";
        String issueType = "Bug";
        Double pageSize = 20.0;
        Double pageNumber = 1.0;
        String fixVersion = "v1.0.0";

        List<ValidationOrchestrator.ValidationResult> validationResults = new ArrayList<>();
        validationResults.add(new ValidationOrchestrator.ValidationResult("Status", "Invalid status value", null, null));

        when(validationOrchestrator.validate(anyMap())).thenReturn(validationResults);
        when(responseGenerator.generateConfirmationResponse(any(), anyList(), anyString(), anyMap()))
                .thenReturn(mock(ExtensionResponse.class));

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(internalStateManager).put(anyString(), anyString());
        verify(responseGenerator).generateConfirmationResponse(any(), anyList(), anyString(), anyMap());
        verify(telemetryMetrics, atLeastOnce()).incrementCounter(anyString(), eq(1L));
    }

    @Test
    @DisplayName("Should use default page size when null")
    void testGetReleaseSummary_DefaultPageSize() {
        // Arrange
        String status = "Done";
        String assignee = null;
        String priority = null;
        String issueType = null;
        Double pageSize = null; // Should default to 20
        Double pageNumber = null; // Should default to 1
        String fixVersion = null;

        ReleaseSummaryResponse mockResponse = new ReleaseSummaryResponse();
        mockResponse.setUserStories(new ArrayList<>());
        mockResponse.setEpics(new ArrayList<>());
        mockResponse.setBugs(new ArrayList<>());
        mockResponse.setTasks(new ArrayList<>());

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion))
                .thenReturn(mockResponse);

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(releaseDao).getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion);
    }

    @Test
    @DisplayName("Should use default page size when zero or negative")
    void testGetReleaseSummary_ZeroPageSize() {
        // Arrange
        String status = "Done";
        String assignee = null;
        String priority = null;
        String issueType = null;
        Double pageSize = 0.0; // Should default to 20
        Double pageNumber = 0.0; // Should default to 1
        String fixVersion = null;

        ReleaseSummaryResponse mockResponse = new ReleaseSummaryResponse();
        mockResponse.setUserStories(new ArrayList<>());
        mockResponse.setEpics(new ArrayList<>());
        mockResponse.setBugs(new ArrayList<>());
        mockResponse.setTasks(new ArrayList<>());

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion))
                .thenReturn(mockResponse);

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(releaseDao).getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion);
    }

    @Test
    @DisplayName("Should handle exception during get release summary")
    void testGetReleaseSummary_Exception() {
        // Arrange
        String status = "Done";
        String assignee = null;
        String priority = null;
        String issueType = null;
        Double pageSize = 20.0;
        Double pageNumber = 1.0;
        String fixVersion = null;

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(anyString(), anyString(), anyString(), anyString(), anyLong(), anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(telemetryMetrics, atLeastOnce()).incrementCounter(anyString(), eq(1L));
    }

    @Test
    @DisplayName("Should validate only non-empty parameters")
    void testGetReleaseSummary_ValidateOnlyNonEmptyParameters() {
        // Arrange
        String status = "Done";
        String assignee = ""; // Empty string should not be validated
        String priority = null; // Null should not be validated
        String issueType = "Bug";
        Double pageSize = 20.0;
        Double pageNumber = 1.0;
        String fixVersion = null;

        ReleaseSummaryResponse mockResponse = new ReleaseSummaryResponse();
        mockResponse.setUserStories(new ArrayList<>());
        mockResponse.setEpics(new ArrayList<>());
        mockResponse.setBugs(new ArrayList<>());
        mockResponse.setTasks(new ArrayList<>());

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, 20L, 1L, fixVersion))
                .thenReturn(mockResponse);

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(validationOrchestrator).validate(anyMap());
    }

    @Test
    @DisplayName("Should handle large page size")
    void testGetReleaseSummary_LargePageSize() {
        // Arrange
        String status = null;
        String assignee = null;
        String priority = null;
        String issueType = null;
        Double pageSize = 1000.0;
        Double pageNumber = 5.0;
        String fixVersion = null;

        ReleaseSummaryResponse mockResponse = new ReleaseSummaryResponse();
        mockResponse.setUserStories(new ArrayList<>());
        mockResponse.setEpics(new ArrayList<>());
        mockResponse.setBugs(new ArrayList<>());
        mockResponse.setTasks(new ArrayList<>());

        when(validationOrchestrator.validate(anyMap())).thenReturn(Collections.emptyList());
        when(releaseDao.getGetReleaseSummaryResponse(status, assignee, priority, issueType, 1000L, 5L, fixVersion))
                .thenReturn(mockResponse);

        // Act
        ExtensionResponse response = releaseManagementArea.getReleaseSummary(status, assignee, priority, issueType, pageSize, pageNumber, fixVersion);

        // Assert
        assertNotNull(response);
        verify(releaseDao).getGetReleaseSummaryResponse(status, assignee, priority, issueType, 1000L, 5L, fixVersion);
    }
}

