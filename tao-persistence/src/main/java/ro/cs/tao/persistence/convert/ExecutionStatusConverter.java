package ro.cs.tao.persistence.convert;

import ro.cs.tao.execution.model.ExecutionStatus;

import javax.persistence.AttributeConverter;

/**
 * Converter for ExecutionStatus enum stored values
 *
 * @author oana
 */
public class ExecutionStatusConverter implements AttributeConverter<ExecutionStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ExecutionStatus attribute) {

        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public ExecutionStatus convertToEntityAttribute(Integer dbData) {
        return dbData != null ? ExecutionStatus.valueOf(ExecutionStatus.getEnumConstantNameByValue(dbData)) : null;
    }
}
