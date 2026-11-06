package ro.cs.tao.serialization;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.eodata.enums.SensorType;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class SensorTypeDeserializer extends StdDeserializer<SensorType> {

    public SensorTypeDeserializer() {
        super(SensorType.class);
    }

    @Override
    public SensorType deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) {
        JsonNode node = jsonParser.readValueAsTree();
        String value = node.textValue();
        return EnumUtils.getEnumConstantByName(SensorType.class, value);
    }
}
