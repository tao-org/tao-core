package ro.cs.tao.utils.executors.container;

/**
 * Enum representing the type of containerization tool.
 * It provides a mapping between container system names (e.g., Docker, Kubernetes) and their
 * respective command-line tools.
 */
public enum ContainerType {
    DOCKER("docker"),
    KUBERNETES("kubectl");

    private final String cmd;

    ContainerType(String value) {
        this.cmd = value;
    }

    public String value() { return this.cmd; }
}
