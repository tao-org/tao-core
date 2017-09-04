package ro.cs.tao.serialization;

import javax.xml.transform.stream.StreamSource;


/**
 * Created by kraftek on 2/28/2017.
 */
public interface Serializer<S, R> {

    S deserialize(StreamSource source) throws SerializationException;
    R serialize(S object) throws SerializationException;

}
