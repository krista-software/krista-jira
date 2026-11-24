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

package app.krista.extensions.devsecops.development.jira.util;

import app.krista.sdk.atlassian.util.DatePlus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * Comprehensive test suite for DatePlus utility class.
 * Tests all public static methods with edge cases, null values, boundary dates, and timezone handling.
 * 
 * Following extension revamp guidelines:
 * - No Mockito usage - using real concrete implementations only
 * - 100% coverage of utility methods
 * - Edge cases: null, boundary values, timezone considerations
 */
@DisplayName("DatePlus Utility Tests")
class DatePlusTest {

    @Test
    @DisplayName("getISODate: Should format current date to ISO format with UTC timezone")
    void testGetISODate_CurrentDate_ReturnsISOFormat() {
        // Arrange
        Date currentDate = new Date();
        
        // Act
        String result = DatePlus.getISODate(currentDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertFalse(result.isEmpty(), "Result should not be empty");
        
        // Verify ISO format pattern: yyyy-MM-dd'T'HH:mm:ss.SSSZ
        assertTrue(result.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{4}"), 
                  "Should match ISO date format pattern");
        
        // Verify it ends with +0000 (UTC timezone)
        assertTrue(result.endsWith("+0000"), "Should end with +0000 for UTC timezone");
    }

    @Test
    @DisplayName("getISODate: Should handle epoch date (January 1, 1970)")
    void testGetISODate_EpochDate_ReturnsCorrectISOFormat() {
        // Arrange
        Date epochDate = new Date(0L); // January 1, 1970, 00:00:00 GMT
        
        // Act
        String result = DatePlus.getISODate(epochDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("1970-01-01T00:00:00.000+0000", result, 
                    "Should return correct ISO format for epoch date");
    }

    @Test
    @DisplayName("getISODate: Should handle specific known date")
    void testGetISODate_SpecificDate_ReturnsCorrectISOFormat() {
        // Arrange - Create a specific date: 2023-12-25 15:30:45.123 UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.DECEMBER, 25, 15, 30, 45);
        cal.set(Calendar.MILLISECOND, 123);
        Date specificDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(specificDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("2023-12-25T15:30:45.123+0000", result, 
                    "Should return correct ISO format for specific date");
    }

    @Test
    @DisplayName("getISODate: Should handle leap year date")
    void testGetISODate_LeapYearDate_ReturnsCorrectISOFormat() {
        // Arrange - February 29, 2024 (leap year)
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2024, Calendar.FEBRUARY, 29, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date leapYearDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(leapYearDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("2024-02-29T12:00:00.000+0000", result, 
                    "Should handle leap year date correctly");
    }

    @Test
    @DisplayName("getISODate: Should handle year 2000 (Y2K boundary)")
    void testGetISODate_Y2KDate_ReturnsCorrectISOFormat() {
        // Arrange - January 1, 2000, 00:00:00.000 UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2000, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date y2kDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(y2kDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("2000-01-01T00:00:00.000+0000", result, 
                    "Should handle Y2K date correctly");
    }

    @Test
    @DisplayName("getISODate: Should handle maximum milliseconds (999)")
    void testGetISODate_MaxMilliseconds_ReturnsCorrectISOFormat() {
        // Arrange - Date with 999 milliseconds
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JUNE, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 999);
        Date maxMillisDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(maxMillisDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains(".999+0000"), "Should correctly format 999 milliseconds");
        assertEquals("2023-06-15T10:30:45.999+0000", result, 
                    "Should handle maximum milliseconds correctly");
    }

    @Test
    @DisplayName("getISODate: Should handle minimum milliseconds (000)")
    void testGetISODate_MinMilliseconds_ReturnsCorrectISOFormat() {
        // Arrange - Date with 0 milliseconds
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JUNE, 15, 10, 30, 45);
        cal.set(Calendar.MILLISECOND, 0);
        Date minMillisDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(minMillisDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertTrue(result.contains(".000+0000"), "Should correctly format 000 milliseconds");
        assertEquals("2023-06-15T10:30:45.000+0000", result, 
                    "Should handle minimum milliseconds correctly");
    }

    @Test
    @DisplayName("getISODate: Should handle end of year date")
    void testGetISODate_EndOfYear_ReturnsCorrectISOFormat() {
        // Arrange - December 31, 2023, 23:59:59.999 UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.DECEMBER, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        Date endOfYearDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(endOfYearDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("2023-12-31T23:59:59.999+0000", result, 
                    "Should handle end of year date correctly");
    }

    @Test
    @DisplayName("getISODate: Should handle beginning of year date")
    void testGetISODate_BeginningOfYear_ReturnsCorrectISOFormat() {
        // Arrange - January 1, 2023, 00:00:00.000 UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2023, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date beginningOfYearDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(beginningOfYearDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("2023-01-01T00:00:00.000+0000", result, 
                    "Should handle beginning of year date correctly");
    }

    @Test
    @DisplayName("getISODate: Should produce parseable ISO date string")
    void testGetISODate_ProducedString_IsParseable() throws ParseException {
        // Arrange
        Date originalDate = new Date();
        
        // Act
        String isoString = DatePlus.getISODate(originalDate);
        
        // Parse it back using the same format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date parsedDate = sdf.parse(isoString);
        
        // Assert
        assertNotNull(parsedDate, "Parsed date should not be null");
        assertEquals(originalDate.getTime(), parsedDate.getTime(), 
                    "Parsed date should equal original date");
    }

    @Test
    @DisplayName("getISODate: Should handle far future date")
    void testGetISODate_FarFutureDate_ReturnsCorrectISOFormat() {
        // Arrange - January 1, 2100, 12:00:00.000 UTC
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        cal.set(2100, Calendar.JANUARY, 1, 12, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date futureDate = cal.getTime();
        
        // Act
        String result = DatePlus.getISODate(futureDate);
        
        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("2100-01-01T12:00:00.000+0000", result, 
                    "Should handle far future date correctly");
    }

    @Test
    @DisplayName("getISODate: Should be consistent across multiple calls")
    void testGetISODate_MultipleCalls_ReturnsConsistentResults() {
        // Arrange
        Date testDate = new Date(1609459200000L); // January 1, 2021, 00:00:00 UTC

        // Act
        String result1 = DatePlus.getISODate(testDate);
        String result2 = DatePlus.getISODate(testDate);
        String result3 = DatePlus.getISODate(testDate);

        // Assert
        assertEquals(result1, result2, "Multiple calls should return identical results");
        assertEquals(result2, result3, "Multiple calls should return identical results");
        assertEquals("2021-01-01T00:00:00.000+0000", result1,
                    "Should return consistent ISO format");
    }

    // ========== toJqlDate() — epoch millis ==========

    @Test
    @DisplayName("toJqlDate: epoch millis converted to JQL date format (yyyy/MM/dd HH:mm UTC)")
    void testToJqlDate_EpochMillis_ConvertsToJqlFormat() {
        String result = DatePlus.toJqlDate("1710511845000");
        assertNotNull(result);
        assertTrue(result.startsWith("2024/03/15"), "Should convert epoch to yyyy/MM/dd: " + result);
        assertTrue(result.matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}"), "Should match JQL date format: " + result);
    }

    @Test
    @DisplayName("toJqlDate: epoch zero converts to 1970/01/01")
    void testToJqlDate_EpochZero() {
        String result = DatePlus.toJqlDate("0");
        assertNotNull(result);
        assertEquals("1970/01/01 00:00", result);
    }

    // ========== toJqlDate() — ISO-8601 strings ==========

    @Test
    @DisplayName("toJqlDate: ISO-8601 datetime converted to JQL date format")
    void testToJqlDate_IsoDatetime() {
        String result = DatePlus.toJqlDate("2025-06-15T10:30:00Z");
        assertNotNull(result);
        assertEquals("2025/06/15 10:30", result);
    }

    @Test
    @DisplayName("toJqlDate: ISO-8601 with timezone offset converted to UTC")
    void testToJqlDate_IsoWithOffset() {
        // +05:30 offset: 10:30 local = 05:00 UTC
        String result = DatePlus.toJqlDate("2025-06-15T10:30:00+05:30");
        assertNotNull(result);
        assertEquals("2025/06/15 05:00", result);
    }

    // ========== toJqlDate() — edge cases ==========

    @Test
    @DisplayName("toJqlDate: null input returns null")
    void testToJqlDate_Null_ReturnsNull() {
        assertNull(DatePlus.toJqlDate(null));
    }

    @Test
    @DisplayName("toJqlDate: blank input returns blank")
    void testToJqlDate_Blank_ReturnsBlank() {
        assertEquals("  ", DatePlus.toJqlDate("  "));
    }

    @Test
    @DisplayName("toJqlDate: non-date string returns as-is")
    void testToJqlDate_NonDateString_ReturnsAsIs() {
        assertEquals("not-a-date", DatePlus.toJqlDate("not-a-date"));
    }

    @Test
    @DisplayName("toJqlDate: already JQL-format date returns as-is")
    void testToJqlDate_AlreadyJqlFormat_ReturnsAsIs() {
        assertEquals("2025/06/15 10:30", DatePlus.toJqlDate("2025/06/15 10:30"));
    }

    @Test
    @DisplayName("toJqlDate: Instant.parse fallback — ISO instant without offset")
    void testToJqlDate_InstantParseFallback() {
        // "2025-06-15T10:30:00Z" is handled by ZonedDateTime, but truncated instant format
        // triggers Instant.parse path
        String result = DatePlus.toJqlDate("2025-06-15T10:30:00.000Z");
        assertNotNull(result);
        assertEquals("2025/06/15 10:30", result);
    }
}
