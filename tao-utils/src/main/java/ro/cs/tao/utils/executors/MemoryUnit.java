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
 * @author Cosmin Cara
 */
public enum MemoryUnit {
    BYTE(1),
    KB(BYTE.value * 1024),
    MB(KB.value * 1024),
    GB(MB.value * 1024),
    TB(GB.value * 1024);

    private final long value;

    MemoryUnit(long value) { this.value = value; }
    @Override
    public String toString() { return String.valueOf(this.value); }

    public Long value() { return this.value; }

    public static MemoryUnit getEnumConstantByName(String name) {
        return name != null ? MemoryUnit.valueOf(name.toUpperCase()) : null;
    }
}
