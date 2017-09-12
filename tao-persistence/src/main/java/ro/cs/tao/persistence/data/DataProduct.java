package ro.cs.tao.persistence.data;

import com.vividsolutions.jts.geom.Geometry;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;
import ro.cs.tao.persistence.data.util.hstore.HstoreUserType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Data Result persistent entity
 * 
 * @author oana
 *
 */
@Entity
@Table(name = "tao.data_product")
@TypeDefs({@TypeDef(name = "hstore",  typeClass = HstoreUserType.class)})
public class DataProduct implements Serializable {

	/**
	 * Data product alphanumerical identifier column maximum length
	 */
	private static final int DATA_PRODUCT_IDENTIFIER_COLUMN_MAX_LENGTH = 250;

	/**
	 * Data product name column maximum length
	 */
	private static final int DATA_PRODUCT_NAME_COLUMN_MAX_LENGTH = 250;

	/**
	 * Unique identifier
	 */
	@Id
	@SequenceGenerator(name = "data_product_identifier", sequenceName = "tao.data_product_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "data_product_identifier")
	@Column(name = "id")
	@NotNull
	private Long id;

	/**
	 * Data product name
	 */
	@Column(name = "identifier", unique = true)
	@NotNull
	@Size(min = 1, max = DATA_PRODUCT_IDENTIFIER_COLUMN_MAX_LENGTH)
	private String identifier;

	/**
	 * Data product name
	 */
	@Column(name = "name")
	@NotNull
	@Size(min = 1, max = DATA_PRODUCT_NAME_COLUMN_MAX_LENGTH)
	private String name;
	
	/**
	 * Data type (format)
	 */
	@Column(name = "type_id")
	@NotNull
	private Integer dataFormat;

	/**
	 * Data product footprint
	 */
	@Column(name = "geometry", columnDefinition="Geometry")
	@NotNull
	private Geometry geometry;
	
	/**
	 * Coordinate reference system
	 */
	@Column(name = "coordinate_reference_system")
	private String coordinateReferenceSystem;
	
	/**
	 * Location
	 */
	@Column(name = "location")
	@NotNull
	private String location;
	
	/**
	 * Sensor type
	 */
	@Column(name = "sensor_type_id")
	@NotNull
	private Integer sensorType;
	
	/**
	 * Acquisition date
	 */
	@Column(name = "acquisition_date")
	private LocalDateTime acquisitionDate;
	
	/**
	 * Pixel type
	 */
	@Column(name = "pixel_type_id")
	@NotNull
	private Integer pixelType;
	
	/**
	 * Width
	 */
	@Column(name = "width")
	@NotNull
	private Integer width;
	
	/**
	 * Height
	 */
	@Column(name = "height")
	@NotNull
	private Integer height;

	/**
	 * Attributes
	 */
    @Type(type = "hstore")
    @Column(name = "attributes" , columnDefinition = "hstore")
    private Map<String, String> attributes = new HashMap<>();
	
	/**
	 * The user to which this product belongs to
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_id", nullable = true)
	private User user;
	
	/**
	 * The data source from where the product was acquired
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "data_source_id", nullable = true)
	private DataSource dataSource;
	
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
	 * Data Result metadata
	 */
	@OneToMany (fetch = FetchType.EAGER, mappedBy = "dataProduct")
	private Set<DataProductMetadata> metadataSet;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(Integer dataFormat) {
		this.dataFormat = dataFormat;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getCoordinateReferenceSystem() {
		return coordinateReferenceSystem;
	}

	public void setCoordinateReferenceSystem(String coordinateReferenceSystem) {
		this.coordinateReferenceSystem = coordinateReferenceSystem;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getSensorType() {
		return sensorType;
	}

	public void setSensorType(Integer sensorType) {
		this.sensorType = sensorType;
	}

	public LocalDateTime getAcquisitionDate() {
		return acquisitionDate;
	}

	public void setAcquisitionDate(LocalDateTime acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}

	public Integer getPixelType() {
		return pixelType;
	}

	public void setPixelType(Integer pixelType) {
		this.pixelType = pixelType;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

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

	public Set<DataProductMetadata> getMetadataSet() {
		return metadataSet;
	}

	public void setMetadataSet(Set<DataProductMetadata> metadataSet) {
		this.metadataSet = metadataSet;
	}
}
