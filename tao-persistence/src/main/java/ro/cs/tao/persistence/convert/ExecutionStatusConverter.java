package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.execution.ExecutionStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for ExecutionStatus enum stored values
 *
 * @author oana
 *
 */
@Converter
public class ExecutionStatusConverter implements AttributeConverter<ExecutionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ExecutionStatus attribute) {

        return Integer.parseInt(attribute.toString());
    }

    @Override
    public ExecutionStatus convertToEntityAttribute(Integer dbData) {
        return ExecutionStatus.valueOf(ExecutionStatus.getEnumConstantNameByValue(dbData));
    }
}
