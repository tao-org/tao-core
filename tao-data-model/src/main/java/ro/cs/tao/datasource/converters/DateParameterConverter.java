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
package ro.cs.tao.datasource.converters;

import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Default query parameter converter for Date values.
 *
 * @author Cosmin Cara
 */
public class DateParameterConverter extends DefaultParameterConverter<LocalDateTime> {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    protected DateTimeFormatter dateFormat;
    public DateParameterConverter(QueryParameter<LocalDateTime> parameter) {
        super(parameter);
        if (!LocalDateTime.class.equals(parameter.getType())) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
        dateFormat = DateUtils.getFormatterAtUTC(DATE_FORMAT);
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        LocalDateTime minValue, maxValue;
        if (parameter.isInterval()) {
            minValue = parameter.getMinValue();
            maxValue = parameter.getMaxValue();
        } else {
            minValue = parameter.getValue();
            maxValue = LocalDateTime.now();
        }
        if (minValue != null) {
            builder.append(minValue.format(dateFormat));
        } else {
            throw new ConversionException("Parameter represents an interval, but the minimum value is absent");
        }
        builder.append(" TO ");
        if (maxValue != null) {
            builder.append(maxValue.format(dateFormat));
        } else {
            throw new ConversionException("Parameter represents an interval, but the maximum value is absent");
        }
        return builder.toString();
    }
}
