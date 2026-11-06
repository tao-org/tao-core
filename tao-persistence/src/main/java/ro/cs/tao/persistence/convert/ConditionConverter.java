package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.enums.Condition;

public class ConditionConverter implements AttributeConverter<Condition, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Condition condition) {
        return condition != null ? condition.value() : null;
    }

    @Override
    public Condition convertToEntityAttribute(Integer integer) {
        return integer != null ? EnumUtils.getEnumConstantByValue(Condition.class, integer) : null;
    }
}

