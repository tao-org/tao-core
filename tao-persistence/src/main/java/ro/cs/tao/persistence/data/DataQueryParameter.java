package ro.cs.tao.persistence.data;

import ro.cs.tao.persistence.data.util.DataQueryParameterKey;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DataQueryParameter persistent entity
 *
 * @author oana
 *
 */
@Entity
@Table(name = "tao.data_query_parameters")
@IdClass(DataQueryParameterKey.class)
public class DataQueryParameter {

    /**
     * Data Query Parameter min value column maximum length
     */
    private static final int DATA_QUERY_PARAMETER_MIN_VALUE_COLUMN_MAX_LENGTH = 250;

    /**
     * Data Query Parameter max value column maximum length
     */
    private static final int DATA_QUERY_PARAMETER_MAX_VALUE_COLUMN_MAX_LENGTH = 250;

    /**
     * Data Query Parameter value column maximum length
     */
    private static final int DATA_QUERY_PARAMETER_VALUE_COLUMN_MAX_LENGTH = 250;

    /**
     * Data Query to which the parameter belongs to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name="data_query_id", nullable = false)
    private DataQuery dataQuery;

    /**
     * Query Parameter
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn (name="query_parameter_id", nullable = false)
    private QueryParameter queryParameter;

    /**
     * Flag that indicates if the query parameter is optional or not
     */
    @Column(name = "optional")
    @NotNull
    private Boolean optional;

    /**
     * Query parameter min value
     */
    @Column(name = "min_value")
    @Size(min = 1, max = DATA_QUERY_PARAMETER_MIN_VALUE_COLUMN_MAX_LENGTH)
    private String minValue;

    /**
     * Query parameter max value
     */
    @Column(name = "max_value")
    @Size(min = 1, max = DATA_QUERY_PARAMETER_MAX_VALUE_COLUMN_MAX_LENGTH)
    private String maxValue;

    /**
     * Query parameter value
     */
    @Column(name = "value")
    @Size(min = 1, max = DATA_QUERY_PARAMETER_VALUE_COLUMN_MAX_LENGTH)
    private String value;

    public DataQuery getDataQuery() {
        return dataQuery;
    }

    public void setDataQuery(DataQuery dataQuery) {
        this.dataQuery = dataQuery;
    }

    public QueryParameter getQueryParameter() {
        return queryParameter;
    }

    public void setQueryParameter(QueryParameter queryParameter) {
        this.queryParameter = queryParameter;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public String getMinValue() {
        return minValue;
    }

    public void setMinValue(String minValue) {
        this.minValue = minValue;
    }

    public String getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(String maxValue) {
        this.maxValue = maxValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
