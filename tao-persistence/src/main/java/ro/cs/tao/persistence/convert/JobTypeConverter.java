package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.execution.model.JobType;

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

