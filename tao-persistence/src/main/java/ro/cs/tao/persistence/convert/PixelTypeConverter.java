package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.PixelType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for PixelType enum stored values
 *
 * @author oana
 *
 */
@Converter
public class PixelTypeConverter implements AttributeConverter<PixelType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PixelType attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public PixelType convertToEntityAttribute(Integer dbData) {
        return PixelType.valueOf(PixelType.getEnumConstantNameByValue(dbData));
    }
}
