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
 * Image data formats
 */
@XmlEnum(Integer.class)
public enum DataFormat implements TaoEnum<Integer> {
    /**
     * RASTER image data format
     */
    @XmlEnumValue("1")
    RASTER(1, "Raster"),
    /**
     * VECTOR image data format
     */
    @XmlEnumValue("2")
    VECTOR(2, "Vector"),
    /**
     * Other image data format
     */
    @XmlEnumValue("3")
    OTHER(3, "Unknown");

    /**
     * Numerical value for enum constants
     */
    private final int value;
    private final String description;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    DataFormat(final int s, final String desc) {
        this.value = s;
        this.description = desc;
    }


    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
