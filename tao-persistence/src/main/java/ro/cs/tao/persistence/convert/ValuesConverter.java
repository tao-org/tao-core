package ro.cs.tao.persistence.convert;

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.configuration.Values;
import ro.cs.tao.utils.logger.Logger;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

public class ValuesConverter implements AttributeConverter<Values, String> {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Values values) {
        try {
            return values != null ? mapper.writerFor(Values.class).writeValueAsString(values) : null;
        } catch (JacksonException e) {
            Logger.getLogger(getClass()).warning(e.getMessage());
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
        } catch (JacksonException e) {
            Logger.getLogger(getClass()).warning(e.getMessage());
            return null;
        }
    }
}


