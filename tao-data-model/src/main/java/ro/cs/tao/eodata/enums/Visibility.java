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
 * Workflow visibility enumeration
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum Visibility implements TaoEnum<Integer> {
    /**
     * The workflow is public, any user can see/use it
     */
    @XmlEnumValue("1")
    PUBLIC(1, "Public", true),
    /**
     * The workflow is private (visible only by the author)
     */
    @XmlEnumValue("2")
    PRIVATE(2, "Private", true),
    /**
     * The workflow is visible only by the users who subscribed to use it
     */
    @XmlEnumValue("3")
    SUBSCRIPTION(3, "Subscription", false);

    private final int value;
    private final String description;
    private final boolean visible;

    Visibility(int value, String description, boolean visible) {
        this.value = value;
        this.description = description;
        this.visible = visible;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }

    @Override
    public boolean isVisible() {
        return this.visible;
    }
}