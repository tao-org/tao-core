package ro.cs.tao.topology;

import ro.cs.tao.component.StringIdentifiable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Objects;

@XmlRootElement(name = "flavor")
public class NodeFlavor extends StringIdentifiable {
    private int cpu;
    private int memory;
    private int disk;
    private float rxtxFactor;

    @XmlElement(name = "cpu")
    public int getCpu() { return cpu; }
    public void setCpu(int cpu) { this.cpu = cpu; }

    @XmlElement(name = "memory")
    public int getMemory() { return memory; }
    public void setMemory(int memory) { this.memory = memory; }

    @XmlElement(name = "disk")
    public int getDisk() { return disk; }
    public void setDisk(int disk) { this.disk = disk; }

    @XmlElement(name = "rxtxFactor")
    public float getRxtxFactor() { return rxtxFactor; }
    public void setRxtxFactor(float rxtxFactor) { this.rxtxFactor = rxtxFactor; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NodeFlavor flavor = (NodeFlavor) o;
        return cpu == flavor.cpu &&
                memory == flavor.memory &&
                disk == flavor.disk &&
                Float.compare(flavor.rxtxFactor, rxtxFactor) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cpu, memory, disk, rxtxFactor);
    }
}
