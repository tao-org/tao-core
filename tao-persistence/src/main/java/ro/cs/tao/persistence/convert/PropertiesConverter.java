package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.utils.JacksonUtil;

import java.util.Properties;

public class PropertiesConverter implements AttributeConverter<Properties, String> {

    @Override
    public String convertToDatabaseColumn(Properties attribute) {
        return attribute == null ? null : JacksonUtil.toString(attribute);
    }

    @Override
    public Properties convertToEntityAttribute(String dbData) {
        return dbData == null ? null : JacksonUtil.fromString(dbData, Properties.class);
    }
}
