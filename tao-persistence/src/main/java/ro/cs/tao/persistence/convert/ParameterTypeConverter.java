package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.ParameterType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for ParameterType enum stored values
 *
 * @author oana
 *
 */
@Converter
public class ParameterTypeConverter implements AttributeConverter<ParameterType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ParameterType attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public ParameterType convertToEntityAttribute(Integer dbData) {
        return ParameterType.valueOf(ParameterType.getEnumConstantNameByValue(dbData));
    }
}
