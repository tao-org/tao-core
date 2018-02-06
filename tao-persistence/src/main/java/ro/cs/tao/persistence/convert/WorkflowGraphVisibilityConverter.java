package ro.cs.tao.persistence.convert;

import ro.cs.tao.workflow.Visibility;

import javax.persistence.AttributeConverter;

/**
 * Converter for ProcessingComponentVisibility enum stored values
 *
 * @author oana
 *
 */
public class WorkflowGraphVisibilityConverter implements AttributeConverter<Visibility, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Visibility attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public Visibility convertToEntityAttribute(Integer dbData) {
        return Visibility.valueOf(Visibility.getEnumConstantNameByValue(dbData));
    }
}
