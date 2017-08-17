package ro.cs.tao.topology.executors;

/**
 * @author Cosmin Cara
 */
public enum ExecutionMode {
    SUPERUSER(true),
    USER(false);

    private final boolean mode;

    private ExecutionMode(final boolean mode) { this.mode = mode; }

    @Override
    public String toString() { return this.mode ? "SUPERUSER" : "USER"; }

    public boolean value() { return this.mode; }
}
