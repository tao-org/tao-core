package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.PixelType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for PixelType enum stored values
 *
 * @author oana
 */
public class PixelTypeConverter implements AttributeConverter<PixelType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PixelType attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public PixelType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? PixelType.valueOf(PixelType.getEnumConstantNameByValue(dbData)) : null;
    }
}
