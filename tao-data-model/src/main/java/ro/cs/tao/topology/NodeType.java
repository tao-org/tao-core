package ro.cs.tao.topology;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum NodeType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    S(1, "Small"),
    @XmlEnumValue("2")
    M(2, "Medium"),
    @XmlEnumValue("3")
    L(3, "Large"),
    @XmlEnumValue("4")
    XL(4, "X-Large");

    private final int value;
    private final String description;

    NodeType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
