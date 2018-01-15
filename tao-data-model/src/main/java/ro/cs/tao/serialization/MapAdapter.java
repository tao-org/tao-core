package ro.cs.tao.serialization;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class MapAdapter extends XmlAdapter<Map<String, String>, String> {
    @Override
    public String unmarshal(Map<String, String> v) throws Exception {
        if (v == null) return null;
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int count = v.size();
        int current = 0;
        for (Map.Entry<String, String> entry : v.entrySet()) {
            builder.append("\"").append(entry.getKey()).append("\"").append(":");
            builder.append("\"").append(entry.getValue()).append("\"");
            if (++current < count) {
                builder.append(",");
            }
        }
        builder.append("}");
        return builder.toString();
    }

    @Override
    public Map<String, String> marshal(String v) throws Exception {
        if (v == null) return null;
        Map<String, String> map = new HashMap<>();
        String[] entries = v.substring(1, v.length() - 1).split(",");
        String[] tokens;
        for (String entry : entries) {
            tokens = entry.split(":");
            map.put(tokens[0].replace("\"", ""),
                    tokens[1].replace("\"", ""));
        }
        return map;
    }
}
