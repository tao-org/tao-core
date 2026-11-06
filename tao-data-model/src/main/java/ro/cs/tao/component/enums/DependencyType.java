package ro.cs.tao.component.enums;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import ro.cs.tao.TaoEnum;

/**
 * Represents different types of dependencies with associated values and descriptions.
 * Each dependency type has a unique integer value and a descriptive friendly name.
 *
 * This enumeration implements the {@link TaoEnum} interface to provide additional properties
 * such as a friendly name and value. It is annotated with {@code @XmlEnum} to facilitate
 * XML binding and serialization.
 */
@XmlEnum(Integer.class)
public enum DependencyType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    EXCLUSIVE(1, "Exclusive"),
    @XmlEnumValue("2")
    FILTER(2, "Filter"),
    @XmlEnumValue("3")
    REQUIRED_IF(3, "Required if");

    private final int value;
    private final String description;

    DependencyType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}

