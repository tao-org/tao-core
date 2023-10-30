package ro.cs.tao.execution.model;

import ro.cs.tao.TaoEnum;

import javax.xml.bind.annotation.XmlEnum;

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
