package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import ro.cs.tao.datasource.param.JavaType;

import java.io.IOException;

public class JavaTypeSerializer extends JsonSerializer<Class<? extends Number>> {

    @Override
    public void serialize(Class<? extends Number> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(JavaType.fromClass(value).friendlyName());
    }
}
