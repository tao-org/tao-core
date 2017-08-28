package ro.cs.tao.eodata.serialization;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringWriter;

/**
 * @author kraftek
 * @date 3/2/2017
 */
class JsonSerializer<T> extends BaseSerializer<T> {

    JsonSerializer(Class<T> tClass) throws SerializationException {
        super(tClass);
    }

    public T deserialize(StreamSource source) throws SerializationException {
        try {
            Unmarshaller unmarshaller = this.context.createUnmarshaller();
            unmarshaller.setProperty("eclipselink.media-type", "application/json");
            unmarshaller.setProperty("eclipselink.json.include-root", false);
            JAXBElement<T> result = unmarshaller.unmarshal(source, this.tClass);
            return result.getValue();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    public String serialize(T object) throws SerializationException {
        try {
            Marshaller marshaller = this.context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty("eclipselink.media-type", "application/json");
            marshaller.setProperty("eclipselink.json.include-root", false);
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
}
