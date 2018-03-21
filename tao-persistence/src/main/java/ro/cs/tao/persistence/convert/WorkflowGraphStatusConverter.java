package ro.cs.tao.persistence.convert;

import ro.cs.tao.workflow.Status;

import javax.persistence.AttributeConverter;

/**
 * Converter for ExecutionStatus enum stored values
 *
 * @author oana
 */
public class WorkflowGraphStatusConverter implements AttributeConverter<Status, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Status attribute) {
        return attribute != null ? Integer.parseInt(attribute.toString()) : null;
    }

    @Override
    public Status convertToEntityAttribute(Integer dbData) {
        return dbData != null ? Status.valueOf(Status.getEnumConstantNameByValue(dbData)) : null;
    }
}
