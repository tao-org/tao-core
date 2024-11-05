package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.subscription.FlavorSubscription;
import ro.cs.tao.utils.logger.Logger;

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
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        }
        return dbValue;
    }

    @Override
    public Map<String, FlavorSubscription> convertToEntityAttribute(String s) {
        Map<String, FlavorSubscription> value = null;
        Map<String, FlavorSubscription> returnMap = new HashMap<>();
        if (s != null) {
            try {
                value = JsonMapper.instance().readerFor(template.getClass()).readValue(s);
                final ObjectMapper mapper = new ObjectMapper();
                for(Map.Entry<String, FlavorSubscription> entry : value.entrySet()){
                    FlavorSubscription flavorSubscription = mapper.convertValue(entry.getValue(), FlavorSubscription.class);
                    returnMap.put(entry.getKey(), flavorSubscription);
                }
            } catch (JsonProcessingException e) {
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        }
        return returnMap;
    }
}
