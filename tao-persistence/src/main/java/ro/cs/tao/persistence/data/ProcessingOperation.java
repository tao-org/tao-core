package ro.cs.tao.persistence.data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Created by oana on 7/25/2017.
 */
@Entity
@Table(name = "tao.processing_operation")
public class ProcessingOperation {

    /**
     * Processing operation name column maximum length
     */
    private static final int PROCESSING_OPERATION_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Processing operation progress pattern column maximum length
     */
    private static final int PROCESSING_OPERATION_PROGRESS_PATTERN_COLUMN_MAX_LENGTH = 500;

    /**
     * Processing operation error pattern column maximum length
     */
    private static final int PROCESSING_OPERATION_ERROR_PATTERN_COLUMN_MAX_LENGTH = 500;

    /**
     * Unique identifier
     */
    @Id
    @SequenceGenerator(name = "processing_operation_identifier", sequenceName = "tao.processing_operation_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "processing_operation_identifier")
    @Column(name = "id")
    @NotNull
    private Integer id;

    /**
     * Processing operation name
     */
    @Column(name = "name")
    @NotNull
    @Size(min = 1, max = PROCESSING_OPERATION_NAME_COLUMN_MAX_LENGTH)
    private String name;

    /**
     * Processing operation progress pattern
     */
    @Column(name = "progress_pattern")
    @Size(min = 1, max = PROCESSING_OPERATION_PROGRESS_PATTERN_COLUMN_MAX_LENGTH)
    private String progressPattern;

    /**
     * Processing operation error pattern
     */
    @Column(name = "error_pattern")
    @Size(min = 1, max = PROCESSING_OPERATION_ERROR_PATTERN_COLUMN_MAX_LENGTH)
    private String errorPattern;

    /**
     * Processing operation source
     */
    @Column(name = "source_id")
    @NotNull
    private Integer source;

    /**
     * Flag that indicates if the processing operation is handling output name
     */
    @Column(name = "is_handling_output_name")
    private Boolean isHandlingOutputName;

    /**
     * The operations parameters
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "tao.operation_parameters",
      joinColumns = {@JoinColumn(name = "operation_id", referencedColumnName = "id") },
      inverseJoinColumns = {@JoinColumn(name = "parameter_id", referencedColumnName = "id")})
    private Set<OperationParameter> parameters;

    /**
     * The operations variables
     */
    @OneToMany (fetch = FetchType.EAGER, mappedBy = "processingOperation")
    private Set<OperationVariable> variables;

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

    public String getProgressPattern() {
        return progressPattern;
    }

    public void setProgressPattern(String progressPattern) {
        this.progressPattern = progressPattern;
    }

    public String getErrorPattern() {
        return errorPattern;
    }

    public void setErrorPattern(String errorPattern) {
        this.errorPattern = errorPattern;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Boolean getIsHandlingOutputName() {
        return isHandlingOutputName;
    }

    public void setIsHandlingOutputName(Boolean isHandlingOutputName) {
        this.isHandlingOutputName = isHandlingOutputName;
    }

    public Set<OperationParameter> getParameters() {
        return parameters;
    }

    public void setParameters(Set<OperationParameter> parameters) {
        this.parameters = parameters;
    }

    public Set<OperationVariable> getVariables() {
        return variables;
    }

    public void setVariables(Set<OperationVariable> variables) {
        this.variables = variables;
    }
}
