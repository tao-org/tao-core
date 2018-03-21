package ro.cs.tao.persistence.convert;

import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;

import javax.persistence.AttributeConverter;
import java.awt.*;
import java.util.Map;

/**
 * @author Oana H.
 */
public class QueryValuesMapConverter implements AttributeConverter<Map<String, String>, String> {

    @Override
    public String convertToDatabaseColumn(Map<String, String> attribute) {
        return JacksonUtil.toString(attribute);
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String dbData) {
        return JacksonUtil.fromString(dbData, Map.class);
    }
}