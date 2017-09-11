package ro.cs.tao.serialization;

/**
 * Created by kraftek on 2/28/2017.
 */
public class SerializerFactory {

    public static <T> Serializer create(Class<T> clazz, MediaType mediaType) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz);
            case XML:
            default:
                return new XmlSerializer<T>(clazz);
        }
    }

}
