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

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.TimeZone;

public final class DatePlus {

    private static final DateTimeFormatter JQL_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneOffset.UTC);

    public static String getISODate(Date date) {
        SimpleDateFormat sdf;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static String toJqlDate(String isoDateOrEpoch) {
        if (isoDateOrEpoch == null || isoDateOrEpoch.isBlank()) {
            return isoDateOrEpoch;
        }
        try {
            long epoch = Long.parseLong(isoDateOrEpoch);
            return JQL_DATE_FORMAT.format(Instant.ofEpochMilli(epoch));
        } catch (NumberFormatException ignored) {
            // Not epoch millis, try ISO-8601
        }
        try {
            ZonedDateTime zdt = ZonedDateTime.parse(isoDateOrEpoch, DateTimeFormatter.ISO_DATE_TIME);
            return JQL_DATE_FORMAT.format(zdt.toInstant());
        } catch (DateTimeParseException ignored) {
            // Not standard ISO
        }
        try {
            Instant instant = Instant.parse(isoDateOrEpoch);
            return JQL_DATE_FORMAT.format(instant);
        } catch (DateTimeParseException ignored) {
            // Return as-is
        }
        return isoDateOrEpoch;
    }
}