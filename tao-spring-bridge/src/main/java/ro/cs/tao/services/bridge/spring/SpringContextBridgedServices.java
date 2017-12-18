package ro.cs.tao.services.bridge.spring;

import ro.cs.tao.messaging.MessagePersister;

/**
 * Created by cosmin on 9/13/2017.
 */
public interface SpringContextBridgedServices<T extends MessagePersister> {
    T getPersistenceManager();
}
