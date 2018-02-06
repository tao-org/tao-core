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
package ro.cs.tao.datasource.param;

import ro.cs.tao.datasource.converters.QueryParameterConverter;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.PolygonAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Abstraction for a query parameter.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "parameter")
@XmlJavaTypeAdapter(value = PolygonAdapter.class, type = Polygon2D.class)
public class QueryParameter<T> {
    private Class<T> type;
    private boolean isOptional;
    private String name;
    private T minValue;
    private T maxValue;
    private T value;
    private QueryParameterConverter converter;

    private QueryParameter() { }

    public QueryParameter(Class<T> type, String name) {
        this(type, name, null, true);
    }

    public QueryParameter(Class<T> type, String name, T value) {
        this(type, name, value, true);
        //checkValid();
    }

    public QueryParameter(Class<T> type, String name, T value, boolean isOptional) {
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

    public QueryParameter(Class<T> type, String name, T minValue, T maxValue) {
        this(type, name, null);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public QueryParameter(Class<T> type, String name, T minValue, T maxValue, boolean isOptional) {
        this(type, name, null, isOptional);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    @XmlElement(name = "class", required = true)
    public Class<T> getType() {
        return type;
    }

    public void setType(Class<T> type) { this.type = type; }

    @XmlElement(name = "name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) { this.name = name; }

    @XmlElement(name = "value")
    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @XmlElement(name = "optional", required = true)
    public boolean isOptional() {
        return isOptional;
    }

    public void setOptional(boolean optional) {
        isOptional = optional;
    }

    @XmlElement(name = "minimum")
    public T getMinValue() {
        return minValue;
    }

    public void setMinValue(T minValue) { this.minValue = minValue; }

    @XmlElement(name = "maximum")
    public T getMaxValue() { return maxValue; }

    public void setMaxValue(T maxValue) { this.maxValue = maxValue; }

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

    public String getMinValueAsFormattedDate(String format) {
        try {
            return new SimpleDateFormat(format).format((Date) this.minValue);
        } catch (Exception e) {
            return null;
        }
    }

    public String getMaxValueAsFormattedDate(String format) {
        try {
            return new SimpleDateFormat(format).format((Date) this.maxValue);
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
