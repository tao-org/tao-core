package ro.cs.tao.services.model.monitoring;

/**
 * @author Cosmin Cara
 */
public class Memory {
    private MemoryUnit memoryUnit;

    private long heapCommitted;
    private long heapInitial;
    private long heapMax;
    private long heapUsed;
    private long nonHeapCommitted;
    private long nonHeapInitial;
    private long nonHeapMax;
    private long nonHeapUsed;

    public Memory(MemoryUnit unit) { this.memoryUnit = unit; }

    public MemoryUnit getMemoryUnit() {
        return memoryUnit;
    }

    public long getHeapCommitted() {
        return heapCommitted / this.memoryUnit.value();
    }

    public void setHeapCommitted(long heapCommitted) {
        this.heapCommitted = heapCommitted;
    }

    public long getHeapInitial() {
        return heapInitial / this.memoryUnit.value();
    }

    public void setHeapInitial(long heapInitial) {
        this.heapInitial = heapInitial;
    }

    public long getHeapMax() {
        return heapMax / this.memoryUnit.value();
    }

    public void setHeapMax(long heapMax) {
        this.heapMax = heapMax;
    }

    public long getHeapUsed() {
        return heapUsed / this.memoryUnit.value();
    }

    public void setHeapUsed(long heapUsed) {
        this.heapUsed = heapUsed;
    }

    public long getNonHeapCommitted() {
        return nonHeapCommitted / this.memoryUnit.value();
    }

    public void setNonHeapCommitted(long nonHeapCommitted) {
        this.nonHeapCommitted = nonHeapCommitted;
    }

    public long getNonHeapInitial() {
        return nonHeapInitial / this.memoryUnit.value();
    }

    public void setNonHeapInitial(long nonHeapInitial) {
        this.nonHeapInitial = nonHeapInitial;
    }

    public long getNonHeapMax() {
        return nonHeapMax / this.memoryUnit.value();
    }

    public void setNonHeapMax(long nonHeapMax) {
        this.nonHeapMax = nonHeapMax;
    }

    public long getNonHeapUsed() {
        return nonHeapUsed / this.memoryUnit.value();
    }

    public void setNonHeapUsed(long nonHeapUsed) {
        this.nonHeapUsed = nonHeapUsed;
    }
}
