package ro.cs.tao.persistence.data;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//import java.util.Set;
//
///**
// * OperationParameter persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.parameter")
//public class OperationParameter implements Serializable {
//
//    /**
//     * Operation parameter name column maximum length
//     */
//    private static final int OPERATION_PARAMETER_NAME_COLUMN_MAX_LENGTH = 50;
//
//    /**
//     * Operation parameter data type column maximum length
//     */
//    private static final int OPERATION_PARAMETER_DATA_TYPE_COLUMN_MAX_LENGTH = 50;
//
//    /**
//     * Operation parameter default value column maximum length
//     */
//    private static final int OPERATION_PARAMETER_DEFAULT_VALUE_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Operation parameter description column maximum length
//     */
//    private static final int OPERATION_PARAMETER_DESCRIPTION_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Unique identifier
//     */
//    @Id
//    @SequenceGenerator(name = "parameter_identifier", sequenceName = "tao.parameter_id_seq", allocationSize = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "parameter_identifier")
//    @Column(name = "id")
//    @NotNull
//    private Long id;
//
//    /**
//     * Operation parameter name
//     */
//    @Column(name = "name")
//    @NotNull
//    @Size(min = 1, max = OPERATION_PARAMETER_NAME_COLUMN_MAX_LENGTH)
//    private String name;
//
//    /**
//     * Operation parameter data type
//     */
//    @Column(name = "data_type")
//    @NotNull
//    @Size(min = 1, max = OPERATION_PARAMETER_DATA_TYPE_COLUMN_MAX_LENGTH)
//    private String dataType;
//
//    /**
//     * Operation parameter default value
//     */
//    @Column(name = "default_value")
//    @NotNull
//    @Size(min = 1, max = OPERATION_PARAMETER_DEFAULT_VALUE_COLUMN_MAX_LENGTH)
//    private String defaultValue;
//
//    /**
//     * Operation parameter description
//     */
//    @Column(name = "description")
//    @NotNull
//    @Size(min = 1, max = OPERATION_PARAMETER_DESCRIPTION_COLUMN_MAX_LENGTH)
//    private String description;
//
//    /**
//     * Flag that indicates if the parameter value should not be NULL
//     */
//    @Column(name = "not_null")
//    private Boolean notNull;
//
//    /**
//     * Flag that indicates if the parameter value should not be empty
//     */
//    @Column(name = "not_empty")
//    private Boolean notEmpty;
//
//    /**
//     * Parameter type
//     */
//    @Column(name = "type_id")
//    private Integer type;
//
//    /**
//     * Depending on its type, a template parameter can have other regular parameters
//     */
//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinTable(name = "tao.parameter_parameter",
//      joinColumns = {@JoinColumn(name = "template_parameter_id", referencedColumnName = "id") },
//      inverseJoinColumns = {@JoinColumn(name = "regular_parameter_id", referencedColumnName = "id")})
//    private Set<OperationParameter> parameters;
//
//    /**
//     * Parameter values set
//     */
//    @OneToMany (fetch = FetchType.EAGER, mappedBy = "operationParameter")
//    private Set<OperationParameterValue> valueSet;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getDataType() {
//        return dataType;
//    }
//
//    public void setDataType(String dataType) {
//        this.dataType = dataType;
//    }
//
//    public String getDefaultValue() {
//        return defaultValue;
//    }
//
//    public void setDefaultValue(String defaultValue) {
//        this.defaultValue = defaultValue;
//    }
//
//    public String getDescription() {
//        return description;
//    }
//
//    public void setDescription(String description) {
//        this.description = description;
//    }
//
//    public Boolean getNotNull() {
//        return notNull;
//    }
//
//    public void setNotNull(Boolean notNull) {
//        this.notNull = notNull;
//    }
//
//    public Boolean getNotEmpty() {
//        return notEmpty;
//    }
//
//    public void setNotEmpty(Boolean notEmpty) {
//        this.notEmpty = notEmpty;
//    }
//
//    public Integer getType() {
//        return type;
//    }
//
//    public void setType(Integer type) {
//        this.type = type;
//    }
//
//    public Set<OperationParameter> getParameters() {
//        return parameters;
//    }
//
//    public void setParameters(Set<OperationParameter> parameters) {
//        this.parameters = parameters;
//    }
//
//    public Set<OperationParameterValue> getValueSet() {
//        return valueSet;
//    }
//
//    public void setValueSet(Set<OperationParameterValue> valueSet) {
//        this.valueSet = valueSet;
//    }
//}
