package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.TaoEnum;

import java.io.IOException;

/**
 * Custom deserializer for enums that implement the {@link TaoEnum} interface.
 * This deserializer is designed to work with enums that extend {@link Enum}
 * and provide additional functionality defined in the {@link TaoEnum} interface,
 * such as having a friendly name or other custom properties.
 *
 * @param <T> The type of the enum being deserialized, which must extend {@link Enum}
 *            and implement the {@link TaoEnum} interface.
 */
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
