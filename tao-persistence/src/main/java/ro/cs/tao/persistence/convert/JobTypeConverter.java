package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.execution.model.JobType;

import javax.persistence.AttributeConverter;

public class JobTypeConverter implements AttributeConverter<JobType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(JobType jobType) {
        return jobType != null ? jobType.value() : null;
    }

    @Override
    public JobType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? EnumUtils.getEnumConstantByValue(JobType.class, dbData) : null;
    }
}
