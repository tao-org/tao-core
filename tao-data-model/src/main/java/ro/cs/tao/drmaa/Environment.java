package ro.cs.tao.drmaa;

import ro.cs.tao.TaoEnum;

/**
 * Represents the different environments in which the application can operate.
 * Each environment is associated with a specific integer value and a descriptive name.
 * It provides a mechanism to retrieve both the value and the friendly name for each environment.
 *
 * Implements:
 * - TaoEnum<Integer>: Adds additional properties such as friendly name and integer value to the enum.
 *
 * Enum Constants:
 * - DEFAULT: Represents the default environment configuration.
 * - KUBERNETES: Represents an environment configured for Kubernetes.
 */
public enum Environment implements TaoEnum<Integer> {
    DEFAULT(1, "Default"),
    KUBERNETES(2, "Kubernetes"),
    SIMPIPE(3, "Simpipe");

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
