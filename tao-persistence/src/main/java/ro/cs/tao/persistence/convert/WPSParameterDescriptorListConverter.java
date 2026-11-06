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

import jakarta.persistence.AttributeConverter;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ParameterExpansionRule;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.utils.StringUtilities;
import tools.jackson.core.*;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

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
        } catch (JacksonException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public List<ParameterDescriptor> convertToEntityAttribute(String s) {
        try {
            return StringUtilities.isNullOrEmpty(s) || "[]".equals(s)
                    ? null
                    : objectMapper().readerForListOf(ParameterDescriptor.class).readValue(s);
        } catch (JacksonException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    private ObjectMapper objectMapper() {
        final ObjectMapper mapper = new ObjectMapper();
        return mapper.rebuild().addModules(serializer, deserializer).build();
    }

    private static class Serializer extends StdSerializer<ParameterDescriptor> {

        protected Serializer(Class<ParameterDescriptor> t) {
            super(t);
        }

        @Override
        public void serialize(ParameterDescriptor descriptor, JsonGenerator jsonGenerator, SerializationContext serializerProvider) {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeStringProperty("id", descriptor.getId());
            jsonGenerator.writeStringProperty("name", descriptor.getName());
            jsonGenerator.writeStringProperty("type", descriptor.getType().name());
            jsonGenerator.writeStringProperty("dataType", JavaType.fromClass(descriptor.getDataType()).friendlyName());
            jsonGenerator.writeStringProperty("defaultValue", descriptor.getDefaultValue());
            jsonGenerator.writeStringProperty("description", descriptor.getDescription());
            jsonGenerator.writeStringProperty("label", descriptor.getLabel());
            jsonGenerator.writeStringProperty("unit", descriptor.getUnit());
            final String[] valueSet = descriptor.getValueSet();
            if (valueSet != null && valueSet.length > 0) {
                jsonGenerator.writeName("valueSet");
                jsonGenerator.writeArray(valueSet, 0, valueSet.length);
            } else {
                jsonGenerator.writeNullProperty("valueSet");
            }
            jsonGenerator.writeStringProperty("format", descriptor.getFormat());
            jsonGenerator.writePOJOProperty("expansionRule", descriptor.getExpansionRule());
            jsonGenerator.writeBooleanProperty("notNull", descriptor.isNotNull());
            jsonGenerator.writeEndObject();
        }
    }

    private static class Deserializer extends StdDeserializer<ParameterDescriptor> {
        protected Deserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public ParameterDescriptor deserialize(JsonParser parser, DeserializationContext deserializationContext) {
            if (parser.currentToken() != JsonToken.START_OBJECT) {
                throw new RuntimeException("Invalid start marker");
            }
            ParameterDescriptor descriptor = new ParameterDescriptor();
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                final String fieldName = parser.currentName();
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

