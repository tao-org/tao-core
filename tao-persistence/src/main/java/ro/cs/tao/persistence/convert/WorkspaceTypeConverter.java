package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.workspaces.WorkspaceType;

import javax.persistence.AttributeConverter;

public class WorkspaceTypeConverter implements AttributeConverter<WorkspaceType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(WorkspaceType fileRepositoryType) {
        return fileRepositoryType != null ? fileRepositoryType.value() : null;
    }

    @Override
    public WorkspaceType convertToEntityAttribute(Integer value) {
        return value != null ? EnumUtils.getEnumConstantByValue(WorkspaceType.class, value) : null;
    }
}
