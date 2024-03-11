package ro.cs.tao.drmaa;

import ro.cs.tao.TaoEnum;

public enum Environment implements TaoEnum<Integer> {
    DEFAULT(1, "Default"),
    KUBERNETES(2, "Kubernetes");

    private final int value;
    private final String description;

    Environment(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Integer value() {
        return this.value;
    }
}
