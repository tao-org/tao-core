package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * User persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.\"user\"")
public class User {

    /**
     * Username column maximum length
     */
    private static final int USER_USERNAME_COLUMN_MAX_LENGTH = 50;

    /**
     * User email (main and alternative) column maximum length
     */
    private static final int USER_EMAIL_COLUMN_MAX_LENGTH = 100;

    /**
     * First name column maximum length
     */
    private static final int USER_FIRST_NAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Last name column maximum length
     */
    private static final int USER_LAST_NAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Phone number column maximum length
     */
    private static final int USER_PHONE_COLUMN_MAX_LENGTH = 50;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "user_identifier", sequenceName = "tao.user_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_identifier")
    @Column(name = "id")
    @NotNull
    private Integer id;

    /**
     * Username used for login
     */
    @Column(name = "username", unique = true)
    @NotNull
    @Size(min = 1, max = USER_USERNAME_COLUMN_MAX_LENGTH)
    private String username;

    /**
     * User password
     */
    @Column(name = "password")
    @NotNull
    private String password;

    /**
     * User email
     */
    @Column(name = "email")
    @NotNull
    @Size(min = 1, max = USER_EMAIL_COLUMN_MAX_LENGTH)
    private String email;

    /**
     * User alternative email
     */
    @Column(name = "alternative_email")
    @Size(min = 1, max = USER_EMAIL_COLUMN_MAX_LENGTH)
    private String alternativeEmail;

    /**
     * User first name
     */
    @Column(name = "first_name")
    @NotNull
    @Size(min = 1, max = USER_FIRST_NAME_COLUMN_MAX_LENGTH)
    private String firstName;

    /**
     * User last name
     */
    @Column(name = "last_name")
    @NotNull
    @Size(min = 1, max = USER_LAST_NAME_COLUMN_MAX_LENGTH)
    private String lastName;

    /**
     * User phone number
     */
    @Column(name = "phone")
    @Size(min = 1, max = USER_PHONE_COLUMN_MAX_LENGTH)
    private String phone;

    /**
     * Last login date
     */
    @Column(name = "last_login_date")
    @NotNull
    private LocalDateTime lastLoginDate;

    /**
     * User quota
     */
    @Column(name = "quota")
    @NotNull
    private Double quota;

    /**
     * The group to which the user belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    /**
     * Created date
     */
    @Column(name = "created")
    @NotNull
    private LocalDateTime createdDate;

    /**
     * Modified date
     */
    @Column(name = "modified")
    private LocalDateTime modifiedDate;

    /**
     * Flag that indicates if the user is active or not
     */
    @Column(name = "active")
    @NotNull
    private Boolean active;

    /**
     * User preferences
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "user")
    private Set<UserPreference> preferences;

    /**
     * User data queries
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "user")
    private Set<UserDataQuery> userDataQueries;

    /**
     * User data sources connections
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "user")
    private Set<UserDataSourceConnection> userDataSourceConnections;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlternativeEmail() {
        return alternativeEmail;
    }

    public void setAlternativeEmail(String alternativeEmail) {
        this.alternativeEmail = alternativeEmail;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public Double getQuota() {
        return quota;
    }

    public void setQuota(Double quota) {
        this.quota = quota;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Set<UserPreference> getPreferences() {
        return preferences;
    }

    public void setPreferences(Set<UserPreference> preferences) {
        this.preferences = preferences;
    }

    public Set<UserDataQuery> getUserDataQueries() {
        return userDataQueries;
    }

    public void setUserDataQueries(Set<UserDataQuery> userDataQueries) {
        this.userDataQueries = userDataQueries;
    }

    public Set<UserDataSourceConnection> getUserDataSourceConnections() {
        return userDataSourceConnections;
    }

    public void setUserDataSourceConnections(Set<UserDataSourceConnection> userDataSourceConnections) {
        this.userDataSourceConnections = userDataSourceConnections;
    }
}
