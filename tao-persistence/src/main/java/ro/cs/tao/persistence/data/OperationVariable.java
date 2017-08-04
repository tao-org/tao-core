package ro.cs.tao.persistence.data;

import ro.cs.tao.persistence.data.util.OperationVariableKey;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by oana on 8/2/2017.
 */
@Entity
@Table(name = "tao.operation_variables")
@IdClass(OperationVariableKey.class)
public class OperationVariable {

    /**
     * Processing operation variable name column maximum length
     */
    private static final int PROCESSING_OPERATION_VARIABLE_NAME_COLUMN_MAX_LENGTH = 50;

    /**
     * Processing operation variable value column maximum length
     */
    private static final int PROCESSING_OPERATION_VARIABLE_VALUE_COLUMN_MAX_LENGTH = 500;

    /**
     * The processing operation to which this variable belongs to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "operation_id", nullable = false)
    private ProcessingOperation processingOperation;

    /**
     * Variable name
     */
    @Id
    @Column(name = "variable_name")
    @NotNull
    @Size(min = 1, max = PROCESSING_OPERATION_VARIABLE_NAME_COLUMN_MAX_LENGTH)
    private String variableName;

    /**
     * Variable value
     */
    @Column(name = "variable_value")
    @NotNull
    @Size(min = 1, max = PROCESSING_OPERATION_VARIABLE_VALUE_COLUMN_MAX_LENGTH)
    private String variableValue;

    public ProcessingOperation getProcessingOperation() {
        return processingOperation;
    }

    public void setProcessingOperation(ProcessingOperation processingOperation) {
        this.processingOperation = processingOperation;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableValue() {
        return variableValue;
    }

    public void setVariableValue(String variableValue) {
        this.variableValue = variableValue;
    }
}
