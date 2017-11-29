package ro.cs.tao.services.model.monitoring;

/**
 * @author Cosmin Cara
 */
public class Snapshot {
    private Memory memory;
    private Runtime runtime;

    public Memory getMemory() {
        return memory;
    }

    public void setMemory(Memory memory) {
        this.memory = memory;
    }

    public Runtime getRuntime() {
        return runtime;
    }

    public void setRuntime(Runtime runtime) {
        this.runtime = runtime;
    }
}
