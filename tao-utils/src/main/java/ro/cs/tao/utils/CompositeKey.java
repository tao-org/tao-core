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

package ro.cs.tao.utils;

import java.util.Arrays;

/**
 * Helper class for using multiple objects as a key in a map.
 *
 * @author Cosmin Cara
 */
public class CompositeKey {
    private final Object[] composites;

    public CompositeKey(Object...composites) {
        this.composites = composites;
    }

    public Object getKey(int index) {
        return composites != null && composites.length > index ? composites[index] : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeKey that = (CompositeKey) o;
        return Arrays.equals(composites, that.composites);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(composites);
    }
}
