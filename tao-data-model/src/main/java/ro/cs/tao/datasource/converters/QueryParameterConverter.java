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

/**
 * A query parameter converter translates the value of a query parameter (typed) into a string that can be
 * understood by a data source.
 *
 * @author Cosmin Cara
 */
public interface QueryParameterConverter {
    /**
     * The string representation of a parameter, as expected by the data source.
     */
    String stringValue() throws ConversionException;
}
