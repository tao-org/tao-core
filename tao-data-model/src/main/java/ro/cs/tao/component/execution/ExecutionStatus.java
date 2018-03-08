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
package ro.cs.tao.component.execution;

import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Cosmin Udroiu
 */
public enum ExecutionStatus {
    /**
     * Job status cannot be determined.
     */
    @XmlEnumValue("0")
    UNDETERMINED(0),
    /**
     *  Job is queued and active.
     */
    @XmlEnumValue("1")
    QUEUED_ACTIVE(1),
    /**
     * Job is running.
     */
    @XmlEnumValue("2")
    RUNNING(2),
    /**
     * Job is suspended.
     */
    @XmlEnumValue("3")
    SUSPENDED(3),
    /**
     * Job has finished normally.
     */
    @XmlEnumValue("4")
    DONE(4),
    /**
     * Job finished, but terminated abnormally.
     */
    @XmlEnumValue("5")
    FAILED(5),
    /**
     * Job cancelled.
     */
    @XmlEnumValue("6")
    CANCELLED(6);

    /**
     * Numerical value for enum constants
     */
    private final int value;

    /**
     * Constructor
     * @param s - the integer value identifier
     */
    ExecutionStatus(final int s)
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
    public static String getEnumConstantNameByValue(final int value)
    {
        for (ExecutionStatus type : values())
        {
            if ((String.valueOf(value)).equals(type.toString()))
            {
                // return the name of the enum constant having the given value
                return type.name();
            }
        }
        return null;
    }

}
