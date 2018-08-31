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
package ro.cs.tao.datasource.remote.result.filters;

/**
 * JSON attribute filter that rejects attributes with the given name if they don't have a given value.
 *
 * @author Cosmin Cara
 */
public class ValueFilter implements AttributeFilter {
    private final String value;

    /**
     * Constructs an instance of a ValueFilter with the given value
     * @param value The value to compare with.
     */
    public ValueFilter(String value) {
        this.value = value;
    }

    @Override
    public boolean accept(String attributeName, String value) {
        return this.value == null || !this.value.equals(value);
    }
}
