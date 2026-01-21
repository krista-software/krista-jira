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

import app.krista.extensions.devsecops.development.jira.catalog.entities.Issue;
import app.krista.extensions.devsecops.development.jira.services.ReleaseSummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test suite for ReleaseDao class.
 * Tests all public methods with various scenarios.
 */
@DisplayName("ReleaseDao Tests")
class ReleaseDaoTest {

    @Mock
    private IssueDao issueDao;

    private ReleaseDao releaseDao;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        releaseDao = new ReleaseDao(issueDao);
    }

    @Test
    @DisplayName("Should get release summary for all issue types when issueType is null")
    void testGetReleaseSummary_AllIssueTypes() {
        // Arrange
        Issue story1 = createIssue("STORY-1", "Story");
        Issue story2 = createIssue("STORY-2", "Story");
        Issue task1 = createIssue("TASK-1", "Task");
        Issue bug1 = createIssue("BUG-1", "Bug");
        Issue epic1 = createIssue("EPIC-1", "Epic");

        when(issueDao.searchByJQL(contains("Story"), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(story1, story2));
        when(issueDao.searchByJQL(contains("Task"), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(task1));
        when(issueDao.searchByJQL(contains("Bug"), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(bug1));
        when(issueDao.searchByJQL(contains("Epic"), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(epic1));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                "Open", "john.doe", "High", null, 20, 1, "v1.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(2, response.get(ReleaseSummaryResponse.totalUserStoriesKey));
        assertEquals(2, response.get(ReleaseSummaryResponse.openUserStoriesKey));
        assertEquals(1, response.get(ReleaseSummaryResponse.totalTasksKey));
        assertEquals(1, response.get(ReleaseSummaryResponse.openTasksKey));
        assertEquals(1, response.get(ReleaseSummaryResponse.totalBugsKey));
        assertEquals(1, response.get(ReleaseSummaryResponse.openBugsKey));
        assertEquals(1, response.get(ReleaseSummaryResponse.totalEpicsKey));
        assertNull(response.get(ReleaseSummaryResponse.releaseDayKey));
        assertNull(response.get(ReleaseSummaryResponse.daysRemainingKey));

        verify(issueDao, times(2)).searchByJQL(contains("Story"), eq(20L), eq(1L));
        verify(issueDao, times(2)).searchByJQL(contains("Task"), eq(20L), eq(1L));
        verify(issueDao, times(2)).searchByJQL(contains("Bug"), eq(20L), eq(1L));
        verify(issueDao, times(1)).searchByJQL(contains("Epic"), eq(20L), eq(1L));
    }

    @Test
    @DisplayName("Should get release summary for Story issue type only")
    void testGetReleaseSummary_StoryOnly() {
        // Arrange
        Issue story1 = createIssue("STORY-1", "Story");
        Issue story2 = createIssue("STORY-2", "Story");

        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(story1, story2));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                "Open", "john.doe", "High", "Story", 20, 1, "v1.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(2, response.get(ReleaseSummaryResponse.totalUserStoriesKey));
        assertEquals(2, response.get(ReleaseSummaryResponse.openUserStoriesKey));
        assertNull(response.get(ReleaseSummaryResponse.totalTasksKey));
        assertNull(response.get(ReleaseSummaryResponse.totalBugsKey));
        assertNull(response.get(ReleaseSummaryResponse.totalEpicsKey));

        verify(issueDao, times(2)).searchByJQL(anyString(), eq(20L), eq(1L));
    }

    @Test
    @DisplayName("Should get release summary for Task issue type only")
    void testGetReleaseSummary_TaskOnly() {
        // Arrange
        Issue task1 = createIssue("TASK-1", "Task");

        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(task1));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                null, null, null, "Task", 10, 1, "v2.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.get(ReleaseSummaryResponse.totalTasksKey));
        assertEquals(1, response.get(ReleaseSummaryResponse.openTasksKey));
        assertNull(response.get(ReleaseSummaryResponse.totalUserStoriesKey));
        assertNull(response.get(ReleaseSummaryResponse.totalBugsKey));
        assertNull(response.get(ReleaseSummaryResponse.totalEpicsKey));

        verify(issueDao, times(2)).searchByJQL(anyString(), eq(10L), eq(1L));
    }

    @Test
    @DisplayName("Should get release summary for Bug issue type only")
    void testGetReleaseSummary_BugOnly() {
        // Arrange
        Issue bug1 = createIssue("BUG-1", "Bug");
        Issue bug2 = createIssue("BUG-2", "Bug");
        Issue bug3 = createIssue("BUG-3", "Bug");

        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(bug1, bug2, bug3));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                "Open", null, "Critical", "Bug", 50, 2, "v1.5"
        );

        // Assert
        assertNotNull(response);
        assertEquals(3, response.get(ReleaseSummaryResponse.totalBugsKey));
        assertEquals(3, response.get(ReleaseSummaryResponse.openBugsKey));
        assertNull(response.get(ReleaseSummaryResponse.totalUserStoriesKey));
        assertNull(response.get(ReleaseSummaryResponse.totalTasksKey));
        assertNull(response.get(ReleaseSummaryResponse.totalEpicsKey));

        verify(issueDao, times(2)).searchByJQL(anyString(), eq(50L), eq(2L));
    }

    @Test
    @DisplayName("Should get release summary for Epic issue type only")
    void testGetReleaseSummary_EpicOnly() {
        // Arrange
        Issue epic1 = createIssue("EPIC-1", "Epic");

        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(epic1));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                "In Progress", "team.lead", "High", "Epic", 20, 1, "v3.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.get(ReleaseSummaryResponse.totalEpicsKey));
        assertNull(response.get(ReleaseSummaryResponse.totalUserStoriesKey));
        assertNull(response.get(ReleaseSummaryResponse.totalTasksKey));
        assertNull(response.get(ReleaseSummaryResponse.totalBugsKey));

        verify(issueDao, times(1)).searchByJQL(anyString(), eq(20L), eq(1L));
    }

    @Test
    @DisplayName("Should handle empty results for all issue types")
    void testGetReleaseSummary_EmptyResults() {
        // Arrange
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                "Closed", "assignee", "Low", null, 20, 1, "v1.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(0, response.get(ReleaseSummaryResponse.totalUserStoriesKey));
        assertEquals(0, response.get(ReleaseSummaryResponse.openUserStoriesKey));
        assertEquals(0, response.get(ReleaseSummaryResponse.totalTasksKey));
        assertEquals(0, response.get(ReleaseSummaryResponse.openTasksKey));
        assertEquals(0, response.get(ReleaseSummaryResponse.totalBugsKey));
        assertEquals(0, response.get(ReleaseSummaryResponse.openBugsKey));
        assertEquals(0, response.get(ReleaseSummaryResponse.totalEpicsKey));
    }

    @Test
    @DisplayName("Should handle case-insensitive issue type - story")
    void testGetReleaseSummary_CaseInsensitive_Story() {
        // Arrange
        Issue story = createIssue("STORY-1", "Story");
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(story));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                null, null, null, "story", 20, 1, "v1.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.get(ReleaseSummaryResponse.totalUserStoriesKey));
    }

    @Test
    @DisplayName("Should handle case-insensitive issue type - TASK")
    void testGetReleaseSummary_CaseInsensitive_Task() {
        // Arrange
        Issue task = createIssue("TASK-1", "Task");
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Arrays.asList(task));

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                null, null, null, "TASK", 20, 1, "v1.0"
        );

        // Assert
        assertNotNull(response);
        assertEquals(1, response.get(ReleaseSummaryResponse.totalTasksKey));
    }

    @Test
    @DisplayName("Should handle null parameters")
    void testGetReleaseSummary_NullParameters() {
        // Arrange
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        ReleaseSummaryResponse response = releaseDao.getGetReleaseSummaryResponse(
                null, null, null, null, 20, 1, null
        );

        // Assert
        assertNotNull(response);
        assertNull(response.get(ReleaseSummaryResponse.releaseDayKey));
        assertNull(response.get(ReleaseSummaryResponse.daysRemainingKey));
    }

    @Test
    @DisplayName("Should handle different page sizes and numbers")
    void testGetReleaseSummary_DifferentPagination() {
        // Arrange
        when(issueDao.searchByJQL(anyString(), anyLong(), anyLong()))
                .thenReturn(Collections.emptyList());

        // Act
        ReleaseSummaryResponse response1 = releaseDao.getGetReleaseSummaryResponse(
                null, null, null, "Story", 10, 1, "v1.0"
        );
        ReleaseSummaryResponse response2 = releaseDao.getGetReleaseSummaryResponse(
                null, null, null, "Story", 50, 3, "v1.0"
        );

        // Assert
        assertNotNull(response1);
        assertNotNull(response2);
        verify(issueDao, times(2)).searchByJQL(anyString(), eq(10L), eq(1L));
        verify(issueDao, times(2)).searchByJQL(anyString(), eq(50L), eq(3L));
    }

    /**
     * Helper method to create a test Issue
     */
    private Issue createIssue(String key, String type) {
        Issue issue = new Issue();
        issue.key = key;
        issue.issueType = type;
        issue.summary = "Test " + type;
        return issue;
    }
}

