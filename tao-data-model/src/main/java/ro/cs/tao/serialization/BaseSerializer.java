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
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.ConfigFeature;

import java.util.*;

/**
 * @author  Cosmin Cara
 */
public abstract class BaseSerializer<T> implements Serializer<T, String> {
    private Class<T> tClass;
    boolean formatOutput;
    boolean ignoreNullFields;
    private Set<ConfigFeature> enabledConfigFeatures;
    private Set<ConfigFeature> disabledConfigFeatures;
    private Set<JsonParser.Feature> enabledParserFeatures;
    private Set<JsonParser.Feature> disabledParserFeatures;

    BaseSerializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    protected abstract ObjectMapper createMapper();

    public void setFormatOutput(boolean value) { this.formatOutput = value; }

    public void setIgnoreNullFields() {
        this.ignoreNullFields = true;
    }

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

    public void enable(JsonParser.Feature feature) {
        if (this.enabledParserFeatures == null) {
            this.enabledParserFeatures = new HashSet<>();
        }
        this.enabledParserFeatures.add(feature);
        if (this.disabledParserFeatures != null) {
            this.disabledParserFeatures.remove(feature);
        }
    }

    public void disable(JsonParser.Feature feature) {
        if (this.disabledParserFeatures == null) {
            this.disabledParserFeatures = new HashSet<>();
        }
        this.disabledParserFeatures.add(feature);
        if (this.enabledParserFeatures != null) {
            this.enabledParserFeatures.remove(feature);
        }
    }

    @Override
    public T deserialize(String source) throws SerializationException {
        if (source == null || "null".equalsIgnoreCase(source)) {
            return null;
        }
        try {
            ObjectMapper mapper = createMapper();
            applyConfiguration(mapper);
            return mapper.readerFor(this.tClass).readValue(source);
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
            ObjectMapper mapper = createMapper();
            applyConfiguration(mapper);
            final JsonNode jsonNode = mapper.readTree(source);
            final List<T> results = new ArrayList<>();
            if (!jsonNode.isArray()) {
                jsonNode.fields().forEachRemaining(entry -> {
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
                final Iterator<JsonNode> elements = jsonNode.elements();
                elements.forEachRemaining(entry -> {
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
            ObjectMapper mapper = createMapper();
            applyConfiguration(mapper);
            return mapper.writerFor(this.tClass).writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
    @Override
    public String serialize(List<T> objects, String name) throws SerializationException {
        try {
            ObjectMapper mapper = createMapper();
            applyConfiguration(mapper);
            return mapper.enable(SerializationFeature.WRAP_ROOT_VALUE).writer().withRootName(name).writeValueAsString(objects);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    private void applyConfiguration(ObjectMapper mapper) {
        if (this.enabledConfigFeatures != null) {
            for (ConfigFeature feature : this.enabledConfigFeatures) {
                if (feature instanceof SerializationFeature) {
                    mapper.enable((SerializationFeature) feature);
                } else if (feature instanceof DeserializationFeature) {
                    mapper.enable((DeserializationFeature) feature);
                }
            }
        }
        if (this.disabledConfigFeatures != null) {
            for (ConfigFeature feature : this.disabledConfigFeatures) {
                if (feature instanceof SerializationFeature) {
                    mapper.disable((SerializationFeature) feature);
                } else if (feature instanceof DeserializationFeature) {
                    mapper.disable((DeserializationFeature) feature);
                }
            }
        }
        if (this.enabledParserFeatures != null) {
            for (JsonParser.Feature feature : this.enabledParserFeatures) {
                mapper.enable(feature);
            }
        }
        if (this.disabledParserFeatures != null) {
            for (JsonParser.Feature feature : this.disabledParserFeatures) {
                mapper.disable(feature);
            }
        }
        if (this.ignoreNullFields) {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        mapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    }
}
