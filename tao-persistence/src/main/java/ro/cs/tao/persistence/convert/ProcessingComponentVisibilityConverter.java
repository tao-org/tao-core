package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.enums.ProcessingComponentVisibility;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for ProcessingComponentVisibility enum stored values
 *
 * @author oana
 *
 */
public class ProcessingComponentVisibilityConverter implements AttributeConverter<ProcessingComponentVisibility, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ProcessingComponentVisibility attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public ProcessingComponentVisibility convertToEntityAttribute(Integer dbData) {
        return ProcessingComponentVisibility.valueOf(ProcessingComponentVisibility.getEnumConstantNameByValue(dbData));
    }
}
