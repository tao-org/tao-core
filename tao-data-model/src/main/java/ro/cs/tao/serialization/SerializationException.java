package ro.cs.tao.serialization;

/**
 * Created by kraftek on 2/28/2017.
 */
public class SerializationException extends Exception {
    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
