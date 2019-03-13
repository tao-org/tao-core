package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.component.Aggregator;

import javax.persistence.AttributeConverter;
import java.io.IOException;

public class AggregatorConverter implements AttributeConverter<Aggregator, String> {
    @Override
    public String convertToDatabaseColumn(Aggregator aggregator) {
        try {
            return aggregator != null ?
                    new ObjectMapper().writeValueAsString(aggregator) : null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Aggregator convertToEntityAttribute(String s) {
        try {
            return s != null ?
                    new ObjectMapper().readerFor(Aggregator.class).readValue(s) : null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
