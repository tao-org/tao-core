package ro.cs.tao.persistence.data;

import ro.cs.tao.persistence.data.util.DataProductMetadataKey;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by oana on 7/26/2017.
 */
@Entity
@Table(name = "tao.data_product_metadata")
@IdClass(DataProductMetadataKey.class)
public class DataProductMetadata {

    /**
     * Data product metadata name column maximum length
     */
    private static final int DATA_PRODUCT_METADATA_NAME_COLUMN_MAX_LENGTH = 250;

    /**
     * Data product metadata value column maximum length
     */
    private static final int DATA_PRODUCT_METADATA_VALUE_COLUMN_MAX_LENGTH = 500;

    /**
     * The data product to which this metadata belongs to
     */
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "data_product_id", nullable = false)
    private DataProduct dataProduct;

    /**
     * Data product metadata name
     */
    @Id
    @Column(name = "attribute_name")
    @NotNull
    @Size(min = 1, max = DATA_PRODUCT_METADATA_NAME_COLUMN_MAX_LENGTH)
    private String attributeName;

    /**
     * Data product metadata value
     */
    @Column(name = "attribute_value")
    @NotNull
    @Size(min = 1, max = DATA_PRODUCT_METADATA_VALUE_COLUMN_MAX_LENGTH)
    private String attributeValue;

    public DataProduct getDataProduct() {
        return dataProduct;
    }

    public void setDataProduct(DataProduct dataProduct) {
        this.dataProduct = dataProduct;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeValue() {
        return attributeValue;
    }

    public void setAttributeValue(String attributeValue) {
        this.attributeValue = attributeValue;
    }
}
