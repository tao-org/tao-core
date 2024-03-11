package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.drmaa.Environment;

import javax.persistence.AttributeConverter;

public class JobEnvironmentConverter implements AttributeConverter<Environment, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Environment env) {
        return env != null ? env.value() : Environment.DEFAULT.value();
    }

    @Override
    public Environment convertToEntityAttribute(Integer dbData) {
        return dbData != null ? EnumUtils.getEnumConstantByValue(Environment.class, dbData) : null;
    }
}
