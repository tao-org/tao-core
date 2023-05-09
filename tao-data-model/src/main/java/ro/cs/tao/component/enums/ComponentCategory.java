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

package ro.cs.tao.component.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum ComponentCategory implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    RASTER(1, "Raster"),
    @XmlEnumValue("2")
    VECTOR(2, "Vector"),
    @XmlEnumValue("3")
    OPTICAL(3, "Optical"),
    @XmlEnumValue("4")
    RADAR(4, "Radar"),
    @XmlEnumValue("5")
    MISC(5, "Miscellaneous");

    private final int value;
    private final String description;

    ComponentCategory(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
