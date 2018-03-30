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
package ro.cs.tao.eodata.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum PixelType {
    @XmlEnumValue("1")
    UINT8(1),
    @XmlEnumValue("2")
    INT8(2),
    @XmlEnumValue("3")
    UINT16(3),
    @XmlEnumValue("4")
    INT16(4),
    @XmlEnumValue("5")
    UINT32(5),
    @XmlEnumValue("6")
    INT32(6),
    @XmlEnumValue("7")
    FLOAT32(7),
    @XmlEnumValue("8")
    FLOAT64(8);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    PixelType(final int s)
    {
        value = s;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }

    /**
     * Retrieve string enum token corresponding to the integer identifier
     * @param value the integer value identifier
     * @return the string token corresponding to the integer identifier
     */
    public static String getEnumConstantNameByValue(final int value) {
        for (PixelType type : values()) {
            if ((String.valueOf(value)).equals(type.toString())) {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }

    public static PixelType getEnumConstantByValue(final int value) {
        for (PixelType type : values()) {
            if ((String.valueOf(value)).equals(type.toString())) {
                return type;
            }
        }
        return null;
    }
}
