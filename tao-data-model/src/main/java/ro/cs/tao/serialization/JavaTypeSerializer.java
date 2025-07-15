package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ro.cs.tao.datasource.param.JavaType;

import java.io.IOException;

/**
 * Serializer for Java class types that extends {@link Number}.
 * Converts the class type to a friendly name and writes it as a string in the JSON output.
 *
 * @author  Cosmin Cara
 */
public class JavaTypeSerializer extends JsonSerializer<Class<? extends Number>> {

    @Override
    public void serialize(Class<? extends Number> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(JavaType.fromClass(value).friendlyName());
    }
}
