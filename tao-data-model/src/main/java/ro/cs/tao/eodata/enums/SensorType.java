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

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import ro.cs.tao.TaoEnum;
import ro.cs.tao.serialization.SensorTypeDeserializer;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * @author Cosmin Cara
 */
@XmlEnum(Integer.class)
@JsonDeserialize(using = SensorTypeDeserializer.class)
public enum SensorType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    OPTICAL(1, "Optical"),
    @XmlEnumValue("2")
    RADAR(2, "Radar"),
    @XmlEnumValue("3")
    ALTIMETRIC(3, "Altimetric"),
    @XmlEnumValue("4")
    ATMOSPHERIC(4, "Atmospheric"),
    @XmlEnumValue("5")
    UNKNOWN(5, "Unknown"),
    @XmlEnumValue("6")
    PASSIVE_MICROWAVE(6, "Passive Microwave");

    private final int value;
    private final String description;

    SensorType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
