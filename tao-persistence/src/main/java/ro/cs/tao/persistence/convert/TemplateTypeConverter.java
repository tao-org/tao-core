package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.template.TemplateType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for TemplateType enum stored values
 *
 * @author oana
 */
public class TemplateTypeConverter implements AttributeConverter<TemplateType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TemplateType attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public TemplateType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? TemplateType.valueOf(TemplateType.getEnumConstantNameByValue(dbData)) : null;
    }
}
