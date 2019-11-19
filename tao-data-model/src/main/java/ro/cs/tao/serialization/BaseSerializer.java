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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.transform.stream.StreamSource;
import java.util.List;

/**
 * @author  Cosmin Cara
 */
public abstract class BaseSerializer<T> implements Serializer<T, String> {
    protected boolean formatOutput;

    Class<T> tClass;

    BaseSerializer(Class<T> tClass) {
        this.tClass = tClass;
    }

    protected abstract ObjectMapper createMapper();

    public void setFormatOutput(boolean value) { this.formatOutput = value; }

    @Override
    public T deserialize(StreamSource source) throws SerializationException {
        throw new NoSuchMethodError("Not implemented");
    }

    @Override
    public List<T> deserializeList(Class<T> tClass, StreamSource source) throws SerializationException {
        throw new NoSuchMethodError("Not implemented");
    }

    public T deserialize(String source) throws SerializationException {
        try {
            ObjectMapper mapper = createMapper();
            return mapper.readerFor(this.tClass).readValue(source);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public List<T> deserialize(Class<T> clazz, String source) throws SerializationException {
        try {
            ObjectMapper mapper = createMapper();
            TypeReference<List<T>> typeRef = new TypeReference<List<T>>() {};
            return mapper.readerFor(typeRef).readValue(source);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public String serialize(T object) throws SerializationException {
        try {
            ObjectMapper mapper = createMapper();
            return mapper.writerFor(this.tClass).writeValueAsString(object);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }

    public String serialize(List<T> objects, String name) throws SerializationException {
        try {
            ObjectMapper mapper = createMapper();
            TypeReference<List<T>> typeRef = new TypeReference<List<T>>() {};
            return mapper.writerFor(typeRef).writeValueAsString(objects);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
