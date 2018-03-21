package ro.cs.tao.persistence.convert;

import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;

import javax.persistence.AttributeConverter;
import java.awt.*;

/**
 * Converter for Dimension  stored values
 */
public class DimensionConverter implements AttributeConverter<Dimension, String> {

    @Override
    public String convertToDatabaseColumn(Dimension attribute) {
        return JacksonUtil.toString(attribute);
    }

    @Override
    public Dimension convertToEntityAttribute(String dbData) {
        return JacksonUtil.fromString(dbData, Dimension.class);
    }
}