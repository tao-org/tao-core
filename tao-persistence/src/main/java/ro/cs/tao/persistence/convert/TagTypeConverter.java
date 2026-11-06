package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.enums.TagType;

public class TagTypeConverter implements AttributeConverter<TagType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TagType attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public TagType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? EnumUtils.getEnumConstantByValue(TagType.class, dbData) : null;
    }
}

