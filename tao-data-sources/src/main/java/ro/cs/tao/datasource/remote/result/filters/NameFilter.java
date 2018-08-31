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

import java.util.Set;

/**
 * JSON filter that rejects attributes that are part of an exclusion list.
 *
 * @author Cosmin Cara
 */
public class NameFilter implements AttributeFilter {
    private final Set<String> namesToExclude;

    /**
     * Constructs an instance of this filter with a set of names to be excluded.
     * @param namesToExclude    The set of names
     */
    public NameFilter(Set<String> namesToExclude) {
        this.namesToExclude = namesToExclude;
    }

    @Override
    public boolean accept(String attributeName, String value) {
        return this.namesToExclude == null || !this.namesToExclude.contains(attributeName);
    }
}
