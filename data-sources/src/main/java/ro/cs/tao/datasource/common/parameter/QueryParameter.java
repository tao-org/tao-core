/*
 * Copyright (C) 2017 CS ROMANIA
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
 *
 */
package ro.cs.tao.datasource.common.parameter;

import ro.cs.tao.datasource.common.converters.ParameterConverter;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstraction for a query parameter.
 *
 * @author Cosmin Cara
 */
public class QueryParameter {
    private Class<?> type;
    private boolean isOptional;
    private String name;
    private Object minValue;
    private Object maxValue;
    private Object value;
    private ParameterConverter converter;

    public QueryParameter(Class type, String name) {
        this(type, name, null, true);
    }

    public QueryParameter(Class type, String name, Object value) {
        this(type, name, value, true);
        //checkValid();
    }

    public QueryParameter(Class type, String name, Object value, boolean isOptional) {
        if (type == null) {
            throw new IllegalArgumentException("[type] cannot be empty");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("[name] cannot be empty");
        }
        this.type = type;
        this.isOptional = isOptional;
        this.name = name;
        this.value = value;
    }

    public QueryParameter(Class type, String name, Object minValue, Object maxValue) {
        this(type, name, null);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public QueryParameter(Class type, String name, Object minValue, Object maxValue, boolean isOptional) {
        this(type, name, null, isOptional);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public Class getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    public Object getMinValue() {
        return minValue;
    }

    public void setMinValue(Object minValue) { this.minValue = minValue; }

    public Object getMaxValue() { return maxValue; }

    public void setMaxValue(Object maxValue) { this.maxValue = maxValue; }

    public boolean isInterval() {
        return this.minValue != null || this.maxValue != null;
    }

    public String getValueAsString() {
        return this.value != null ?
                String.valueOf(this.value) :
                null;
    }

    public Integer getValueAsInt() {
        return this.value != null ?
                Integer.parseInt(String.valueOf(this.value)) :
                null;
    }

    public Double getValueAsDouble() {
        return this.value != null ?
                Double.parseDouble(String.valueOf(this.value)) :
                null;
    }

    public String getValueAsFormattedDate(String format) {
        try {
            return new SimpleDateFormat(format).format((Date) this.value);
        } catch (Exception e) {
            return null;
        }
    }

    private void checkValid() {
        if (!isOptional && !isInterval() && value == null) {
            throw new IllegalArgumentException("Value missing for mandatory parameter");
        }
        if (value != null && !type.isAssignableFrom(value.getClass())) {
            throw new IllegalArgumentException("Parameter value type mismatch");
        }
    }
}
