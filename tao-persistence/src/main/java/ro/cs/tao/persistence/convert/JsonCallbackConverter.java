package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.execution.model.EndpointDescriptor;

import javax.persistence.AttributeConverter;

public class JsonCallbackConverter implements AttributeConverter<EndpointDescriptor, String> {
    @Override
    public String convertToDatabaseColumn(EndpointDescriptor descriptor) {
        String dbValue = null;
        if (descriptor != null) {
            try {
                dbValue = new ObjectMapper().writerFor(EndpointDescriptor.class).writeValueAsString(descriptor);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return dbValue;
    }

    @Override
    public EndpointDescriptor convertToEntityAttribute(String value) {
        EndpointDescriptor descriptor = null;
        try {
            descriptor = new ObjectMapper().readerFor(EndpointDescriptor.class).readValue(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return descriptor;
    }
}
