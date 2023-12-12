package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.subscription.SubscriptionType;

import javax.persistence.AttributeConverter;

public class SubscriptionTypeConverter implements AttributeConverter<SubscriptionType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(SubscriptionType type) {
        return type != null ? type.value() : null;
    }

    @Override
    public SubscriptionType convertToEntityAttribute(Integer integer) {
        return integer != null ? EnumUtils.getEnumConstantByValue(SubscriptionType.class, integer) : null;
    }
}
