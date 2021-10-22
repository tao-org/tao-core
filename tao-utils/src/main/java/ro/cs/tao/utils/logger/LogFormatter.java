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
package ro.cs.tao.utils.logger;

import ro.cs.tao.utils.DateUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * Simple formatter class for log messages.
 *
 * @author COsmin Cara
 */
public class LogFormatter extends Formatter {
    private final DateTimeFormatter dateFormat = DateUtils.getFormatterAtLocal("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String format(LogRecord record) {
        String level = record.getLevel().getName();
        return formatTime(record.getMillis()) +
                "\t" +
                "[" + level + "]" +
                "\t" + (level.length() < 6 ? "\t" : "") +
                record.getMessage() +
                "\n";
    }

    private String formatTime(long millis) {
        return dateFormat.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
    }
}