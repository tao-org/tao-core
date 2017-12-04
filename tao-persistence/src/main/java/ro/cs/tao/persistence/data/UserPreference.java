package ro.cs.tao.persistence.data;
//
//import ro.cs.tao.persistence.data.util.UserPreferenceKey;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//
///**
// * UserPreference persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.user_prefs")
//@IdClass(UserPreferenceKey.class)
//public class UserPreference implements Serializable {
//
//    /**
//     * User preference key column maximum length
//     */
//    private static final int USER_PREF_KEY_COLUMN_MAX_LENGTH = 50;
//
//    /**
//     * User preference value column maximum length
//     */
//    private static final int USER_PREF_VALUE_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * The user to which this preference belongs to
//     */
//    @Id
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    /**
//     * User preference key
//     */
//    @Id
//    @Column(name = "pref_key")
//    @NotNull
//    @Size(min = 1, max = USER_PREF_KEY_COLUMN_MAX_LENGTH)
//    private String prefKey;
//
//    /**
//     * User preference key value
//     */
//    @Column(name = "pref_value")
//    @NotNull
//    @Size(min = 1, max = USER_PREF_VALUE_COLUMN_MAX_LENGTH)
//    private String prefValue;
//
//    public User getUser() {
//        return user;
//    }
//
//    public void setUser(User user) {
//        this.user = user;
//    }
//
//    public String getPrefKey() {
//        return prefKey;
//    }
//
//    public void setPrefKey(String prefKey) {
//        this.prefKey = prefKey;
//    }
//
//    public String getPrefValue() {
//        return prefValue;
//    }
//
//    public void setPrefValue(String prefValue) {
//        this.prefValue = prefValue;
//    }
//}
