package ro.cs.tao.component.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

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
