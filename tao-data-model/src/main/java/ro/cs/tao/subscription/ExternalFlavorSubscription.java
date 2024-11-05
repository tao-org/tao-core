package ro.cs.tao.subscription;

public class ExternalFlavorSubscription {
    private String flavorId;
    private Integer cpu;
    private Integer ramGb;
    private Integer diskGB;

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getRamGb() {
        return ramGb;
    }

    public void setRamGb(Integer ramGb) {
        this.ramGb = ramGb;
    }

    public Integer getDiskGB() {
        return diskGB;
    }

    public void setDiskGB(Integer diskGB) {
        this.diskGB = diskGB;
    }
}
