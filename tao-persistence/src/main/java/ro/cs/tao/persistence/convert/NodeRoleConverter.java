package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.topology.NodeRole;

public class NodeRoleConverter implements AttributeConverter<NodeRole, String> {

    @Override
    public String convertToDatabaseColumn(NodeRole value) {
        return value != null ? value.value() : null;
    }

    @Override
    public NodeRole convertToEntityAttribute(String value) {
        return value != null ? EnumUtils.getEnumConstantByValue(NodeRole.class, value) : null;
    }
}

