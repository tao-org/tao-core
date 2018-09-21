package ro.cs.tao.component;

import ro.cs.tao.component.enums.Condition;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ParameterDependency {
    private String referencedParameterId;
    private Condition condition;
    private String expectedValue;

    public ParameterDependency() { }

    public ParameterDependency(String referencedParameterId, Condition condition, String... expectedValue) {
        this.referencedParameterId = referencedParameterId;
        this.condition = condition;
        if (condition == Condition.IN || condition == Condition.NOTIN) {
            if (expectedValue == null || expectedValue.length == 0) {
                throw new IllegalArgumentException("At least one value has to be supplied for parameter [expectedValues]");
            }
            this.expectedValue = String.join(",", expectedValue);
        } else {
            this.expectedValue = expectedValue != null && expectedValue.length > 0 ? expectedValue[0] : null;
        }
    }

    public String getReferencedParameterId() { return referencedParameterId; }
    public void setReferencedParameterId(String referencedParameterId) { this.referencedParameterId = referencedParameterId; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    public Set<String> expectedValues() {
        if (this.expectedValue == null) {
            return null;
        }
        Set<String> values;
        if (condition == Condition.IN || condition == Condition.NOTIN) {
            values = Arrays.stream(this.expectedValue.split(",")).distinct().collect(Collectors.toSet());
        } else {
            values = new HashSet<>();
            values.add(this.expectedValue);
        }
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParameterDependency that = (ParameterDependency) o;
        return Objects.equals(referencedParameterId, that.referencedParameterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(referencedParameterId);
    }
}
