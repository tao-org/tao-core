package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.TaoEnum;

import java.io.IOException;

public class TaoEnumDeserializer<T extends Enum<T> & TaoEnum> extends StdDeserializer<T> {
    private final Class<T> clazz;

    public TaoEnumDeserializer() {
        this(null);
    }

    public TaoEnumDeserializer(Class<T> vc) {
        super(vc);
        this.clazz = vc;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String value = node.textValue();
        return EnumUtils.getEnumConstantByName(clazz, value);
    }
}
