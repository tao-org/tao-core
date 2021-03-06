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

/**
 * @author Cosmin Cara
 * @author Oana H.
 */
@XmlEnum
public enum OrbitDirection implements TaoEnum<Integer> {
    ASCENDING(1, "Ascending"),
    DESCENDING(2, "Descending");

    /**
     * Numerical value for enum constants
     */
    private final int value;
    private final String description;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    OrbitDirection(final int s, final String desc) {
        this.value = s; this.description = desc;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Integer value() {
        return this.value;
    }
}
