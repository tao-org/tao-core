package ro.cs.tao.datasource;

import ro.cs.tao.BaseException;

/**
 * @author Cosmin Cara
 */
public class InterruptedException extends BaseException {
    public InterruptedException() {
    }

    public InterruptedException(String message) {
        super(message);
    }

    public InterruptedException(String message, Throwable cause) {
        super(message, cause);
    }

    public InterruptedException(Throwable cause) {
        super(cause);
    }
}
