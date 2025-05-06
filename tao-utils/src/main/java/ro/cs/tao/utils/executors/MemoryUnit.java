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
package ro.cs.tao.utils.executors;

/**
 * An enumeration representing memory units, ranging from bytes to terabytes.
 * Each constant in this enumeration is associated with a unique identifier (id)
 * and a specific numeric value representing the size of the memory unit in bytes.
 */
public enum MemoryUnit {
    BYTE(1, 1),
    KB(2, BYTE.value * 1024),
    MB(3, KB.value * 1024),
    GB(4, MB.value * 1024),
    TB(5, GB.value * 1024);

    private final int id;
    private final long value;

    MemoryUnit(int id, long value) {
        this.id = id;
        this.value = value;
    }
    @Override
    public String toString() { return String.valueOf(this.value); }

    public int id() { return id; }

    public long value() { return this.value; }

    public static MemoryUnit getEnumConstantByName(String name) {
        return name != null ? MemoryUnit.valueOf(name.toUpperCase()) : null;
    }

    public static MemoryUnit getEnumConstantById(int value) {
        return value == BYTE.id
               ? BYTE
               : value == KB.id
                 ? KB
                 : value == MB.id
                   ? MB
                   : value == GB.id
                     ? GB
                     : value == TB.id
                       ? TB
                       : null;
    }

    public static MemoryUnit getEnumConstantByValue(long value) {
        return value == BYTE.value
                 ? BYTE
                 : value == KB.value
                   ? KB
                   : value == MB.value
                     ? MB
                     : value == GB.value
                       ? GB
                       : value == TB.value
                         ? TB
               : null;
    }
}
