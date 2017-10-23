package ro.cs.tao.serialization;

/**
 * Created by kraftek on 2/28/2017.
 */
public class SerializerFactory {

    public static <T> BaseSerializer<T> create(Class<T> clazz, MediaType mediaType) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz);
            case XML:
            default:
                return new XmlSerializer<T>(clazz);
        }
    }

    public static <T> BaseSerializer<T> create(Class<T> clazz, MediaType mediaType, Class...dependencies) throws SerializationException {
        switch (mediaType) {
            case JSON:
                return new JsonSerializer<T>(clazz, dependencies);
            case XML:
            default:
                return new XmlSerializer<T>(clazz, dependencies);
        }
    }

}
