package ro.cs.tao.bridge.spring;

import ro.cs.tao.persistence.PersistenceManager;

/**
 * Created by cosmin on 9/13/2017.
 */
public interface SpringContextBridgedServices {
    PersistenceManager getPersistenceManager();
}
