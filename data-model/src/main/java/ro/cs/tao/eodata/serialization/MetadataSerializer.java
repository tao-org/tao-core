package ro.cs.tao.eodata.serialization;

import javax.xml.transform.stream.StreamSource;


/**
 * Created by kraftek on 2/28/2017.
 */
public interface MetadataSerializer<S, R> {

    S deserialize(StreamSource source) throws SerializationException;
    R serialize(S object) throws SerializationException;

}
