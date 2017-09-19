package ro.cs.tao.execution;

import ro.cs.tao.BaseException;

/**
 * Created by cosmin on 9/12/2017.
 */
public class ExecutionException extends BaseException {
    public ExecutionException() {
    }

    public ExecutionException(String message) {
        super(message);
    }

    public ExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExecutionException(Throwable cause) {
        super(cause);
    }
}
