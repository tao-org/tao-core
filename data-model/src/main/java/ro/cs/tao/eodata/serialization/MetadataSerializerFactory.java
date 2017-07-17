package ro.cs.tao.eodata.serialization;

/**
 * Created by kraftek on 2/28/2017.
 */
public class MetadataSerializerFactory {

    public static <T> MetadataSerializer create(Class<T> clazz, MediaType mediaType) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz);
            case XML:
            default:
                return new XmlSerializer<T>(clazz);
        }
    }

}
