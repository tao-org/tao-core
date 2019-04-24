package ro.cs.tao.persistence.convert;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.topology.NodeType;

import javax.persistence.AttributeConverter;

public class NodeTypeConverter implements AttributeConverter<NodeType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(NodeType nodeType) {
        return nodeType != null ? nodeType.value() : null;
    }

    @Override
    public NodeType convertToEntityAttribute(Integer dbData) {
        return dbData != null ? EnumUtils.getEnumConstantByValue(NodeType.class, dbData) : null;
    }
}
