package ro.cs.tao.persistence.data;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//
///**
// * QueryParameter persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.query_parameter")
//public class QueryParameter implements Serializable {
//
//    /**
//     * Parameter name column maximum length
//     */
//    private static final int PARAMETER_NAME_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Unique identifier
//     */
//    @Id
//    @SequenceGenerator(name = "query_parameter_identifier", sequenceName = "tao.query_parameter_id_seq", allocationSize = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "query_parameter_identifier")
//    @Column(name = "id")
//    @NotNull
//    private Integer id;
//
//    /**
//     * Parameter value data type
//     */
//    @Column(name = "data_type_id")
//    @NotNull
//    private Integer dataType;
//
//    /**
//     * Parameter name
//     */
//    @Column(name = "name")
//    @NotNull
//    @Size(min = 1, max = PARAMETER_NAME_COLUMN_MAX_LENGTH)
//    private String name;
//}
