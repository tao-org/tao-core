package ro.cs.tao.datasource.param;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class Dependency<T extends Comparable, V extends Comparable> {
    private QueryParameter<T> dependant;
    private QueryParameter<V> dependent;
    private Map<Map.Entry<Condition, T>, V> dependentValues;

    public Dependency(QueryParameter<T> dependant, QueryParameter<V> dependent, Map<Map.Entry<Condition, T>, V> conditions) {
        this.dependant = dependant;
        this.dependent = dependent;
        this.dependentValues = conditions;
    }

    public QueryParameter<T> getDependant() { return dependant; }

    public QueryParameter<V> getDependent() { return dependent; }

    public V getDependentValue(T forValue) {
        V value = null;
        for (Map.Entry<Condition, T> entry : dependentValues.keySet()) {
            if (entry.getKey().evaluate(forValue, entry.getValue())) {
                value = dependentValues.get(entry);
                break;
            }
        }
        return value;
    }

}
