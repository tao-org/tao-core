package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.enums.SensorType;

import javax.persistence.AttributeConverter;

/**
 * Converter for SensorType enum stored values
 *
 * @author oana
 */
public class SensorTypeConverter implements AttributeConverter<SensorType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SensorType attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public SensorType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? SensorType.valueOf(SensorType.getEnumConstantNameByValue(dbData)) : null;
    }
}
