package ro.cs.tao.utils.executors.container;

public enum ContainerType {
    DOCKER("docker"),
    KUBERNETES("kubectl");

    private final String cmd;

    ContainerType(String value) {
        this.cmd = value;
    }

    public String value() { return this.cmd; }
}
