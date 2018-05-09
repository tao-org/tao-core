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

package ro.cs.tao.workflow.enums;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum TransitionBehavior {
    /**
     * The execution flow should not continue if the current node fails.
     * This is the default (and only) value for regular nodes.
     * For group nodes, if a sub-task fails -> the group fails.
     */
    @XmlEnumValue("1")
    FAIL_ON_ERROR(1),
    /**
     * The execution flow should continue if the current node fails.
     * This is intended for group nodes, such that if a sub-task fails -> the current sub-flow fails,
     * but the group continues with the next sub-flow.
     */
    @XmlEnumValue("2")
    CONTINUE_ON_ERROR(2);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    TransitionBehavior(final int s)
    {
        value = s;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }

    public int value() { return this.value; }

    /**
     * Retrieve string enum token corresponding to the integer identifier
     * @param value the integer value identifier
     * @return the string token corresponding to the integer identifier
     */
    public static String getEnumConstantNameByValue(final int value) {
        for (TransitionBehavior type : values()) {
            if ((String.valueOf(value)).equals(type.toString())) {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }

    public static TransitionBehavior getEnumConstantByValue(final int value) {
        for (TransitionBehavior type : values()) {
            if (value == type.value) {
                // return the name of the enum constant having the given value
                return type;
            }
        }
        return null;
    }

}
