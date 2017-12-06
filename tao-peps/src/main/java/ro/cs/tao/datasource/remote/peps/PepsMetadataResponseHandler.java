package ro.cs.tao.datasource.remote.peps;

import ro.cs.tao.datasource.remote.result.filters.AttributeFilter;
import ro.cs.tao.datasource.remote.result.json.JSonResponseHandler;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class PepsMetadataResponseHandler implements JSonResponseHandler<Boolean> {
    @Override
    public List<Boolean> readValues(String content, AttributeFilter... filters) throws IOException {
        JsonReader reader = Json.createReader(new StringReader(content));
        JsonObject rootObject = reader.readObject();
        JsonObject propObject = rootObject.getJsonObject("properties");
        JsonObject storageObject = propObject.getJsonObject("storage");
        return new ArrayList<Boolean>() {{
            add(!"tape".equalsIgnoreCase(storageObject.getJsonString("mode").getString()));
        }};
    }
}
