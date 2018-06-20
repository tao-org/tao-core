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

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * User (activation) status enumeration
 * @author Oana H.
 */
@XmlEnum(Integer.class)
public enum UserStatus {

    /**
     * The user is pending activation
     */
    @XmlEnumValue("1")
    PENDING(1),
    /**
     * The user is active
     */
    @XmlEnumValue("2")
    ACTIVE(2),
    /**
     * The user is disabled (no longer active)
     */
    @XmlEnumValue("3")
    DISABLED(3);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    UserStatus(final int s)
    {
        value = s;
    }

    public int value() { return value; }

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
        for (UserStatus type : values()) {
            if ((String.valueOf(value)).equals(type.toString())) {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }

    public static UserStatus getEnumConstantByValue(final int value) {
        for (UserStatus type : values()) {
            if (value == type.value()) {
                // return the name of the enum constant having the given value
                return type;
            }
        }
        return null;
    }
}
