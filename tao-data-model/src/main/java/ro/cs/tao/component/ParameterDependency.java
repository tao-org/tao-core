package ro.cs.tao.component;

import ro.cs.tao.component.enums.Condition;
import ro.cs.tao.component.enums.DependencyType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The {@code ParameterDependency} class represents a dependency between parameters,
 * specifying the type of dependency, the parameter being referenced, the condition applied,
 * the expected value(s), and optionally a set of allowed values.
 *
 * This class is primarily used to enforce constraints or relationships between parameters
 * that need to follow a specific condition or match specific values for logical consistency
 * or validation purposes.
 */
public class ParameterDependency {
    private DependencyType dependencyType;
    private String referencedParameterId;
    private Condition condition;
    private String expectedValue;
    private String allowedValues;

    public ParameterDependency() { }

    public ParameterDependency(DependencyType type, String referencedParameterId, Condition condition, String... expectedValue) {
        this.dependencyType = type;
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

    /**
     * Retrieves the dependency type associated with this instance.
     *
     * @return the DependencyType representing the type of dependency
     */
    public DependencyType getDependencyType() { return dependencyType; }
    public void setDependencyType(DependencyType dependencyType) { this.dependencyType = dependencyType; }

    /**
     * Retrieves the identifier of the referenced parameter.
     *
     * @return a string representing the identifier of the referenced parameter
     */
    public String getReferencedParameterId() { return referencedParameterId; }
    public void setReferencedParameterId(String referencedParameterId) { this.referencedParameterId = referencedParameterId; }

    /**
     * Retrieves the condition associated with this instance.
     *
     * @return the {@link Condition} representing the condition in the dependency
     */
    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    /**
     * Retrieves the expected value associated with this instance.
     *
     * @return a string representing the expected value
     */
    public String getExpectedValue() { return expectedValue; }
    public void setExpectedValue(String expectedValue) { this.expectedValue = expectedValue; }

    /**
     * Retrieves the allowed values associated with this instance.
     *
     * @return a string representing the allowed values
     */
    public String getAllowedValues() { return allowedValues; }
    public void setAllowedValues(String allowedValues) { this.allowedValues = allowedValues; }

    /**
     * Retrieves the list of allowed values associated with this instance.
     * Parses the stored string of allowed values (if not null) into a list by splitting it on commas.
     *
     * @return a list of strings representing the allowed values, or null if no allowed values are defined
     */
    public List<String> allowedValues() { return allowedValues != null ? Arrays.stream(allowedValues.split(",")).collect(Collectors.toList()) : null; }

    /**
     * Generates a set of expected values based on the current condition and expectedValue of the instance.
     * If the expectedValue is null, null is returned. For conditions of type IN or NOTIN,
     * the expectedValue is split by commas and converted into a set. For other conditions,
     * a single-element set containing the expectedValue is returned.
     *
     * @return a set of expected values, or null if the expectedValue is null
     */
    public Set<String> expectedValues() {
        if (this.expectedValue == null) {
            return null;
        }
        Set<String> values;
        if (condition == Condition.IN || condition == Condition.NOTIN) {
            values = Arrays.stream(this.expectedValue.split(",")).collect(Collectors.toSet());
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
        return dependencyType == that.dependencyType && referencedParameterId.equals(that.referencedParameterId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dependencyType, referencedParameterId);
    }
}
