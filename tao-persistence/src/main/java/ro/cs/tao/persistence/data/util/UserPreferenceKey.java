package ro.cs.tao.persistence.data.util;

import ro.cs.tao.persistence.data.User;

import java.io.Serializable;

/**
 * Created by oana on 8/4/2017.
 */
public class UserPreferenceKey implements Serializable {
    private User user;
    private String prefKey;

    @Override
    public boolean equals(Object o) {
        if (this == o)
        {
            return true;
        }
        if (o == null|| getClass() != o.getClass())
        {
            return false;
        }

        UserPreferenceKey that = (UserPreferenceKey) o;

        if (user != null ?
          !user.equals(that.user) : that.user !=null)
            return false;
        if (prefKey != null ?
          !prefKey.equals(that.prefKey) : that.prefKey !=null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (user != null ? user.hashCode() : 0);
        result = 31 * result + (prefKey != null ? prefKey.hashCode() : 0);
        return result;
    }

}
