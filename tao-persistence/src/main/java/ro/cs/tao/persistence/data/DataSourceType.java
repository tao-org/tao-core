package ro.cs.tao.persistence.data;
//
//import javax.persistence.*;
//import javax.validation.constraints.NotNull;
//import javax.validation.constraints.Size;
//import java.io.Serializable;
//
///**
// * DataSource persistent entity
// *
// * @author oana
// *
// */
//@Entity
//@Table(name = "tao.data_source_type")
//public class DataSourceType implements Serializable {
//
//    /**
//     * Data source type column maximum length
//     */
//    private static final int DATA_SOURCE_TYPE_COLUMN_MAX_LENGTH = 250;
//
//    /**
//     * Unique identifier
//     */
//    @Id
//    @SequenceGenerator(name = "data_source_type_identifier", sequenceName = "tao.data_source_type_id_seq", allocationSize = 1)
//    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_source_type_identifier")
//    @Column(name = "id")
//    @NotNull
//    private Integer id;
//
//    /**
//     * Data source type
//     */
//    @Column(name = "type")
//    @NotNull
//    @Size(min = 1, max = DATA_SOURCE_TYPE_COLUMN_MAX_LENGTH)
//    private String type;
//
//    public Integer getId() {
//        return id;
//    }
//
//    public void setId(Integer id) {
//        this.id = id;
//    }
//
//    public String getType() {
//        return type;
//    }
//
//    public void setType(String type) {
//        this.type = type;
//    }
//}
