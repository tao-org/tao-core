package ro.cs.tao.component.constraints;

import ro.cs.tao.BaseException;

/**
 * @author Cosmin Cara
 */
public class ConstraintException extends BaseException {
    public ConstraintException() {
    }

    public ConstraintException(String message) {
        super(message);
    }

    public ConstraintException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConstraintException(Throwable cause) {
        super(cause);
    }
}
