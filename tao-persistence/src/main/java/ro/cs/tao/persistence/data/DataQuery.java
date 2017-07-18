package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by oana on 7/18/2017.
 */
@Entity
@Table(name = "tao.data_query")
public class DataQuery {

    /**
     * Data query name column maximum length
     */
    private static final int DATA_QUERY_NAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "data_query_identifier", sequenceName = "tao.data_query_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_query_identifier")
    @Column(name = "id")
    @NotNull
    private Integer id;

    /**
     * Data query name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = DATA_QUERY_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * The data source to which the query applies
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_source_id", nullable = false)
    private DataSource dataSource;

    /**
     * Query text
     */
    @Column(name = "query_text")
    @NotNull
    private String queryText;

    /**
     * Page size
     */
    @Column(name = "page_size")
    private Integer pageSize;

    /**
     * Page number
     */
    @Column(name = "page_number")
    private Integer pageNumber;

    /**
     * Limit
     */
    @Column(name = "\"limit\"")
    private Integer limit;

    /**
     * Timeout
     */
    @Column(name = "timeout")
    private Integer timeout;

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

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public String getQueryText() {
        return queryText;
    }

    public void setQueryText(String queryText) {
        this.queryText = queryText;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }
}
