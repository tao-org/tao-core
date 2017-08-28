package ro.cs.tao.component.validation;

import ro.cs.tao.BaseException;

/**
 * @author Cosmin Cara
 */
public class ValidationException extends BaseException {
    public ValidationException() {
    }

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(Throwable cause) {
        super(cause);
    }
}
