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

import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
class JsonSerializer<T> extends BaseSerializer<T> {
    private final static Map<String, Object> properties;

    static {
        System.setProperty(JAXBContext.class.getName(), "org.eclipse.persistence.jaxb.JAXBContextFactory");
        properties = new HashMap<String, Object>() {{
            put("eclipselink.media-type", "application/json");
            put("eclipselink.json.include-root", true);
        }};
    }

    JsonSerializer(Class<T> tClass) throws SerializationException {
        super(tClass, properties);
    }

    JsonSerializer(Class<T> tClass, Class[] dependencies) throws SerializationException {
        super(tClass, properties, dependencies);
    }

    public T deserialize(StreamSource source) throws SerializationException {
        try {
            Unmarshaller unmarshaller = this.context.createUnmarshaller();
            JAXBElement<T> result = unmarshaller.unmarshal(source, this.tClass);
            return result.getValue();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    /*public List<T> deserializeList(Class<T> clazz, StreamSource source) throws SerializationException {
        try {
            Unmarshaller unmarshaller = this.context.createUnmarshaller();
            ListWrapper<T> wrapper = (ListWrapper<T>) unmarshaller.unmarshal(source, ListWrapper.class).getValue();
            return wrapper.getItems();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }*/

    public String serialize(T object) throws SerializationException {
        try {
            Marshaller marshaller = this.context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, this.formatOutput);
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    /*public String serialize(List<T> objects, String name) throws SerializationException {
        try {
            Marshaller marshaller = this.context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            QName qName = new QName(name);
            ListWrapper<T> wrapper = new ListWrapper<>(objects);
            JAXBElement<ListWrapper> rootElement = new JAXBElement<>(qName, ListWrapper.class, wrapper);
            StringWriter writer = new StringWriter();
            marshaller.marshal(rootElement, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }*/
}
