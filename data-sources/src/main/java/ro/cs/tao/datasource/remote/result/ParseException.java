package ro.cs.tao.datasource.remote.result;

import ro.cs.tao.BaseException;

/**
 * @author Cosmin Cara
 */
public class ParseException extends BaseException {
    public ParseException() {
    }

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseException(Throwable cause) {
        super(cause);
    }
}
