package ro.cs.tao.persistence.convert;

import ro.cs.tao.utils.JacksonUtil;

import javax.persistence.AttributeConverter;
import java.util.Set;

public class SetConverter implements AttributeConverter<Set<String>, String> {

    @Override
    public String convertToDatabaseColumn(Set<String> attribute) {
        return JacksonUtil.toString(attribute);
    }

    @Override
    public Set<String> convertToEntityAttribute(String dbData) {
        return JacksonUtil.fromString(dbData, Set.class);
    }
}