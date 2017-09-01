package ro.cs.tao.serialization;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.util.Map;

/**
 * @author  kraftek
 * @date    2017-02-03
 */
abstract class BaseSerializer<T> implements Serializer<T, String> {
    Class<T> tClass;
    JAXBContext context;

    BaseSerializer(Class<T> tClass) throws SerializationException {
        this.tClass = tClass;
        try {
            this.context = JAXBContext.newInstance(this.tClass);
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }

    BaseSerializer(Class<T> tClass, Map<String, Object> properties) throws SerializationException {
        this.tClass = tClass;
        try {
            if (properties != null) {
                this.context = JAXBContext.newInstance(new Class[] { this.tClass }, properties);
            } else {
                this.context = JAXBContext.newInstance(this.tClass);
            }
        } catch (JAXBException e) {
            throw new SerializationException(e);
        }
    }
}
