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
 * Interface defining a filter for JSON attributes.
 * It instructs the parser to keep or not the respective attribute in the final result.
 *
 * @author Cosmin Cara
 */
public interface AttributeFilter {
    /**
     * Instructs the caller to accept or not the current attribute, based on its value.
     *
     * @param attributeName The name of the attribute
     * @param value         The value of the attribute
     *
     * @return  <code>true</code> if the attribute passes this filter, <code>false</code> otherwise
     */
    default boolean accept(String attributeName, String value) { return true; }
}
