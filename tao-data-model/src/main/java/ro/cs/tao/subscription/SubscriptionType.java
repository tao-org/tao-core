package ro.cs.tao.subscription;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum SubscriptionType implements TaoEnum<Integer> {
    @XmlEnumValue("1")
    FIXED_RESOURCES(1, "Fixed resources"),
    @XmlEnumValue("2")
    PAY_PER_USE(2, "Pay per use");

    private final int value;
    private final String description;

    SubscriptionType(int value, String description) {
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
