package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import ro.cs.tao.component.Aggregator;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.logger.Logger;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class AggregatorConverter implements AttributeConverter<Aggregator, String> {
    @Override
    public String convertToDatabaseColumn(Aggregator aggregator) {
        try {
            return aggregator != null ?
                    JsonMapper.instance().writeValueAsString(aggregator) : null;
        } catch (JsonProcessingException e) {
            Logger.getLogger(getClass()).warning(e.getMessage());
            return null;
        }
    }

    @Override
    public Aggregator convertToEntityAttribute(String s) {
        try {
            return s != null ?
                    JsonMapper.instance().readerFor(Aggregator.class).readValue(s) : null;
        } catch (IOException e) {
            Logger.getLogger(getClass()).warning(e.getMessage());
            return null;
        }
    }
}
