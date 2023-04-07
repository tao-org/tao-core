package ro.cs.tao.messaging.system;

import ro.cs.tao.messaging.Message;

/**
 * Specialized message signaling the finalization of Spring boot initialization.
 *
 * @author Cosmin Cara
 */
public class StartupCompletedMessage extends Message {

    public StartupCompletedMessage() {
        super();
        setMessage("System is ready");
    }
}
