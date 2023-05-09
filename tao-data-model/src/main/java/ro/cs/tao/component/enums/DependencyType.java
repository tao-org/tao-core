package ro.cs.tao.component.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum DependencyType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    EXCLUSIVE(1, "Exclusive"),
    @XmlEnumValue("2")
    FILTER(2, "Filter");

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
