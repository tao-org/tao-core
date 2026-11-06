package ro.cs.tao.subscription;

import java.io.Serializable;

/**
 * Represents a user associated with an external resource subscription.
 * This class serves as a mapping between a user and a specific
 * instance of an external resource subscription.
 *
 * Implements Serializable to allow instances of this class
 * to be serialized and deserialized for storage or transmission purposes.
 */
public class ExternalSubscriptionUsers implements Serializable {
    private ExternalResourceSubscription subscription;
    private String userId;

    public ExternalResourceSubscription getSubscription() {
        return subscription;
    }

    public void setSubscription(ExternalResourceSubscription subscription) {
        this.subscription = subscription;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
