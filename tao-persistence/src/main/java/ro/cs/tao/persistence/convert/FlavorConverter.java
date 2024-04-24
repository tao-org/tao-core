package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.subscription.FlavorSubscription;

import javax.persistence.AttributeConverter;
import java.util.HashMap;
import java.util.Map;

public class FlavorConverter implements AttributeConverter<Map<String, FlavorSubscription>, String> {
    private final Map<String, FlavorSubscription> template = new HashMap<>();
    @Override
    public String convertToDatabaseColumn(Map<String, FlavorSubscription> map) {
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
    public Map<String, FlavorSubscription> convertToEntityAttribute(String s) {
        Map<String, FlavorSubscription> value = null;
        if (s != null) {
            try {
                value = JsonMapper.instance().readerFor(template.getClass()).readValue(s);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return value;
    }
}
