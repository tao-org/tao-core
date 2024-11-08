package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.JsonProcessingException;
import ro.cs.tao.execution.callback.EndpointDescriptor;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.utils.logger.Logger;

import javax.persistence.AttributeConverter;

public class JsonCallbackConverter implements AttributeConverter<EndpointDescriptor, String> {
    @Override
    public String convertToDatabaseColumn(EndpointDescriptor descriptor) {
        String dbValue = null;
        if (descriptor != null) {
            try {
                dbValue = JsonMapper.instance().writerFor(EndpointDescriptor.class).writeValueAsString(descriptor);
            } catch (JsonProcessingException e) {
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        }
        return dbValue;
    }

    @Override
    public EndpointDescriptor convertToEntityAttribute(String value) {
        EndpointDescriptor descriptor = null;
        if (value != null) {
            try {
                descriptor = JsonMapper.instance().readerFor(EndpointDescriptor.class).readValue(value);
            } catch (JsonProcessingException e) {
                Logger.getLogger(getClass()).warning(e.getMessage());
            }
        }
        return descriptor;
    }
}
