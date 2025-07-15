package ro.cs.tao.subscription;

/**
 * Represents a subscription to a flavor resource configuration.
 * This class holds details related to a specific flavor subscription
 * such as the flavor identifier, the number of instances, and
 * optional configurations for storage in HDD or SSD.
 */
public class FlavorSubscription {
    private String flavorId;
    private int quantity;
    private Integer hddGB;
    private Integer ssdGB;

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public Integer getHddGB() {
        return hddGB;
    }

    public void setHddGB(Integer hddGB) {
        this.hddGB = hddGB;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer getSsdGB() {
        return ssdGB;
    }

    public void setSsdGB(Integer ssdGB) {
        this.ssdGB = ssdGB;
    }
}
