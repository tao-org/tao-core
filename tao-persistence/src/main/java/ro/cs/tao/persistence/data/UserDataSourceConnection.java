package ro.cs.tao.persistence.data;

import ro.cs.tao.persistence.data.util.UserDataSourceConnectionKey;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * UserDataSourceConnection persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.user_data_source_connection")
@IdClass(UserDataSourceConnectionKey.class)
public class UserDataSourceConnection implements Serializable {

    /**
     * Data source connection username column maximum length
     */
    private static final int DATA_SOURCE_CONNECTION_USERNAME_COLUMN_MAX_LENGTH = 50;

    /**
     * The user to which this data source connection belongs to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The data source to which this connection applies to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    /**
     * Username used for login
     */
    @Column(name = "username")
    @NotNull
    @Size(min = 1, max = DATA_SOURCE_CONNECTION_USERNAME_COLUMN_MAX_LENGTH)
    private String username;

    /**
     * User password
     */
    @Column(name = "password")
    private String password;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
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
}
