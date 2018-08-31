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
package ro.cs.tao.component;

import java.io.Serializable;

/**
 * Interface that signals an object that has an identifier.
 *
 * @param <T>   The type of the identifier
 *
 * @author Cosmin Cara
 */
public interface Identifiable<T> extends Cloneable, Serializable {

    /**
     * Returns the ID of this instance.
     */
    T getId();

    /**
     * Sets the ID of this instance.
     * Warning: For mapped entities that have ID generators, the ID of a new entity should not be set.
     *
     * @param id    The ID value.
     */
    void setId(T id);

    /**
     * Returns the default ID value that an instance should have when created.
     */
    T defaultId();
}
