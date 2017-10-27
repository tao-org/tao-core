package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.template.TemplateType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for TemplateType enum stored values
 *
 * @author oana
 *
 */
public class TemplateTypeConverter implements AttributeConverter<TemplateType, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TemplateType attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public TemplateType convertToEntityAttribute(Integer dbData) {
        return TemplateType.valueOf(TemplateType.getEnumConstantNameByValue(dbData));
    }
}
