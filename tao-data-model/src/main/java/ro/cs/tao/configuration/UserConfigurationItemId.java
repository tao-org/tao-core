package ro.cs.tao.configuration;

import java.io.Serializable;
import java.util.Objects;

public class UserConfigurationItemId implements Serializable {
    private String userId;
    private String key;

    public UserConfigurationItemId() {
    }

    public UserConfigurationItemId(String userId, String key) {
        this.userId = userId;
        this.key = key;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserConfigurationItemId that = (UserConfigurationItemId) o;
        return Objects.equals(userId, that.userId) && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, key);
    }
}
