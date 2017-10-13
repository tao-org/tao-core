package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.SensorType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for SensorType enum stored values
 *
 * @author oana
 *
 */
@Converter
public class SensorTypeConverter implements AttributeConverter<SensorType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SensorType attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public SensorType convertToEntityAttribute(Integer dbData) {
        return SensorType.valueOf(SensorType.getEnumConstantNameByValue(dbData));
    }
}
