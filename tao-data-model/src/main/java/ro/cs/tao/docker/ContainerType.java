package ro.cs.tao.docker;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum ContainerType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    DOCKER(1, "Docker"),
    @XmlEnumValue("2")
    WPS(2, "Web Processing Service"),
    @XmlEnumValue("3")
    UTILITY(3, "Utility"),
    @XmlEnumValue("4")
    STAC(4, "STAC Service");

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
