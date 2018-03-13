package ro.cs.tao.persistence.convert;

import ro.cs.tao.execution.model.ExecutionStatus;

import javax.persistence.AttributeConverter;

/**
 * Converter for ExecutionStatus enum stored values
 *
 * @author oana
 *
 */
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
