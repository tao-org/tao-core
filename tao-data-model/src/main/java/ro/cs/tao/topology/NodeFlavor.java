package ro.cs.tao.topology;

import ro.cs.tao.component.StringIdentifiable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "flavor")
public class NodeFlavor extends StringIdentifiable {
    private int cpu;
    private int memory;
    private int disk;
    private int swap;
    private float rxtxFactor;

    public NodeFlavor() {
        super();
    }

    public NodeFlavor(String id, int cpu, int memory, int disk, int swap, float rxtxFactor) {
        super(id);
        this.cpu = cpu;
        this.memory = memory;
        this.disk = disk;
        this.swap = swap;
        this.rxtxFactor = rxtxFactor;
    }

    @XmlElement(name = "cpu")
    public int getCpu() { return cpu; }
    public void setCpu(int cpu) { this.cpu = cpu; }

    @XmlElement(name = "memory")
    public int getMemory() { return memory; }
    public void setMemory(int memory) { this.memory = memory; }

    @XmlElement(name = "disk")
    public int getDisk() { return disk; }
    public void setDisk(int disk) { this.disk = disk; }

    @XmlElement(name = "swap")
    public int getSwap() { return swap; }
    public void setSwap(int swap) { this.swap = swap; }

    @XmlElement(name = "rxtxFactor")
    public float getRxtxFactor() { return rxtxFactor; }
    public void setRxtxFactor(float rxtxFactor) { this.rxtxFactor = rxtxFactor; }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
