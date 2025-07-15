package ro.cs.tao.docker;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Enum representing different types of container services.
 *
 * Each constant in this enumeration has an associated integer value and a description
 * that represents the type of container. This is used to categorize container services
 * in various applications.
 *
 * The enum implements the {@code TaoEnum<Integer>} interface which provides the ability
 * to retrieve the friendly name and integer value of each enum constant.
 *
 * The enum is annotated with {@code @XmlEnum} to support XML serialization and deserialization.
 */
@XmlEnum(Integer.class)
public enum ContainerType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    DOCKER(1, "Docker"),
    @XmlEnumValue("2")
    WPS(2, "Web Processing Service"),
    @XmlEnumValue("3")
    UTILITY(3, "Utility"),
    @XmlEnumValue("4")
    STAC(4, "STAC Service"),
    @XmlEnumValue("5")
    WMS(5, "Web Map Service"),
    @XmlEnumValue("6")
    WMTS(6, "Web Map Tile Service");

    private final int value;
    private final String description;

    ContainerType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Integer value() {
        return this.value;
    }
}
