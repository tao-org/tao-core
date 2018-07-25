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
package ro.cs.tao.topology;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Possible statuses of a service.
 *
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
public enum ServiceStatus implements TaoEnum<Integer> {
    NOT_FOUND(1, "Not Found"),
    INSTALLED(2, "Installed"),
    UNINSTALLED(3, "Uninstalled"),
    ERROR(4, "Error");

    private final int value;
    private final String description;

    ServiceStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
