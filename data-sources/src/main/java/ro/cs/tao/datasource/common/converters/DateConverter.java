/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.common.converters;

import ro.cs.tao.datasource.common.QueryParameter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Cosmin Cara
 */
public class DateConverter extends DefaultConverter {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private final DateTimeFormatter dateFormat;
    public DateConverter(QueryParameter parameter) {
        super(parameter);
        if (!Date.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
        dateFormat = DateTimeFormatter.ofPattern(DATE_FORMAT);
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        if (parameter.isInterval()) {
            Object minValue = parameter.getMinValue();
            if (minValue != null) {
                LocalDateTime minDate = ((Date) minValue).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime();
                builder.append(minDate.format(dateFormat));
            } else {
                throw new ConversionException("Parameter represents an interval, but the minimum value is absent");
            }
            builder.append(" TO ");
            Object maxValue = parameter.getMaxValue();
            if (maxValue != null) {
                LocalDateTime maxDate = ((Date) parameter.getMaxValue()).toInstant()
                        .atZone((ZoneId.systemDefault())).toLocalDateTime();
                builder.append(maxDate.format(dateFormat));
            } else {
                throw new ConversionException("Parameter represents an interval, but the maximum value is absent");
            }
        } else {
            LocalDateTime date = ((Date) parameter.getValue()).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
            builder.append(date.format(dateFormat));
        }
        return builder.toString();
    }
}
