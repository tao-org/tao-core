package ro.cs.tao.persistence.convert;

import ro.cs.tao.component.NodeAffinity;

import javax.persistence.AttributeConverter;

public class NodeAffinityConverter implements AttributeConverter<NodeAffinity, String> {

    @Override
    public String convertToDatabaseColumn(NodeAffinity value) {
        return value != null ? value.getValue() : null;
    }

    @Override
    public NodeAffinity convertToEntityAttribute(String value) {
        return value != null ? NodeAffinity.of(value) : null;
    }
}
