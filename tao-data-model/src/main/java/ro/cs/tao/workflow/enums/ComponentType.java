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

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum ComponentType implements TaoEnum<Integer> {

    @XmlEnumValue("1")
    DATASOURCE(1, "Data Source"),
    /**
     * The workflow is validated and ready to be executed, and may still be edited
     */
    @XmlEnumValue("2")
    PROCESSING(2, "Processing Component"),
    /**
     * The workflow was published and hence cannot be edited
     */
    @XmlEnumValue("3")
    GROUP(3, "Group Component");

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