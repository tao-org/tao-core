package ro.cs.tao.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import ro.cs.tao.datasource.param.DataSourceParameter;

import java.util.Map;

public class ObjectMapperProvider {

    public static final ObjectMapper JSON_OBJECT_MAPPER;
    public static final ObjectWriter JSON_DATA_SOURCE_PARAMETERS_WRITER;
    public static final ObjectReader JSON_DATA_SOURCE_PARAMETERS_READER;

    static {
        JSON_OBJECT_MAPPER = new ObjectMapper()
                .findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        JSON_DATA_SOURCE_PARAMETERS_READER = JSON_OBJECT_MAPPER.readerFor(new TypeReference<Map<String, Map<String, DataSourceParameter>>>() {
        });
        JSON_DATA_SOURCE_PARAMETERS_WRITER = JSON_OBJECT_MAPPER.writerFor(new TypeReference<Map<String, Map<String, DataSourceParameter>>>() {
        });

    }

    private ObjectMapperProvider() {

    }
}

