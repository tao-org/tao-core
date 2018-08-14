/*
 * Copyright (C) 2017 CS ROMANIA
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

import java.util.Arrays;

/**
 * @author Cosmin Cara
 */
public enum ByteUnit {
    BYTE(1),
    KILOBYTE(1024),
    MEGABYTE(1024*1024),
    GIGABYTE(1024*1024*1024);

    private final int value;

    ByteUnit(int value) { this.value = value; }
    @Override
    public String toString() { return String.valueOf(this.value); }

    public int value() { return this.value; }

    public static String getEnumConstantNameByValue(final int value) {
        return Arrays.stream(values()).filter(t -> String.valueOf(value).equals(t.toString())).findFirst().get().name();
    }

}
