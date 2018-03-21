package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.ParameterType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for ParameterType enum stored values
 *
 * @author oana
 */
public class ParameterTypeConverter implements AttributeConverter<ParameterType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ParameterType attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public ParameterType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? ParameterType.valueOf(ParameterType.getEnumConstantNameByValue(dbData)) : null;
    }
}
