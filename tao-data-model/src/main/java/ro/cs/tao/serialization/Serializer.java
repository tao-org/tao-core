package ro.cs.tao.serialization;

import javax.xml.transform.stream.StreamSource;
import java.util.List;


/**
 * Created by kraftek on 2/28/2017.
 */
public interface Serializer<S, R> {

    void setFormatOutput(boolean value);
    S deserialize(StreamSource source) throws SerializationException;
    List<S> deserializeList(Class<S> sClass, StreamSource source) throws SerializationException;
    R serialize(S object) throws SerializationException;
    R serialize(List<S> objects, String name) throws SerializationException;

}
