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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Container for holding a map of field names and sort direction, that is used by entity managers.
 * It acts as a bridge between the TAO framework and JPA persistence.
 *
 * @author Cosmin Cara
 */
public class Sort {
    private final Map<String, SortDirection> fieldsToSort;

    public static Sort by(String fieldName, SortDirection direction) {
        return new Sort(fieldName, direction);
    }

    public Sort() {
        this.fieldsToSort = new LinkedHashMap<>();
    }

    public Sort(String fieldName, SortDirection direction) {
        this();
        this.fieldsToSort.put(fieldName, direction);
    }

    public Sort withField(String fieldName, SortDirection direction) {
        this.fieldsToSort.put(fieldName, direction);
        return this;
    }

    public Map<String, SortDirection> getFieldsForSort() {
        return this.fieldsToSort;
    }
}
