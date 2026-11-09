package ro.cs.tao.execution.model;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;

/**
 * Enum that defines the execution strategy types for distributing or managing tasks
 * in a processing workflow. It provides predefined constants, each associated with
 * a unique integer value and a descriptive representation.
 *
 * The enum implements the {@code TaoEnum<Integer>} interface, allowing an enumeration
 * of execution strategy types to contain both a unique value and a human-readable description.
 *
 * Enumeration Constants:
 * - DISTRIBUTE(1, "Distribute tasks to node pool"): Represents an execution strategy where
 *   tasks are distributed across a pool of nodes for parallel execution.
 * - SAME_NODE(2, "Bound job tasks to the same node"): Represents an execution strategy where
 *   all job tasks are executed on the same node to maintain data locality or resource constraints.
 *
 * Thread Safety:
 * This enum is immutable and thread-safe as long as its usage adheres to typical enum semantics.
 */
@XmlEnum(Integer.class)
public enum ExecutionStrategyType implements TaoEnum<Integer> {

    DISTRIBUTE(1, "Distribute tasks to node pool"),
    SAME_NODE(2, "Bound job tasks to the same node");

    private final int value;
    private final String description;

    ExecutionStrategyType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    @Override
    public String friendlyName() {
        return this.description;
    }

    @Override
    public Integer value() { return this.value; }
}
