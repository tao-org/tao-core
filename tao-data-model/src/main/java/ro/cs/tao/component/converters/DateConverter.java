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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Converter between {@link Date} and {@link String} objects.
 *
 * @author Cosmin Cara
 */
public class DateConverter extends DefaultConverter<Date> {
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    public DateConverter() { }

    @Override
    public Date fromString(String value) throws ConversionException {
        try {
            return value != null ? format.parse(value) : null;
        } catch (ParseException e) {
            throw new ConversionException(e.getMessage());
        }
    }

    @Override
    public String stringValue(Date value) throws ConversionException {
        try {
            return value != null ? format.format(value) : null;
        } catch (Exception ex) {
            throw new ConversionException(ex.getMessage());
        }
    }
}
