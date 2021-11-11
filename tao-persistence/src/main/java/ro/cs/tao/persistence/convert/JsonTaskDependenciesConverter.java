package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import ro.cs.tao.serialization.JsonMapper;

import javax.persistence.AttributeConverter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonTaskDependenciesConverter implements AttributeConverter<Map<String, List<String>>, String> {
    private final Map<String, ArrayList<String>> template = new HashMap<>();
    @Override
    public String convertToDatabaseColumn(Map<String, List<String>> map) {
        String dbValue = null;
        if (map != null) {
            try {
                dbValue = JsonMapper.instance().writerFor(template.getClass()).writeValueAsString(map);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return dbValue;
    }

    @Override
    public Map<String, List<String>> convertToEntityAttribute(String value) {
        Map<String, List<String>> descriptor = null;
        if (value != null) {
            try {
                descriptor = JsonMapper.instance().readerFor(template.getClass()).readValue(value);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return descriptor;
    }
}