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

package app.krista.extensions.devsecops.development.jira.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test suite for ReleaseSummaryResponse class.
 * Tests release summary data structure and getter/setter methods.
 */
@DisplayName("ReleaseSummaryResponse Tests")
class ReleaseSummaryResponseTest {

    private ReleaseSummaryResponse response;

    @BeforeEach
    void setUp() {
        response = new ReleaseSummaryResponse();
    }

    @Test
    @DisplayName("Should set and get total user stories")
    void testSetAndGetTotalUserStories() {
        // Arrange
        Double totalUserStories = 25.0;

        // Act
        response.setTotalUserStories(totalUserStories);
        Double result = response.getTotalUserStories();

        // Assert
        assertEquals(totalUserStories, result);
    }

    @Test
    @DisplayName("Should set and get open user stories")
    void testSetAndGetOpenUserStories() {
        // Arrange
        Double openUserStories = 10.0;

        // Act
        response.setOpenUserStories(openUserStories);
        Double result = response.getOpenUserStories();

        // Assert
        assertEquals(openUserStories, result);
    }

    @Test
    @DisplayName("Should set and get total epics")
    void testSetAndGetTotalEpics() {
        // Arrange
        Double totalEpics = 5.0;

        // Act
        response.setTotalEpics(totalEpics);
        Double result = response.getTotalEpics();

        // Assert
        assertEquals(totalEpics, result);
    }

    @Test
    @DisplayName("Should set and get total tasks")
    void testSetAndGetTotalTasks() {
        // Arrange
        Double totalTasks = 50.0;

        // Act
        response.setTotalTasks(totalTasks);
        Double result = response.getTotalTasks();

        // Assert
        assertEquals(totalTasks, result);
    }

    @Test
    @DisplayName("Should set and get open tasks")
    void testSetAndGetOpenTasks() {
        // Arrange
        Double openTasks = 20.0;

        // Act
        response.setOpenTasks(openTasks);
        Double result = response.getOpenTasks();

        // Assert
        assertEquals(openTasks, result);
    }

    @Test
    @DisplayName("Should set and get total bugs")
    void testSetAndGetTotalBugs() {
        // Arrange
        Double totalBugs = 15.0;

        // Act
        response.setTotalBugs(totalBugs);
        Double result = response.getTotalBugs();

        // Assert
        assertEquals(totalBugs, result);
    }

    @Test
    @DisplayName("Should set and get open bugs")
    void testSetAndGetOpenBugs() {
        // Arrange
        Double openBugs = 8.0;

        // Act
        response.setOpenBugs(openBugs);
        Double result = response.getOpenBugs();

        // Assert
        assertEquals(openBugs, result);
    }

    @Test
    @DisplayName("Should set and get release day")
    void testSetAndGetReleaseDay() {
        // Arrange
        Long releaseDay = 1704067200000L; // Unix timestamp

        // Act
        response.setReleaseDay(releaseDay);
        Long result = response.getReleaseDay();

        // Assert
        assertEquals(releaseDay, result);
    }

    @Test
    @DisplayName("Should set and get days remaining")
    void testSetAndGetDaysRemaining() {
        // Arrange
        Double daysRemaining = 30.0;

        // Act
        response.setDaysRemaining(daysRemaining);
        Double result = response.getDaysRemaining();

        // Assert
        assertEquals(daysRemaining, result);
    }

    @Test
    @DisplayName("Should set and get user stories list")
    void testSetAndGetUserStories() {
        // Arrange
        List<String> userStories = Arrays.asList("STORY-1", "STORY-2", "STORY-3");

        // Act
        response.setUserStories(userStories);
        List<String> result = response.getUserStories();

        // Assert
        assertEquals(userStories, result);
        assertEquals(3, result.size());
    }

    @Test
    @DisplayName("Should set and get epics list")
    void testSetAndGetEpics() {
        // Arrange
        List<String> epics = Arrays.asList("EPIC-1", "EPIC-2");

        // Act
        response.setEpics(epics);
        List<String> result = response.getEpics();

        // Assert
        assertEquals(epics, result);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should set and get bugs list")
    void testSetAndGetBugs() {
        // Arrange
        List<String> bugs = Arrays.asList("BUG-1", "BUG-2", "BUG-3", "BUG-4");

        // Act
        response.setBugs(bugs);
        List<String> result = response.getBugs();

        // Assert
        assertEquals(bugs, result);
        assertEquals(4, result.size());
    }

    @Test
    @DisplayName("Should set and get tasks list")
    void testSetAndGetTasks() {
        // Arrange
        List<String> tasks = Arrays.asList("TASK-1", "TASK-2", "TASK-3", "TASK-4", "TASK-5");

        // Act
        response.setTasks(tasks);
        List<String> result = response.getTasks();

        // Assert
        assertEquals(tasks, result);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should handle zero values")
    void testZeroValues() {
        // Arrange & Act
        response.setTotalUserStories(0.0);
        response.setOpenUserStories(0.0);
        response.setTotalTasks(0.0);
        response.setOpenTasks(0.0);

        // Assert
        assertEquals(0.0, response.getTotalUserStories());
        assertEquals(0.0, response.getOpenUserStories());
        assertEquals(0.0, response.getTotalTasks());
        assertEquals(0.0, response.getOpenTasks());
    }

    @Test
    @DisplayName("Should handle null values")
    void testNullValues() {
        // Arrange & Act
        response.setTotalUserStories(null);
        response.setUserStories(null);

        // Assert
        assertNull(response.getTotalUserStories());
        assertNull(response.getUserStories());
    }

    @Test
    @DisplayName("Should handle empty lists")
    void testEmptyLists() {
        // Arrange
        List<String> emptyList = Arrays.asList();

        // Act
        response.setUserStories(emptyList);
        response.setEpics(emptyList);
        response.setBugs(emptyList);
        response.setTasks(emptyList);

        // Assert
        assertEquals(0, response.getUserStories().size());
        assertEquals(0, response.getEpics().size());
        assertEquals(0, response.getBugs().size());
        assertEquals(0, response.getTasks().size());
    }

    @Test
    @DisplayName("Should handle large numbers")
    void testLargeNumbers() {
        // Arrange
        Double largeNumber = 999999.0;

        // Act
        response.setTotalUserStories(largeNumber);
        response.setTotalTasks(largeNumber);

        // Assert
        assertEquals(largeNumber, response.getTotalUserStories());
        assertEquals(largeNumber, response.getTotalTasks());
    }

    @Test
    @DisplayName("Should handle decimal values")
    void testDecimalValues() {
        // Arrange
        Double decimalValue = 12.5;

        // Act
        response.setDaysRemaining(decimalValue);

        // Assert
        assertEquals(decimalValue, response.getDaysRemaining());
    }

    @Test
    @DisplayName("Should store all values independently")
    void testAllValuesIndependently() {
        // Arrange & Act
        response.setTotalUserStories(25.0);
        response.setOpenUserStories(10.0);
        response.setTotalEpics(5.0);
        response.setTotalTasks(50.0);
        response.setOpenTasks(20.0);
        response.setTotalBugs(15.0);
        response.setOpenBugs(8.0);
        response.setReleaseDay(1704067200000L);
        response.setDaysRemaining(30.0);
        response.setUserStories(Arrays.asList("STORY-1"));
        response.setEpics(Arrays.asList("EPIC-1"));
        response.setBugs(Arrays.asList("BUG-1"));
        response.setTasks(Arrays.asList("TASK-1"));

        // Assert
        assertEquals(25.0, response.getTotalUserStories());
        assertEquals(10.0, response.getOpenUserStories());
        assertEquals(5.0, response.getTotalEpics());
        assertEquals(50.0, response.getTotalTasks());
        assertEquals(20.0, response.getOpenTasks());
        assertEquals(15.0, response.getTotalBugs());
        assertEquals(8.0, response.getOpenBugs());
        assertEquals(1704067200000L, response.getReleaseDay());
        assertEquals(30.0, response.getDaysRemaining());
        assertEquals(1, response.getUserStories().size());
        assertEquals(1, response.getEpics().size());
        assertEquals(1, response.getBugs().size());
        assertEquals(1, response.getTasks().size());
    }

    @Test
    @DisplayName("Should extend HashMap and allow direct put/get")
    void testHashMapFunctionality() {
        // Arrange & Act
        response.put("Custom Key", "Custom Value");
        Object result = response.get("Custom Key");

        // Assert
        assertEquals("Custom Value", result);
    }

    @Test
    @DisplayName("Should verify constant key values")
    void testConstantKeyValues() {
        // Assert
        assertEquals("Total user stories", ReleaseSummaryResponse.totalUserStoriesKey);
        assertEquals("Open user stories", ReleaseSummaryResponse.openUserStoriesKey);
        assertEquals("Total epics", ReleaseSummaryResponse.totalEpicsKey);
        assertEquals("Total Tasks", ReleaseSummaryResponse.totalTasksKey);
        assertEquals("Open Tasks", ReleaseSummaryResponse.openTasksKey);
        assertEquals("Total Bugs", ReleaseSummaryResponse.totalBugsKey);
        assertEquals("Open Bugs", ReleaseSummaryResponse.openBugsKey);
        assertEquals("Release day", ReleaseSummaryResponse.releaseDayKey);
        assertEquals("Days remaining", ReleaseSummaryResponse.daysRemainingKey);
        assertEquals("User stories", ReleaseSummaryResponse.userStoriesKey);
        assertEquals("Epics", ReleaseSummaryResponse.epicsKey);
        assertEquals("Bugs", ReleaseSummaryResponse.bugsKey);
        assertEquals("Tasks", ReleaseSummaryResponse.tasksKey);
    }
}

