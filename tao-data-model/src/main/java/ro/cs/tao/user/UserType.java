package ro.cs.tao.user;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import ro.cs.tao.TaoEnum;

/**
 * Enumeration representing different types of users in the system.
 * This enum is annotated with {@code @XmlEnum} to enable XML representation
 * and mapping, and it implements {@code TaoEnum<Integer>} to provide
 * additional metadata and functionality for the enumeration values.
 *
 * Each type of user is associated with a unique integer value and a friendly
 * name description.
 *
 * Available User Types:
 * - INTERNAL: Represents a local user managed within the system.
 * - LDAP: Represents a user authenticated via LDAP.
 * - KEYCLOAK: Represents a user authenticated using Keycloak.
 * - GITHUB: Represents a user authenticated using GitHub.
 *
 * Implements methods from the {@code TaoEnum} interface:
 * - {@code friendlyName()}: Provides the user-friendly name of the user type.
 * - {@code value()}: Retrieves the integer value associated with the user type.
 */
@XmlEnum(Integer.class)
public enum UserType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    INTERNAL(1, "Local"),
    @XmlEnumValue("2")
    LDAP(2, "LDAP"),
    @XmlEnumValue("3")
    KEYCLOAK(3, "Keycloak"),
    @XmlEnumValue("4")
    GITHUB(4, "Github");

    private final int value;
    private final String description;

    UserType(int value, String description) {
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

