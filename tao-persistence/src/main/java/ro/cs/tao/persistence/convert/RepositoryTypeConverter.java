package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.workspaces.RepositoryType;

import javax.persistence.AttributeConverter;

public class RepositoryTypeConverter implements AttributeConverter<RepositoryType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(RepositoryType fileRepositoryType) {
        return fileRepositoryType != null ? fileRepositoryType.value() : null;
    }

    @Override
    public RepositoryType convertToEntityAttribute(Integer value) {
        return value != null ? EnumUtils.getEnumConstantByValue(RepositoryType.class, value) : null;
    }
}
