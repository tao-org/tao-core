/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */

package ro.cs.tao.utils;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;

/**
 * Utility class for various operations (such as formatting) on date types.
 *
 * @author Cosmin Cara
 * @since 1.0
 */
public class DateUtils {

    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "--h--m--s";
        }
        long seconds = duration.getSeconds();
        return seconds < 3600 ? String.format("%02dm%02ds", seconds / 60, seconds % 60) :
                String.format("%02dh%02dm%02ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    public static DateTimeFormatter getFormatterAtUTC(String format) {
        //return DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss").appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true);
        if (format.endsWith("Z")) {
            builder.appendLiteral("Z");
        }
        return builder.toFormatter().withZone(ZoneId.of("UTC"));
    }

    public static DateTimeFormatter getFormatterAtLocal(String format) {
        //return DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss").appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true);
        if (format.endsWith("Z") || format.endsWith("Z'")) {
            builder.appendLiteral("Z");
        }
        return builder.toFormatter().withZone(ZoneId.systemDefault());
    }

    public static DateTimeFormatter getResilientFormatterAtUTC() {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-").appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
                .appendLiteral("-").appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
                .optionalStart()
                    .appendLiteral("T")
                .optionalEnd()
                .optionalStart()
                    .appendLiteral(" ")
                .optionalEnd()
                .optionalStart()
                    .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral(":").appendValue(ChronoField.MINUTE_OF_HOUR, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral(":").appendValue(ChronoField.SECOND_OF_MINUTE, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 6, true)
                    .optionalStart()
                        .appendZoneOrOffsetId()
                    .optionalEnd()
                    .optionalStart()
                        .appendLiteral("Z")
                    .optionalEnd()
                .optionalEnd();
        return builder.toFormatter().withZone(ZoneId.of("UTC"));
    }
}
