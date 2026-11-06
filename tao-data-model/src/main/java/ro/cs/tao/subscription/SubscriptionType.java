package ro.cs.tao.subscription;

import jakarta.xml.bind.annotation.XmlEnum;
import jakarta.xml.bind.annotation.XmlEnumValue;
import ro.cs.tao.TaoEnum;

/**
 * Enum representing different types of subscription strategies within the system.
 * This enumeration provides details for both fixed resource subscriptions and
 * pay-per-use subscriptions.
 *
 * Each type of subscription includes an integer value for identification
 * and a description for a more user-friendly representation.
 */
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

