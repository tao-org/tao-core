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

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for various operations (such as formatting) on date types.
 *
 * @author Cosmin Cara
 * @since 1.0
 */
public class DateUtils {

    /**
     * Formats a duration in h m s format
     * @param duration  The time duration
     */
    public static String formatDuration(Duration duration) {
        if (duration == null) {
            return "--h--m--s";
        }
        long seconds = duration.getSeconds();
        return seconds < 3600
               ? String.format("%02dm%02ds", seconds / 60, seconds % 60)
               : String.format("%02dh%02dm%02ds", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }
    /**
     * Gets a DateTimeFormatter at UTC time zone for parsing datetime strings
     * @param format    The expected datetime pattern
     */
    public static DateTimeFormatter getFormatterAtUTC(String format) {
        return DateTimeFormatter.ofPattern(format).withZone(ZoneId.of("UTC"));
    }
    /**
     * Gets a DateTimeFormatter at system's local time zone for parsing datetime strings
     * @param format    The expected datetime pattern
     */
    public static DateTimeFormatter getFormatterAtLocal(String format) {
        return DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
    }
    /**
     * Returns a DateTimeFormatter that is permissive to optional format tokens.
     * The supported pattern is:
     *      <@{@code yyyy-M{1,2}-D{1,2}(T| )(H{1,2}(:)m{1,2}(:)s{1,2}(.S{0,6}(+NN)(Z)}
     */
    public static DateTimeFormatter getResilientFormatterAtUTC() {
        final DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR, 2, 4, SignStyle.NOT_NEGATIVE)
                .optionalStart()
                    .appendLiteral("-")
                .optionalEnd()
                    .appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE)
                .optionalStart()
                    .appendLiteral("-")
                .optionalEnd()
                    .appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE)
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
    /**
     * Tries to parse the date string using the resilient DateTimeFormatter at UTC time zone
     * @param date  The date string
     */
    public static LocalDate parseDate(String date) {
        return LocalDate.parse(date, getResilientFormatterAtUTC());
    }
    /**
     * Tries to parse the datetime string using the resilient DateTimeFormatter at UTC time zone
     * @param date  The datetime string
     */
    public static LocalDateTime parseDateTime(String date) {
        return LocalDateTime.parse(date, getResilientFormatterAtUTC());
    }

    /**
     * Splits a date period into intervals, optionally considering skipping certain months
     * @param startDate         The start date of the period
     * @param endDate           The end date of the period
     * @param exceptedMonths    (Optional) The months to be excluded from interval
     * @return          A list of intervals
     */
    public static List<LocalDate[]> splitIntoIntervals(LocalDate startDate, LocalDate endDate, int...exceptedMonths) {
        if (startDate == null || endDate == null) {
            return null;
        }
        final List<LocalDate[]> intervals = new ArrayList<>();
        if (startDate.isBefore(endDate)) {
            if (exceptedMonths == null || exceptedMonths.length == 0) {
                intervals.add(new LocalDate[] { startDate, endDate });
            } else {
                final int months = Period.between(startDate, endDate).getMonths();
                final Set<Integer> distinctMonths = new HashSet<>();
                for (int month : exceptedMonths) {
                    distinctMonths.add(month);
                }
                LocalDate newStartDate = startDate;
                LocalDate currentDate;
                for (int i = 0; i < months; i++) {
                    currentDate = startDate.plusMonths(i);
                    // we are at the last month, we need to close the interval
                    if (i == months - 1) {
                        intervals.add(new LocalDate[] { newStartDate, currentDate });
                        break;
                    }
                    // current date should be at the end of the month
                    currentDate = LocalDate.of(currentDate.getYear(), currentDate.getMonth(), currentDate.lengthOfMonth());
                    if (!distinctMonths.contains(currentDate.getMonth().getValue())) {
                        // advance in iteration
                        continue;
                    }
                    // if we have an except month, and it is not consecutive to the previous except month, we create a new interval
                    if (Period.between(newStartDate, currentDate).getMonths() > 0) {
                        intervals.add(new LocalDate[] { newStartDate, currentDate } );
                        // The start date for the next interval becomes the beginning of the next month
                        newStartDate = currentDate.plusDays(1);
                    }
                }
            }
        }
        return intervals;
    }
}
