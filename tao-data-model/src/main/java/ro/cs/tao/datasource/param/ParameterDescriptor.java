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
 */
package ro.cs.tao.datasource.param;

/**
 * @author Cosmin Cara
 */
public class ParameterDescriptor {
    private final String name;
    private final Class type;
    private final Object defaultValue;
    private final boolean required;

    public ParameterDescriptor(String name, Class type) {
        this(name, type, null, false);
    }

    public ParameterDescriptor(String name, Class type, boolean required) {
        this(name, type, null, required);
    }

    public ParameterDescriptor(String name, Class type, Object defaultValue) {
        this(name, type, defaultValue, false);
    }

    public ParameterDescriptor(String name, Class type, Object defaultValue, boolean required) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public Class getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Object getDefaultValue() { return defaultValue; }
}
