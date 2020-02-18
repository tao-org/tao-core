package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.topology.NodeRole;

import javax.persistence.AttributeConverter;

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
