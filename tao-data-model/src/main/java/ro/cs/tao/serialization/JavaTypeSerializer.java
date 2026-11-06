package ro.cs.tao.serialization;

import ro.cs.tao.datasource.param.JavaType;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

/**
 * Serializer for Java class types that extends {@link Number}.
 * Converts the class type to a friendly name and writes it as a string in the JSON output.
 *
 * @author  Cosmin Cara
 */
public class JavaTypeSerializer extends ValueSerializer<Class<? extends Number>> {

    @Override
    public void serialize(Class<? extends Number> value, JsonGenerator gen, SerializationContext serializers) {
        gen.writeString(JavaType.fromClass(value).friendlyName());
    }
}
