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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents different categories of components for processing and classification purposes.
 * Each category is associated with a unique integer value and a human-readable description.
 *
 * This enumeration includes the following component categories:
 * - RASTER: Represents raster data processing components.
 * - VECTOR: Represents vector data processing components.
 * - OPTICAL: Represents optical data processing components.
 * - RADAR: Represents radar data processing components.
 * - MISC: Represents miscellaneous components.
 * - CONDITION: Represents condition-related components.
 *
 * The enum provides methods to:
 * - Retrieve the friendly name (human-readable description) of a component category.
 * - Retrieve the integer value associated with a particular component category.
 * - Determine compatibility between a source category and other categories via a compatibility map.
 *
 * Implements the {@link TaoEnum} interface which includes methods to get the friendly name
 * and associated value of each enum constant.
 *
 * Compatibility Map:
 * Each component category defines a set of other categories it is compatible with.
 * This information is stored in a static compatibility map.
 * The compatibility check can be performed using the {@code isCompatibleWith} method.
 *
 * Serialization/Deserialization:
 * The enum values are annotated with {@code @XmlEnumValue} for serialization and deserialization
 * to and from XML, using their respective integer values.
 */
@XmlEnum(Integer.class)
public enum ComponentCategory implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    RASTER(1, "Raster"),
    @XmlEnumValue("2")
    VECTOR(2, "Vector"),
    @XmlEnumValue("3")
    OPTICAL(3, "Optical"),
    @XmlEnumValue("4")
    RADAR(4, "Radar"),
    @XmlEnumValue("5")
    MISC(5, "Miscellaneous"),
    @XmlEnumValue("6")
    CONDITION(6, "Condition");

    // Other component categories that can be used as input for this category
    private static final Map<ComponentCategory, EnumSet<ComponentCategory>> compatibilityMap;

    static {
        compatibilityMap = new HashMap<>();
        compatibilityMap.put(RASTER, EnumSet.of(RASTER, OPTICAL, RADAR, MISC, CONDITION));
        compatibilityMap.put(VECTOR, EnumSet.of(VECTOR, RASTER, MISC, CONDITION));
        compatibilityMap.put(OPTICAL, EnumSet.of(OPTICAL, RASTER, MISC, CONDITION));
        compatibilityMap.put(RADAR, EnumSet.of(RADAR, RASTER, MISC, CONDITION));
        compatibilityMap.put(MISC, EnumSet.of(RASTER, VECTOR, OPTICAL, RADAR, MISC));
        compatibilityMap.put(CONDITION, EnumSet.of(RASTER, VECTOR, OPTICAL, RADAR, MISC));
    }

    private final int value;
    private final String description;

    /**
     * Checks whether two {@link ComponentCategory} instances are compatible with each other.
     * Compatibility is determined based on pre-defined mappings stored in the compatibility map.
     *
     * @param source the source {@link ComponentCategory} to be checked for compatibility
     * @param other  the target {@link ComponentCategory} to check compatibility against
     * @return {@code true} if the source component category is compatible with the target component category;
     *         {@code false} otherwise
     */
    public static boolean isCompatibleWith(ComponentCategory source, ComponentCategory other) {
        return compatibilityMap.get(source).contains(other);
    }

    ComponentCategory(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
