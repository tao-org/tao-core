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

import java.util.Objects;

public class Triple<T,U,V> {
    private final T keyOne;
    private final U keyTwo;
    private final V keyThree;

    public Triple(T keyOne, U keyTwo, V keyThree) {
        if (keyOne == null || keyTwo == null) {
            throw new IllegalArgumentException("Keys cannot be null");
        }
        this.keyOne = keyOne;
        this.keyTwo = keyTwo;
        this.keyThree = keyThree;
    }

    public T getKeyOne() { return keyOne; }
    public U getKeyTwo() { return keyTwo; }
    public V getKeyThree() { return keyThree; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return keyOne.equals(triple.keyOne) &&
                keyTwo.equals(triple.keyTwo) &&
                Objects.equals(keyThree, triple.keyThree);
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyOne, keyTwo, keyThree);
    }
}
