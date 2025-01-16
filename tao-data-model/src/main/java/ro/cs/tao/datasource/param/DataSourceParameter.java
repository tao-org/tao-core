/*
 * Copyright (C) 2018 CS ROMANIA
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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.ParameterDependency;
import ro.cs.tao.component.enums.Condition;
import ro.cs.tao.component.enums.DependencyType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Descriptor for a data source query parameter.
 *
 * @author Cosmin Cara
 */
public class DataSourceParameter {
    private final String name;
    private final String remoteName;
    private final String label;
    final JavaType type;
    private Object defaultValue;
    private final boolean required;
    private Object[] valueSet;
    private int order;
    private List<ParameterDependency> dependencies;

    public DataSourceParameter(String name, String remoteName, Class type, String label) {
        this(name, remoteName, type, null, false);
    }

    public DataSourceParameter(String name, String remoteName, Class type, String label, boolean required) {
        this(name, remoteName, type, label, null, required);
    }

    public DataSourceParameter(String name, String remoteName, Class type, String label, Object defaultValue) {
        this(name, remoteName, type, label, defaultValue, false);
    }

    public DataSourceParameter(String name, String remoteName, Class type, String label, Object defaultValue, boolean required) {
        this.name = name;
        this.remoteName = remoteName;
        this.label = label;
        this.type = EnumUtils.getEnumConstantByValue(JavaType.class, type);
        this.defaultValue = defaultValue;
        this.required = required;
    }

    public DataSourceParameter(String name, String remoteName, Class type, String label, Object defaultValue, boolean required, Object... valueSet) {
        this.name = name;
        this.remoteName = remoteName;
        this.label = label;
        this.type = EnumUtils.getEnumConstantByValue(JavaType.class, type);
        this.defaultValue = defaultValue;
        this.required = required;
        this.valueSet = valueSet;
    }

    @JsonCreator
    public DataSourceParameter(@JsonProperty("name") String name,
                               @JsonProperty("remoteName") String remoteName,
                               @JsonProperty("label") String label,
                               @JsonProperty("type") JavaType type,
                               @JsonProperty("defaultValue") Object defaultValue,
                               @JsonProperty("required") boolean required,
                               @JsonProperty("valueSet") Object[] valueSet,
                               @JsonProperty("order") int order) {
        this.name = name;
        this.remoteName = remoteName;
        this.label = label;
        this.type = type;
        this.defaultValue = defaultValue;
        this.required = required;
        this.valueSet = valueSet;
        this.order = order;
    }

    public String getName() { return name; }

    public String getRemoteName() { return remoteName; }

    public String getLabel() { return label; }

    public Class getType() { return type != null ? type.value() : null; }

    public boolean isRequired() { return required; }

    public Object getDefaultValue() { return defaultValue; }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Object[] getValueSet() { return valueSet; }

    public void setValueSet(Object[] valueSet) { this.valueSet = valueSet; }

    public int getOrder() { return order; }

    public void setOrder(int order) { this.order = order; }

    public List<ParameterDependency> getDependencies() { return dependencies; }

    public void setDependencies(List<ParameterDependency> dependencies) { this.dependencies = dependencies; }

    public void addDependency(DependencyType type, String parameterId, Condition condition, String... values) {
        if (this.dependencies == null) {
            this.dependencies = new ArrayList<>();
        }
        ParameterDependency dependency = new ParameterDependency(type, parameterId, condition, values);
        if (!this.dependencies.contains(dependency)) {
            this.dependencies.add(dependency);
        }
    }

    public String typeFriendlyName() { return type != null ? type.friendlyName() : null; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataSourceParameter that = (DataSourceParameter) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type);
    }
}
