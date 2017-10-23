package ro.cs.tao.serialization;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;
import java.util.List;

/**
 * @author kraftek
 * @date 3/2/2017
 */
class XmlSerializer<T> extends BaseSerializer<T> {

    XmlSerializer(Class<T> tClass) throws SerializationException {
        super(tClass);
    }

    XmlSerializer(Class<T> tClass, Class[] dependencies) throws SerializationException {
        super(tClass, dependencies);
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

    public List<T> deserializeList(Class<T> clazz, StreamSource source) throws SerializationException {
        try {
            Unmarshaller unmarshaller = this.context.createUnmarshaller();
            ListWrapper<T> wrapper = (ListWrapper<T>) unmarshaller.unmarshal(source, ListWrapper.class).getValue();
            return wrapper.getItems();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public String serialize(T object) throws SerializationException {
        try {
            Marshaller marshaller = this.context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public String serialize(List<T> objects, String name) throws SerializationException {
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
    }
}
