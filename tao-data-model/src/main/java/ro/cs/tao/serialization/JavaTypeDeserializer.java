package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.datasource.param.JavaType;

import java.io.IOException;

public class JavaTypeDeserializer extends StdDeserializer<JavaType> {
    protected JavaTypeDeserializer() {
        super(JavaType.class);
    }

    @Override
    public JavaType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String value = node.textValue();
        return EnumUtils.getEnumConstantByFriendlyName(JavaType.class, value);
    }
}
