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
package ro.cs.tao.eodata.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum PixelType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    UINT8(1, "Unsigned byte"),
    @XmlEnumValue("2")
    INT8(2, "Signed byte"),
    @XmlEnumValue("3")
    UINT16(3, "Unsigned short"),
    @XmlEnumValue("4")
    INT16(4, "Signed short"),
    @XmlEnumValue("5")
    UINT32(5, "Unsigned integer"),
    @XmlEnumValue("6")
    INT32(6, "Signed integer"),
    @XmlEnumValue("7")
    FLOAT32(7, "Float"),
    @XmlEnumValue("8")
    FLOAT64(8, "Double");

    /**
     * Numerical value for enum constants
     */
    private final int value;
    private final String description;

    PixelType(int value, String description) {
        this.value = value;
        this.description = description;
    }


    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
