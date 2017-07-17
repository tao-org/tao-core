package ro.cs.tao.eodata.serialization;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * @author  kraftek
 * @date    2017-02-03
 */
abstract class BaseSerializer<T> implements MetadataSerializer<T, String> {
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
}
