package ro.cs.tao.persistence.convert;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for Class<?>  stored values
 *
 * @author oana
 */
public class DataTypeConverter implements AttributeConverter<Class<?>, String> {

    @Override
    public String convertToDatabaseColumn(Class<?> attribute) {
        return attribute != null ? attribute.getCanonicalName() : null;
    }

    @Override
    public Class<?> convertToEntityAttribute(String dbData) {
        Class<?> clasz = null;
        try {
            if (dbData != null) {
                clasz = ClassLoader.getSystemClassLoader().loadClass(dbData);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clasz;
    }
}
