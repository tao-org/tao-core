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

package ro.cs.tao.workflow.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enumeration representing different types of components used in a workflow system.
 * Each type is associated with an integer value and a descriptive text.
 *
 * This enumeration implements the {@code TaoEnum<Integer>} interface to provide
 * additional properties and behaviors like obtaining a friendly name and its value.
 */
@XmlEnum(Integer.class)
public enum ComponentType implements TaoEnum<Integer> {
    /**
     * Designates a datasource component
     */
    @XmlEnumValue("1")
    DATASOURCE(1, "Data Source"),
    /**
     * Designates a processing component
     */
    @XmlEnumValue("2")
    PROCESSING(2, "Processing Component"),
    /**
     * Designates a grouping component
     */
    @XmlEnumValue("3")
    GROUP(3, "Group Component"),
    /**
     * Designates a data source group component
     */
    @XmlEnumValue("4")
    DATASOURCE_GROUP(4, "Data Source Group"),
    /**
     * Designates a WPS component
     */
    @XmlEnumValue("5")
    WPS_COMPONENT(5, "WPS Component"),
    /**
     * Designates a condition component
     */
    @XmlEnumValue("6")
    CONDITION(6, "Condition"),
    /**
     * Designates an OGC Process component
     */
    OGC_PROCESS(7, "OGC Process"),
    /**
     * Represents a pass-through component in the workflow system.
     *
     * A pass-through component does not perform any specific processing or operations
     * on the data it receives. Instead, it simply passes the data through to the next
     * component in the workflow as-is. This type of component can be used in workflows
     * to maintain a seamless flow of data without altering its content.
     */
    PASS_THROUGH(8, "Pass-through Component");

    private final int value;
    private final String description;

    ComponentType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
