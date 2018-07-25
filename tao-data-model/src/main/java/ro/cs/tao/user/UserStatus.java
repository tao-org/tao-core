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
package ro.cs.tao.user;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * User (activation) status enumeration
 * @author Oana H.
 */
@XmlEnum(Integer.class)
public enum UserStatus implements TaoEnum<Integer> {

    /**
     * The user is pending activation
     */
    @XmlEnumValue("1")
    PENDING(1, "Pending activation"),
    /**
     * The user is active
     */
    @XmlEnumValue("2")
    ACTIVE(2, "Active"),
    /**
     * The user is disabled (no longer active)
     */
    @XmlEnumValue("3")
    DISABLED(3, "Account disabled");

    private final int value;
    private final String description;

    UserStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
