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

/**
 * Holder class for two objects that can be used as a key in a map.
 *
 * @author Cosmin Cara
 */
public class Tuple<T,V> {
    private final T keyOne;
    private final V keyTwo;

    public Tuple(T keyOne, V keyTwo) {
        if (keyOne == null || keyTwo == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        this.keyOne = keyOne;
        this.keyTwo = keyTwo;
    }

    public T getKeyOne() { return keyOne; }
    public V getKeyTwo() { return keyTwo; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple<?, ?> key = (Tuple<?, ?>) o;

        return keyOne.equals(key.keyOne) && keyTwo.equals(key.keyTwo);
    }

    @Override
    public int hashCode() {
        int result = keyOne.hashCode();
        result = 31 * result + keyTwo.hashCode();
        return result;
    }
}