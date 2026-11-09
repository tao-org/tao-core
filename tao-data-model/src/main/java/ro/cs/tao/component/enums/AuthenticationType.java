package ro.cs.tao.component.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Represents the types of authentication mechanisms supported.
 * Provides an association of an integer value and a human-readable description
 * for each authentication type.
 * Implemented as an enumeration that adheres to the {@link TaoEnum} interface.
 *
 * The available authentication types are:
 * - NONE: No authentication is required.
 * - BASIC: Basic username and password-based authentication.
 * - TOKEN: Token-based authentication.
 *
 * Each constant in the enumeration is mapped to a specific integer value
 * which can be serialized/deserialized using the {@code @XmlEnumValue} annotation.
 *
 * Implements the following methods defined in the {@link TaoEnum} interface:
 * - {@code friendlyName()}: Returns the human-readable description of the authentication type.
 * - {@code value()}: Returns the associated integer value of the authentication type.
 */
@XmlEnum(Integer.class)
public enum AuthenticationType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    NONE(1, "None"),
    @XmlEnumValue("2")
    BASIC(2, "Basic"),
    @XmlEnumValue("3")
    TOKEN(3, "Token-based");

    private final int value;
    private final String description;

    AuthenticationType(int value, String description) {
        this.value = value;
        this.description = description;
    }


    @Override
    public String friendlyName() {
        return description;
    }

    @Override
    public Integer value() {
        return value;
    }
}
