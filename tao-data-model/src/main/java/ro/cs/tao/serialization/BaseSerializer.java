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

import ro.cs.tao.component.constraints.*;
import ro.cs.tao.component.template.BasicTemplate;
import ro.cs.tao.component.template.Template;
import ro.cs.tao.component.validation.*;
import ro.cs.tao.component.validation.Validator;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.Polygon2D;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

/**
 * @author  Cosmin Cara
 */
public abstract class BaseSerializer<T> implements Serializer<T, String> {
    private static Class[] classes;
    protected boolean formatOutput;

    static {
        classes = new Class[] {
                ListWrapper.class,
                Polygon2D.class,
                Template.class, BasicTemplate.class,
                IOConstraint.class, CRSConstraint.class, DimensionConstraint.class, GeometryConstraint.class,
                SensorConstraint.class,
                Validator.class, NotEmptyValidator.class, NotNullValidator.class, TypeValidator.class, ValueSetValidator.class,
                QueryParameter.class
        };
    }

    Class<T> tClass;
    JAXBContext context;

    BaseSerializer(Class<T> tClass) throws SerializationException {
        this.tClass = tClass;
        try {
            final Class[] classesCopy = new Class[classes.length + 1];
            System.arraycopy(classes, 0, classesCopy, 0, classes.length);
            classesCopy[classes.length] = tClass;
            this.context = JAXBContext.newInstance(classesCopy);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    BaseSerializer(Class<T> tClass, Class...dependencies) throws SerializationException {
        this.tClass = tClass;
        try {
            final Class[] classesCopy = new Class[classes.length + (dependencies != null ? dependencies.length : 0) + 1];
            System.arraycopy(classes, 0, classesCopy, 0, classes.length);
            if (dependencies != null && dependencies.length > 0) {
                System.arraycopy(dependencies, 0, classesCopy, classes.length, dependencies.length);
            }
            classesCopy[classesCopy.length - 1] = tClass;
            this.context = JAXBContext.newInstance(classesCopy);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    BaseSerializer(Class<T> tClass, Map<String, Object> properties, Class...dependencies) throws SerializationException {
        this.tClass = tClass;
        try {
            final Class[] classesCopy = new Class[classes.length + 1];
            System.arraycopy(classes, 0, classesCopy, 0, classes.length);
            if (dependencies != null && dependencies.length > 0) {
                System.arraycopy(dependencies, 0, classesCopy, classes.length, dependencies.length);
            }
            classesCopy[classesCopy.length - 1] = tClass;
            if (properties != null) {
                this.context = JAXBContext.newInstance(classesCopy, properties);
            } else {
                this.context = JAXBContext.newInstance(classesCopy);
            }
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public void setFormatOutput(boolean value) { this.formatOutput = value; }

    public List<T> deserializeList(Class<T> clazz, StreamSource source) throws SerializationException {
        try {
            Unmarshaller unmarshaller = this.context.createUnmarshaller();
            ListWrapper<T> wrapper = (ListWrapper<T>) unmarshaller.unmarshal(source, ListWrapper.class).getValue();
            return wrapper.getItems();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public String serialize(List<T> objects, String name) throws SerializationException {
        try {
            Marshaller marshaller = this.context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, this.formatOutput);
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
    }
}
