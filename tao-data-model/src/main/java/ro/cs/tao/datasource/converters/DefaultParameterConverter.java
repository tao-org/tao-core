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
 * Default (fallback) query parameter converter.
 *
 * @author Cosmin Cara
 */
public class DefaultParameterConverter implements QueryParameterConverter {
    protected QueryParameter parameter;

    public DefaultParameterConverter(QueryParameter parameter) {
        this.parameter = parameter;
    }

    @Override
    public String stringValue() throws ConversionException {
        if (this.parameter.isInterval()) {
            return String.valueOf(this.parameter.getMinValue()) + " TO " +
                    String.valueOf(parameter.getMaxValue());

        } else {
            return String.valueOf(this.parameter.getValue());
        }
    }
}
