/*
 * Copyright (C) 2017 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
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
