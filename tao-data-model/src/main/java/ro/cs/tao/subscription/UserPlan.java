package ro.cs.tao.subscription;

public class UserPlan {
    private String userPlan;
    private int cpu;
    private int ram;
    private int disk;
    private String flavor;

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    public int getDisk() {
        return disk;
    }

    public void setDisk(int disk) {
        this.disk = disk;
    }

    public String getFlavor() {
        return flavor;
    }

    public void setFlavor(String flavor) {
        this.flavor = flavor;
    }

    public int getRam() {
        return ram;
    }

    public void setRam(int ram) {
        this.ram = ram;
    }

    public String getUserPlan() {
        return userPlan;
    }

    public void setUserPlan(String userPlan) {
        this.userPlan = userPlan;
    }
}
