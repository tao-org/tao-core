package ro.cs.tao.component.constraints;

/**
 * @author Cosmin Cara
 */
public class ConstraintException extends Exception {
    public ConstraintException(String message) {
        super(message);
    }

    public ConstraintException(Throwable cause) {
        super(cause);
    }
}
