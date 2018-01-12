package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.Attribute;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;

import javax.persistence.AttributeConverter;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class MapConverter implements AttributeConverter<Map<String, String>, String> {
    private static BaseSerializer<Attribute> serializer;

    static {
        try {
            serializer = SerializerFactory.create(Attribute.class, MediaType.JSON);
        } catch (SerializationException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public String convertToDatabaseColumn(Map<String, String> data) {
        try {
            List<Attribute> attributes = data.entrySet().stream().map(e -> {
                Attribute attribute = new Attribute();
                attribute.setName(e.getKey());
                attribute.setValue(e.getValue());
                return attribute;
            }).collect(Collectors.toList());
            return serializer.serialize(attributes, "data");
        } catch (SerializationException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Map<String, String> convertToEntityAttribute(String s) {
        try {
            List<Attribute> attributes = serializer.deserializeList(Attribute.class, new StreamSource(new StringReader(s)));
            return attributes.stream().collect(Collectors.toMap(Attribute::getName, Attribute::getValue));
        } catch (SerializationException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}