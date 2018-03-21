package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for ProcessingComponentVisibility enum stored values
 *
 * @author oana
 */
public class ProcessingComponentVisibilityConverter implements AttributeConverter<ProcessingComponentVisibility, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessingComponentVisibility attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public ProcessingComponentVisibility convertToEntityAttribute(Integer dbData) {
        return dbData != null ? ProcessingComponentVisibility.valueOf(ProcessingComponentVisibility.getEnumConstantNameByValue(dbData)) : null;
    }
}
