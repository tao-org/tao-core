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

/**
 * A parameter converter allows the conversion of parameter value between the parameter value type and its string representation.
 *
 * @param <T>   The type of the parameter value.
 *
 * @author Cosmin Cara
 */
public interface ParameterConverter<T> {
    /**
     * Returns the string value of the given object value.
     * @param value The value to be converted to string.
     */
    String stringValue(T value) throws ConversionException;

    /**
     * Returns the typed value of the parameter given its string representation.
     * Implementors should override this method.
     *
     * @param value The typed parameter value.
     */
    default T fromString(String value) throws ConversionException { return null; }
}
