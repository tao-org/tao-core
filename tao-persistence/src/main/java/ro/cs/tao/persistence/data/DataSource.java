package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * DataSource persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.data_source")
public class DataSource {

    /**
     * Data source name column maximum length
     */
    private static final int DATA_SOURCE_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Data source username column maximum length
     */
    private static final int DATA_SOURCE_USERNAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Data source connection string column maximum length
     */
    private static final int DATA_SOURCE_CONNECTION_STRING_COLUMN_MAX_LENGTH = 500;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "data_source_identifier", sequenceName = "tao.data_source_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_source_identifier")
    @Column(name = "id")
    @NotNull
    private Integer id;

    /**
     * Data source name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = DATA_SOURCE_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * Data source type
     */
    @Column(name = "data_source_type_id")
    @NotNull
    private Integer dataSourceType;

    /**
     * Username used for login
     */
    @Column(name = "username")
    @NotNull
    @Size(min = 1, max = DATA_SOURCE_USERNAME_COLUMN_MAX_LENGTH)
    private String username;

    /**
     * User password
     */
    @Column(name = "password")
    private String password;

    /**
     * Authentication token
     */
    @Column(name = "auth_token")
    private String authToken;

    /**
     * Connection string
     */
    @Column(name = "connection_string")
    @Size(min = 1, max = DATA_SOURCE_CONNECTION_STRING_COLUMN_MAX_LENGTH)
    private String connectionString;

    /**
     * Description
     */
    @Column(name = "description")
    private String description;

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
     * Flag that indicates if the data source is active or not
     */
    @Column(name = "active")
    @NotNull
    private Boolean active;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(Integer dataSourceType) {
        this.dataSourceType = dataSourceType;
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

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
}
