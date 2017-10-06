package ro.cs.tao.persistence.convert;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;

/**
 * Converter for String[] stored values
 *
 * @author oana
 *
 */
@Converter
public class StringArrayToStringConverter implements AttributeConverter<String[], String> {

    @Override
    public String convertToDatabaseColumn(String[] attribute) {
        return Arrays.toString(attribute);
    }

    @Override
    public String[] convertToEntityAttribute(String dbData) {
        return dbData.replace("[", "").replace("]", "").split(", ");
    }
}
