package ro.cs.tao.user;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

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
