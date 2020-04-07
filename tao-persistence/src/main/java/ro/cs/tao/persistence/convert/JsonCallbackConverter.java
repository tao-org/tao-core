package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ro.cs.tao.execution.model.ExternalTaskListenerDescriptor;

import javax.persistence.AttributeConverter;

public class JsonCallbackConverter implements AttributeConverter<ExternalTaskListenerDescriptor, String> {
    @Override
    public String convertToDatabaseColumn(ExternalTaskListenerDescriptor descriptor) {
        String dbValue = null;
        if (descriptor != null) {
            try {
                dbValue = new ObjectMapper().writerFor(ExternalTaskListenerDescriptor.class).writeValueAsString(descriptor);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        return dbValue;
    }

    @Override
    public ExternalTaskListenerDescriptor convertToEntityAttribute(String value) {
        ExternalTaskListenerDescriptor descriptor = null;
        try {
            descriptor = new ObjectMapper().readerFor(ExternalTaskListenerDescriptor.class).readValue(value);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return descriptor;
    }
}
