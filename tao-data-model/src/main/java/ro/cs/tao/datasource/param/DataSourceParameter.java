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
package ro.cs.tao.datasource.param;

import java.util.Objects;

/**
 * Descriptor for a data source query parameter.
 *
 * @author Cosmin Cara
 */
public class DataSourceParameter {
    private final String name;
    private final Class type;
    private final Object defaultValue;
    private final boolean required;
    private Object[] valueSet;

    public DataSourceParameter(String name, Class type) {
        this(name, type, null, false);
    }

    public DataSourceParameter(String name, Class type, boolean required) {
        this(name, type, null, required);
    }

    public DataSourceParameter(String name, Class type, Object defaultValue) {
        this(name, type, defaultValue, false);
    }

    public DataSourceParameter(String name, Class type, Object defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public DataSourceParameter(String name, Class type, Object defaultValue, boolean required, Object... valueSet) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
        this.valueSet = valueSet;
    }

    public String getName() { return name; }

    public Class getType() { return type; }

    public boolean isRequired() { return required; }

    public Object getDefaultValue() { return defaultValue; }

    public Object[] getValueSet() { return valueSet; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSourceParameter that = (DataSourceParameter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, type);
    }
}