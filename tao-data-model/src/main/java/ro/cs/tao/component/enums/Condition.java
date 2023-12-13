package ro.cs.tao.component.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum Condition implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    EQ(1, "Equals"),
    @XmlEnumValue("2")
    NEQ(2, "Not equals"),
    @XmlEnumValue("3")
    LT(3, "Less than"),
    @XmlEnumValue("4")
    LTE(4, "Less than or equal"),
    @XmlEnumValue("5")
    GT(5, "Greater than"),
    @XmlEnumValue("6")
    GTE(6, "Greater than or equal"),
    @XmlEnumValue("7")
    IN(7, "In"),
    @XmlEnumValue("8")
    NOTIN(8, "Not in"),
    @XmlEnumValue("9")
    NOTSET(9, "Not set"),
    @XmlEnumValue("10")
    SET(10, "Not in");

    private final int value;
    private final String description;

    Condition(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
