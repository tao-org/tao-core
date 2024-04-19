package ro.cs.tao.docker;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

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
