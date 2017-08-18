package ro.cs.tao.topology;

import ro.cs.tao.BaseException;

/**
 * @author Cosmin Cara
 */
public class TopologyException extends BaseException {
    public TopologyException() {
    }

    public TopologyException(String message) {
        super(message);
    }

    public TopologyException(String message, Throwable cause) {
        super(message, cause);
    }

    public TopologyException(Throwable cause) {
        super(cause);
    }
}
