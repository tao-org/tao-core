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

package ro.cs.tao.eodata;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface to be implemented by classes that are supposed to handle (do something with) a collection of items.
 *
 * @param <T>   The type of the item to be handled
 *
 * @author Cosmin Cara
 */
public interface OutputDataHandler<T> {
    /**
     * Returns the type for which this handler is intended for.
     */
    Class<T> isIntendedFor();

    /**
     * Returns the priority of this handler among the handlers for the intended type.
     * The highest priority is zero.
     */
    int getPriority();

    /**
     * Performs the operation for which this handler is intended on the given list of items.
     *
     * @param list  The items to be handled
     * @return  The list of updated items.
     *
     */
    List<T> handle(List<T> list) throws DataHandlingException;

    /**
     * Performs the operation for which this handler is intended on the given item.
     *
     * @param item  The item to be handled
     * @return  The updated item.
     *
     */
    default T handle(T item) throws DataHandlingException {
        List<T> list = handle(new ArrayList<T>() {{ add(item); }});
        return list != null && list.size() == 1 ? list.get(0) : null;
    }
}
