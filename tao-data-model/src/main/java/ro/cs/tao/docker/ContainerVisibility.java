package ro.cs.tao.docker;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import ro.cs.tao.TaoEnum;

/**
 * Enumeration representing the visibility levels for a container.
 * This enum is decorated with {@code @XmlEnum} to allow XML binding and
 * implements the {@code TaoEnum<Integer>} interface to provide additional properties
 * such as value and friendly name.
 *
 * The available visibility levels are:
 * - UNDEFINED: The visibility is not validated (value 1).
 * - PRIVATE: The container is private (value 2).
 * - PUBLIC: The container is public (value 3).
 *
 * Each constant is associated with an integer value and a description.
 */
@XmlEnum(Integer.class)
public enum ContainerVisibility implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    UNDEFINED(1, "Not validated"),
    @XmlEnumValue("2")
    PRIVATE(2, "Private"),
    @XmlEnumValue("3")
    PUBLIC(3, "Public");

    private final int value;
    private final String description;

    ContainerVisibility(int value, String description) {
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

