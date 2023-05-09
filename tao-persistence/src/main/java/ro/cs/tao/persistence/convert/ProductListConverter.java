package ro.cs.tao.persistence.convert;

import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;

import javax.persistence.AttributeConverter;
import java.util.List;

public class ProductListConverter implements AttributeConverter<List<EOProduct>, String> {
    private static final BaseSerializer<EOProduct> serializer;

    static {
        try {
            serializer = SerializerFactory.create(EOProduct.class, MediaType.JSON);
        } catch (SerializationException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public String convertToDatabaseColumn(List<EOProduct> products) {
        try {
            return serializer.serialize(products, "products");
        } catch (SerializationException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public List<EOProduct> convertToEntityAttribute(String s) {
        try {
            return serializer.deserialize(EOProduct.class, s);
        } catch (SerializationException e) {
            throw new RuntimeException(e.getCause());
        }
    }
}