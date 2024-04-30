package ro.cs.tao.subscription;

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
