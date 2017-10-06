package ro.cs.tao.persistence.convert;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for Class<?>  stored values
 *
 * @author oana
 *
 */
@Converter
public class DataTypeConverter implements AttributeConverter<Class<?> , String> {

    @Override
    public String convertToDatabaseColumn(Class<?>  attribute) {
        return attribute.getCanonicalName();
    }

    @Override
    public Class<?> convertToEntityAttribute(String dbData) {
        Class<?> clasz = null;
        try {
            clasz = ClassLoader.getSystemClassLoader().loadClass(dbData);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clasz;
    }
}
