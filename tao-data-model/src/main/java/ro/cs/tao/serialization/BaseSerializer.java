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
package ro.cs.tao.serialization;

import com.fasterxml.jackson.annotation.JsonInclude;
import ro.cs.tao.utils.Tuple;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.databind.*;
import tools.jackson.databind.cfg.ConfigFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.introspect.AnnotationIntrospectorPair;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;
import tools.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationIntrospector;

import java.io.InputStream;
import java.util.*;

/**
 * Abstract base class for implementing custom serializers. The class provides
 * core methods and functionality for serializing and deserializing objects of
 * type {@code T} to and from a string-based format, such as JSON or XML.
 * Subclasses must implement the {@link #createMapper()} method to provide
 * an instance of {@link ObjectMapper} specialized for their specific format.
 *
 * @param <T> The type of the objects to be serialized and deserialized.
 *
 * @author  Cosmin Cara
 */
public abstract class BaseSerializer<T> implements Serializer<T, String> {
    private Class<T> tClass;
    boolean formatOutput;
    boolean ignoreNullFields;
    boolean overrideJAXBPrecedence;
    private Set<ConfigFeature> enabledConfigFeatures;
    private Set<ConfigFeature> disabledConfigFeatures;
    private Set<StreamReadFeature> enabledParserFeatures;
    private Set<StreamReadFeature> disabledParserFeatures;
    private Set<StdSerializer<?>> customSerializers;
    private Map<Class, StdDeserializer> customDeserializers;
    private Tuple<Class, Class> mixIn;

    BaseSerializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    protected abstract ObjectMapper createMapper();

    public void setFormatOutput(boolean value) { this.formatOutput = value; }

    public void setIgnoreNullFields() {
        this.ignoreNullFields = true;
    }

    public void setOverrideJAXBPrecedence() { this.overrideJAXBPrecedence = true; }

    public void enable(ConfigFeature feature) {
        if (this.enabledConfigFeatures == null) {
            this.enabledConfigFeatures = new HashSet<>();
        }
        this.enabledConfigFeatures.add(feature);
        if (this.disabledConfigFeatures != null) {
            this.disabledConfigFeatures.remove(feature);
        }
    }

    public void disable(ConfigFeature feature) {
        if (this.disabledConfigFeatures == null) {
            this.disabledConfigFeatures = new HashSet<>();
        }
        this.disabledConfigFeatures.add(feature);
        if (this.enabledConfigFeatures != null) {
            this.enabledConfigFeatures.remove(feature);
        }
    }

    public void enable(StreamReadFeature feature) {
        if (this.enabledParserFeatures == null) {
            this.enabledParserFeatures = new HashSet<>();
        }
        this.enabledParserFeatures.add(feature);
        if (this.disabledParserFeatures != null) {
            this.disabledParserFeatures.remove(feature);
        }
    }

    public void disable(StreamReadFeature feature) {
        if (this.disabledParserFeatures == null) {
            this.disabledParserFeatures = new HashSet<>();
        }
        this.disabledParserFeatures.add(feature);
        if (this.enabledParserFeatures != null) {
            this.enabledParserFeatures.remove(feature);
        }
    }

    public void addSerializer(StdSerializer<?> serializer) {
        if (this.customSerializers == null) {
            this.customSerializers = new HashSet<>();
        }
        this.customSerializers.add(serializer);
    }

    public void addDeserializer(Class clazz, StdDeserializer deserializer) {
        if (this.customDeserializers == null) {
            this.customDeserializers = new HashMap<>();
        }
        this.customDeserializers.put(clazz, deserializer);
    }

    public void addMixIn(Class forClass, Class mixInClass) {
        this.mixIn = new Tuple<>(forClass, mixInClass);
    }

    @Override
    public T deserialize(String source) throws SerializationException {
        if (source == null || "null".equalsIgnoreCase(source)) {
            return null;
        }
        try {
            ObjectMapper mapper = applyConfiguration(createMapper());
            return mapper.readerFor(this.tClass).readValue(source);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public T deserialize(InputStream stream) throws SerializationException {
        if (stream == null) {
            return null;
        }
        try {
            ObjectMapper mapper = applyConfiguration(createMapper());
            return mapper.readerFor(this.tClass).readValue(stream);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public List<T> deserialize(Class<T> clazz, String source) throws SerializationException {
        if (source == null || "null".equalsIgnoreCase(source)) {
            return null;
        }
        try {
            ObjectMapper mapper = applyConfiguration(createMapper());
            final JsonNode jsonNode = mapper.readTree(source);
            final List<T> results = new ArrayList<>();
            if (!jsonNode.isArray()) {
                jsonNode.properties().forEach(entry -> {
                    final JsonNode node = entry.getValue();
                    if (node.isArray()) {
                        final int size = node.size();
                        for (int i = 0; i < size; i++) {
                            results.add(mapper.convertValue(node.get(i), clazz));
                        }
                    }/* else {
                    results.add(mapper.convertValue(node, clazz));
                }*/
                });
            } else {
                jsonNode.values().forEach(entry -> {
                    results.add(mapper.convertValue(entry, clazz));
                });
            }
            return results;
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
    @Override
    public String serialize(T object) throws SerializationException {
        try {
            ObjectMapper mapper = applyConfiguration(createMapper());
            return mapper.writerFor(this.tClass).writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
    @Override
    public String serialize(List<T> objects, String name) throws SerializationException {
        try {
            ObjectMapper mapper = applyConfiguration(createMapper());
            return mapper.writer(SerializationFeature.WRAP_ROOT_VALUE).withRootName(name).writeValueAsString(objects);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    private ObjectMapper applyConfiguration(ObjectMapper mapper) {
        MapperBuilder<?, ?> builder = mapper.rebuild();
        if (this.enabledConfigFeatures != null) {
            for (ConfigFeature feature : this.enabledConfigFeatures) {
                if (feature instanceof SerializationFeature) {
                    builder.enable((SerializationFeature) feature);
                } else if (feature instanceof DeserializationFeature) {
                    builder.enable((DeserializationFeature) feature);
                }
            }
        }
        if (this.disabledConfigFeatures != null) {
            for (ConfigFeature feature : this.disabledConfigFeatures) {
                if (feature instanceof SerializationFeature) {
                    builder.disable((SerializationFeature) feature);
                } else if (feature instanceof DeserializationFeature) {
                    builder.disable((DeserializationFeature) feature);
                }
            }
        }
        if (this.enabledParserFeatures != null) {
            for (StreamReadFeature feature : this.enabledParserFeatures) {
                builder.enable(feature);
            }
        }
        if (this.disabledParserFeatures != null) {
            for (StreamReadFeature feature : this.disabledParserFeatures) {
                builder.disable(feature);
            }
        }
        if (this.ignoreNullFields) {
            builder.changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL));
        }
        if (this.overrideJAXBPrecedence) {
            AnnotationIntrospector primary = new JacksonAnnotationIntrospector();
            AnnotationIntrospector secondary = new JakartaXmlBindAnnotationIntrospector();
            builder.annotationIntrospector(new AnnotationIntrospectorPair(primary, secondary));
        }
        builder.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        SimpleModule module = null;
        if (this.customSerializers != null){
            module = new SimpleModule();
            for (StdSerializer<?> serializer : this.customSerializers) {
                module.addSerializer(serializer);
            }
        }
        if (this.customDeserializers != null) {
            if (module == null) {
                module = new SimpleModule();
            }
            for (Map.Entry<Class, StdDeserializer> entry : this.customDeserializers.entrySet()) {
                module.addDeserializer(entry.getKey(), entry.getValue());
            }
        }
        if (this.mixIn != null) {
            builder.addMixIn(this.mixIn.getKeyOne(), this.mixIn.getKeyTwo());
        }
        if (module != null) {
            builder.addModule(module);
        }
        return builder.build();
    }
}
