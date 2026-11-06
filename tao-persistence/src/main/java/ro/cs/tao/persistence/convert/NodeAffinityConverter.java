package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.component.NodeAffinity;

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

