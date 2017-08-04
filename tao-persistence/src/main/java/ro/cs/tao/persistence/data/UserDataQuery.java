package ro.cs.tao.persistence.data;

import ro.cs.tao.persistence.data.util.UserDataQueryKey;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * UserDataQuery persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.user_data_query")
@IdClass(UserDataQueryKey.class)
public class UserDataQuery {

    /**
     * User query parameter value column maximum length
     */
    private static final int USER_QUERY_PARAMETER_VALUE_COLUMN_MAX_LENGTH = 500;

    /**
     * The user to which this query belongs to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The data source to which this query applies to
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    /**
     * The job to which this query applies to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /**
     * Query parameter
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "query_parameter_id", nullable = false)
    private QueryParameter queryParameter;

    /**
     * Query parameter value
     */
    @Column(name = "query_parameter_value")
    @NotNull
    @Size(min = 1, max = USER_QUERY_PARAMETER_VALUE_COLUMN_MAX_LENGTH)
    private String queryParameterValue;

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

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    public QueryParameter getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(QueryParameter queryParameter) {
        this.queryParameter = queryParameter;
    }

    public String getQueryParameterValue() {
        return queryParameterValue;
    }

    public void setQueryParameterValue(String queryParameterValue) {
        this.queryParameterValue = queryParameterValue;
    }
}
