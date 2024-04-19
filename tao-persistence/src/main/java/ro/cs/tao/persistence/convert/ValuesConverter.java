package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.configuration.Values;

import javax.persistence.AttributeConverter;

public class ValuesConverter implements AttributeConverter<Values, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Values values) {
        try {
            return values != null ? mapper.writerFor(Values.class).writeValueAsString(values) : null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Values convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }
        try {
            return mapper.readerFor(Values.class).readValue(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }
}
