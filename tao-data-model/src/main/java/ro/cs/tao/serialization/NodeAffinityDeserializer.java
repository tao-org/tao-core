package ro.cs.tao.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import ro.cs.tao.component.NodeAffinity;

import java.io.IOException;

public class NodeAffinityDeserializer extends StdDeserializer<NodeAffinity> {

    protected NodeAffinityDeserializer(){
        super(NodeAffinity.class);
    }

    @Override
    public NodeAffinity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        String text = node.get("value").asText();
        return NodeAffinity.of(text);
    }

}
