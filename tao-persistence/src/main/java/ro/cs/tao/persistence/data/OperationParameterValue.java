package ro.cs.tao.persistence.data;
//
//import ro.cs.tao.persistence.data.util.OperationParameterValueKey;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//
///**
// * OperationParameterValue persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.parameter_values_set")
//@IdClass(OperationParameterValueKey.class)
//public class OperationParameterValue implements Serializable {
//
//    /**
//     * Data product metadata value column maximum length
//     */
//    private static final int PROCESSING_OPERATION_PARAMETER_VALUE_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * The processing operation parameter to which this possible value belongs to
//     */
//    @Id
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "parameter_id", nullable = false)
//    private OperationParameter operationParameter;
//
//    /**
//      * Parameter possible value
//    */
//    @Id
//    @Column(name = "parameter_value")
//    @NotNull
//    @Size(min = 1, max = PROCESSING_OPERATION_PARAMETER_VALUE_COLUMN_MAX_LENGTH)
//    private String possibleValue;
//
//    public OperationParameter getOperationParameter() {
//        return operationParameter;
//    }
//
//    public void setOperationParameter(OperationParameter operationParameter) {
//        this.operationParameter = operationParameter;
//    }
//
//    public String getPossibleValue() {
//        return possibleValue;
//    }
//
//    public void setPossibleValue(String possibleValue) {
//        this.possibleValue = possibleValue;
//    }
//}
