package ro.cs.tao.topology;

import ro.cs.tao.TaoEnum;

public enum NodeRole implements TaoEnum<String> {
    MASTER("master", "Master node"),
    WORKER("worker", "Worker node"),
    K8S("k8s", "Kubernetes node");

    private final String value;
    private final String description;

    NodeRole(String value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() { return this.description; }

    @Override
    public String value() { return this.value; }
}
