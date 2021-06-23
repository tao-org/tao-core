package ro.cs.tao.eodata.enums;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

@XmlEnum(Integer.class)
public enum ProductStatus implements TaoEnum<Integer> {
    /**
     * The product was queried and its descriptor saved, but the product is not downloaded.
     */
    @XmlEnumValue("1")
    QUERIED(1, "Queried"),
    /**
     * Product download is in progress.
     */
    @XmlEnumValue("2")
    DOWNLOADING(2, "Downloading"),
    /**
     * The product was downloaded.
     */
    @XmlEnumValue("3")
    DOWNLOADED(3, "Downloaded"),
    /**
     * The product download failed.
     */
    @XmlEnumValue("4")
    FAILED(4, "Downloaded"),
    /**
     * The product is an output of an execution task.
     */
    @XmlEnumValue("5")
    PRODUCED(5, "Produced"),
    /**
     * The product was queried for download.
     */
    @XmlEnumValue("6")
    QUEUED(5, "Queued");

    private final int value;
    private final String description;

    ProductStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public Integer value() { return this.value; }
}
