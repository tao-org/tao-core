package ro.cs.tao.services.bridge.spring;

import ro.cs.tao.BaseException;

/**
 * @author Cosmin Udroiu
 */
public class SpringBridgeException extends BaseException {
    public SpringBridgeException() {
    }

    public SpringBridgeException(String message) {
        super(message);
    }

    public SpringBridgeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpringBridgeException(Throwable cause) {
        super(cause);
    }
}
