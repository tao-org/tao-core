package ro.cs.tao.serialization;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.datasource.param.JavaType;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

/**
 * A custom deserializer for the {@code JavaType} enumeration, extending the standard
 * deserialization functionality provided by {@code StdDeserializer}.
 *
 * This deserializer handles the conversion of JSON input into a {@code JavaType} instance,
 * based on its textual representation. It attempts to match the input string value
 * to an enumeration constant by either a friendly name or class representation.
 * If the matching fails, it throws an {@code IOException}.
 * @author  Cosmin Cara
 */
public class JavaTypeDeserializer extends StdDeserializer<JavaType> {
    public JavaTypeDeserializer() {
        super(JavaType.class);
    }

    @Override
    public JavaType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        JsonNode node = jsonParser.readValueAsTree();
        String value = node.textValue();
        JavaType javaType = EnumUtils.getEnumConstantByFriendlyName(JavaType.class, value);
        if (javaType == null) {
            try {
            Class<?> dataType = Class.forName(value);
            javaType = EnumUtils.getEnumConstantByValue(JavaType.class, dataType);
            } catch(ClassNotFoundException e){
                throw new RuntimeException(e);
            }
        }
        return javaType;
    }
}
