package ro.cs.tao.subscription;

import java.io.Serializable;

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
