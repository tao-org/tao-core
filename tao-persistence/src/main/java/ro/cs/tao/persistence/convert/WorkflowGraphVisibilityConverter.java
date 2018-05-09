package ro.cs.tao.persistence.convert;

import ro.cs.tao.workflow.enums.Visibility;

import javax.persistence.AttributeConverter;

/**
 * Converter for ProcessingComponentVisibility enum stored values
 *
 * @author oana
 */
public class WorkflowGraphVisibilityConverter implements AttributeConverter<Visibility, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Visibility attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public Visibility convertToEntityAttribute(Integer dbData) {
        return dbData != null ? Visibility.valueOf(Visibility.getEnumConstantNameByValue(dbData)) : null;
    }
}
