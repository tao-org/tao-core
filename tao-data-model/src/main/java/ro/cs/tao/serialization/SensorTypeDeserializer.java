package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.eodata.enums.SensorType;

import java.io.IOException;

public class SensorTypeDeserializer extends StdDeserializer<SensorType> {

    public SensorTypeDeserializer() {
        super(SensorType.class);
    }

    @Override
    public SensorType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String value = node.textValue();
        return EnumUtils.getEnumConstantByName(SensorType.class, value);
    }
}
