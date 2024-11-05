/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.persistence.convert;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterExpansionRule;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.utils.StringUtilities;

import javax.persistence.AttributeConverter;
import java.io.IOException;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class WPSParameterDescriptorListConverter implements AttributeConverter<List<ParameterDescriptor>, String> {

    private static final SimpleModule serializer;
    private static final SimpleModule deserializer;

    static {
        final Version version = new Version(1, 3, 1, null, null, null);
        serializer = new SimpleModule("Deserializer", version);
        serializer.addDeserializer(ParameterDescriptor.class, new Deserializer(ParameterDescriptor.class));
        deserializer = new SimpleModule("Serializer", version);
        deserializer.addSerializer(ParameterDescriptor.class, new Serializer(ParameterDescriptor.class));
    }

    @Override
    public String convertToDatabaseColumn(List<ParameterDescriptor> parameters) {
        try {
            return parameters != null && !parameters.isEmpty()
                    ? objectMapper().writerFor(parameters.getClass()).writeValueAsString(parameters)
                    : null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public List<ParameterDescriptor> convertToEntityAttribute(String s) {
        try {
            return StringUtilities.isNullOrEmpty(s) || "[]".equals(s)
                    ? null
                    : objectMapper().readerForListOf(ParameterDescriptor.class).readValue(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(serializer, deserializer);
        return mapper;
    }

    private static class Serializer extends StdSerializer<ParameterDescriptor> {

        protected Serializer(Class<ParameterDescriptor> t) {
            super(t);
        }

        @Override
        public void serialize(ParameterDescriptor descriptor, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringField("id", descriptor.getId());
            jsonGenerator.writeStringField("name", descriptor.getName());
            jsonGenerator.writeStringField("type", descriptor.getType().name());
            jsonGenerator.writeStringField("dataType", JavaType.fromClass(descriptor.getDataType()).friendlyName());
            jsonGenerator.writeStringField("defaultValue", descriptor.getDefaultValue());
            jsonGenerator.writeStringField("description", descriptor.getDescription());
            jsonGenerator.writeStringField("label", descriptor.getLabel());
            jsonGenerator.writeStringField("unit", descriptor.getUnit());
            final String[] valueSet = descriptor.getValueSet();
            if (valueSet != null && valueSet.length > 0) {
                jsonGenerator.writeFieldName("valueSet");
                jsonGenerator.writeArray(valueSet, 0, valueSet.length);
            } else {
                jsonGenerator.writeNullField("valueSet");
            }
            jsonGenerator.writeStringField("format", descriptor.getFormat());
            jsonGenerator.writeObjectField("expansionRule", descriptor.getExpansionRule());
            jsonGenerator.writeBooleanField("notNull", descriptor.isNotNull());
            jsonGenerator.writeEndObject();
        }
    }

    private static class Deserializer extends StdDeserializer<ParameterDescriptor> {
        protected Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ParameterDescriptor deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
            if (parser.getCurrentToken() != JsonToken.START_OBJECT) {
                throw new IOException("Invalid start marker");
            }
            ParameterDescriptor descriptor = new ParameterDescriptor();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                final String fieldName = parser.getCurrentName();
                parser.nextToken();
                switch (fieldName) {
                    case "id":
                        descriptor.setId(parser.getValueAsString());
                        break;
                    case "name":
                        descriptor.setName(parser.getValueAsString());
                        break;
                    case "type":
                        descriptor.setType(ParameterType.valueOf(parser.getValueAsString()));
                        break;
                    case "dataType":
                        descriptor.setDataType(JavaType.fromFriendlyName(parser.getValueAsString()).value());
                        break;
                    case "defaultValue":
                        descriptor.setDefaultValue(parser.getValueAsString());
                        break;
                    case "description":
                        descriptor.setDescription(parser.getValueAsString());
                        break;
                    case "label":
                        descriptor.setLabel(parser.getValueAsString());
                        break;
                    case "unit":
                        descriptor.setUnit(parser.getValueAsString());
                        break;
                    case "valueSet":
                        break;
                    case "format":
                        descriptor.setFormat(parser.getValueAsString());
                        break;
                    case "expansionRule":
                        descriptor.setExpansionRule(parser.readValueAs(ParameterExpansionRule.class));
                        break;
                    case "notNull":
                        descriptor.setNotNull(parser.getBooleanValue());
                        break;
                }
            }
            return descriptor;
        }
    }
}
