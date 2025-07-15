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

/**
 * Represents the various types of processing components that can be defined in the system.
 * Each component type is associated with a unique integer value and a descriptive name.
 *
 * This enumeration implements the {@link TaoEnum} interface to provide additional properties such as a friendly name and a value.
 * It is annotated with {@code @XmlEnum} to enable XML binding and serialization.
 *
 * Enum Constants:
 * - EXECUTABLE: Represents an executable component, identified by the value 1.
 * - SCRIPT: Represents a scripting component, identified by the value 2.
 * - AGGREGATE: Represents an aggregate component, identified by value 3.
 * - EXTERNAL: Represents an external component, identified by the value 4.
 * - UTILITY: Represents a utility component, identified by the value 5.
 */
@XmlEnum(Integer.class)
public enum ProcessingComponentType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    EXECUTABLE(1, "Executable"),
    @XmlEnumValue("2")
    SCRIPT(2, "Script"),
    @XmlEnumValue("3")
    AGGREGATE(3, "Aggregate"),
    @XmlEnumValue("4")
    EXTERNAL(4, "External"),
    @XmlEnumValue("5")
    UTILITY(5, "Utility");

    private final int value;
    private final String description;

    ProcessingComponentType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
