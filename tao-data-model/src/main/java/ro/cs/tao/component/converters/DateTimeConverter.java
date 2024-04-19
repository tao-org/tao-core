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
package ro.cs.tao.component.converters;

import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Converter between {@link LocalDateTime} and {@link String} objects.
 *
 * @author Cosmin Cara
 */
public class DateTimeConverter extends DefaultConverter<LocalDateTime> {
    private static final DateTimeFormatter format = DateUtils.getFormatterAtUTC("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter alternateFormat = DateUtils.getFormatterAtUTC("MM/dd/yyyy HH:mm:ss");

    public DateTimeConverter() { }

    @Override
    public LocalDateTime fromString(String value) throws ConversionException {
        try {
            return value != null ? DateUtils.parseDateTime(value) : null;
        } catch (DateTimeParseException e) {
            throw new ConversionException(e.getMessage());
        }
    }

    @Override
    public String stringValue(LocalDateTime value) throws ConversionException {
        if (value == null) return null;
        try {
            return format.format(value);
        } catch (Exception ex) {
            try {
                return alternateFormat.format(value);
            } catch (Exception ex2) {
                throw new ConversionException(ex.getMessage());
            }
        }
    }
}
