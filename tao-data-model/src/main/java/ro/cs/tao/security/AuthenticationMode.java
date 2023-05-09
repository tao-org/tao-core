package ro.cs.tao.security;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Authentication type (mode) enumeration
 */
@XmlEnum(Integer.class)
public enum AuthenticationMode implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    LOCAL(1, "Local database authentication"),
    @XmlEnumValue("2")
    LDAP(2, "Active Directory authentication"),
    @XmlEnumValue("4")
    KEYCLOAK(4, "Keycloak (OAuth2) authentication"),
    @XmlEnumValue("7")
    ANY(7, "All supported authentication providers, in order");

    private final int value;
    private final String description;

    AuthenticationMode(int value, String description) {
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
