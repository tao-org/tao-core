package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.subscription.ExternalFlavorSubscription;
import ro.cs.tao.utils.logger.Logger;

import javax.persistence.AttributeConverter;
import java.util.HashMap;
import java.util.Map;

public class ExternalFlavorConverter implements AttributeConverter<Map<String, ExternalFlavorSubscription>, String> {
    private final Map<String, ExternalFlavorSubscription> template = new HashMap<>();
    @Override
    public String convertToDatabaseColumn(Map<String, ExternalFlavorSubscription> map) {
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
    public Map<String, ExternalFlavorSubscription> convertToEntityAttribute(String s) {
        Map<String, ExternalFlavorSubscription> value = null;
        Map<String, ExternalFlavorSubscription> returnMap = new HashMap<>();
        if (s != null) {
            try {
                value = JsonMapper.instance().readerFor(template.getClass()).readValue(s);
                final ObjectMapper mapper = new ObjectMapper();
                for(Map.Entry<String, ExternalFlavorSubscription> entry : value.entrySet()){
                    ExternalFlavorSubscription externalFlavorSubscription = mapper.convertValue(entry.getValue(), ExternalFlavorSubscription.class);
                    returnMap.put(entry.getKey(), externalFlavorSubscription);
                }
            } catch (JsonProcessingException e) {
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        }
        return returnMap;
    }
}
