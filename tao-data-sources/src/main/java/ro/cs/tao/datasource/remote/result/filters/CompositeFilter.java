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

import java.util.ArrayList;
import java.util.List;

/**
 * Filter that aggregates other filters. It instructs the parser to accept an attribute only if all its aggregated
 * filters allow it.
 *
 * @author Cosmin Cara
 */
public class CompositeFilter implements AttributeFilter {
    private final List<AttributeFilter> filters;

    public CompositeFilter() {
        this.filters = new ArrayList<>();
    }

    public void addFilter(AttributeFilter filter) {
        this.filters.add(filter);
    }

    @Override
    public boolean accept(String attributeName, String value) {
        return this.filters.stream().allMatch(f -> f.accept(attributeName, value));
    }
}
