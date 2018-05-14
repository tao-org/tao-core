package ro.cs.tao.persistence.convert;

import javax.persistence.AttributeConverter;

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
                if (!dbData.endsWith("[]")) {
                    clasz = ClassLoader.getSystemClassLoader().loadClass(dbData);
                } else {
                    switch (dbData) {
                        case "java.lang.Integer[]":
                            clasz = Integer[].class;
                            break;
                        case "java.lang.Float[]":
                            clasz = Float[].class;
                            break;
                        case "java.lang.Boolean[]":
                            clasz = Boolean[].class;
                            break;
                        case "java.lang.String[]":
                        default:
                            clasz = String[].class;
                            break;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return clasz;
    }
}
