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

package ro.cs.tao;

/**
 * Interface for decorating enumerations with additional properties.
 *
 * @param <T>   The value type of the actual enum.
 */
public interface TaoEnum<T> {
    /**
     * Returns the friendly name of the enumeration constant.
     */
    String friendlyName();

    /**
     * Returns the value associated to the enumeration constant.
     */
    T value();

    /**
     * Returns <code>true</code> if the pair should be visible/usable by upper layers
     */
    default boolean isVisible() { return true; }
}
