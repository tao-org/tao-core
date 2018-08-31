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

/**
 * Converts a range parameter (i.e. a query parameter for which minumum and maximum values are set) into a string
 * representation (of the form '[minValue,maxValue]').
 *
 * @author Cosmin Cara
 */
public class RangeParameterConverter extends DefaultParameterConverter {

    public RangeParameterConverter(QueryParameter parameter) {
        super(parameter);
        if (!Number.class.isAssignableFrom(parameter.getType()) &&
                !parameter.isInterval()) {
            throw new IllegalArgumentException("Invalid parameter type");
        }
    }

    @Override
    public String stringValue() throws ConversionException {
        StringBuilder builder = new StringBuilder();
        Number minValue = (Number) this.parameter.getMinValue();
        if (minValue == null) {
            minValue = 0;
        }
        builder.append("[").append(minValue).append(",");
        Number maxValue = (Number) this.parameter.getMaxValue();
        if (maxValue == null) {
            Number value = (Number) this.parameter.getValue();
            maxValue = value != null ? value : 100;
        }
        builder.append(maxValue).append("]");
        return builder.toString();
    }
}
