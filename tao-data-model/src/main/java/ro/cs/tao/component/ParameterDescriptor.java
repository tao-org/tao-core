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
package ro.cs.tao.component;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.enums.Condition;
import ro.cs.tao.component.enums.DependencyType;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.component.validation.*;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.utils.StringUtilities;

import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Complete descriptor of a component parameter.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "parameter")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonSubTypes({ @JsonSubTypes.Type(value = ParameterDescriptor.class), @JsonSubTypes.Type(value = TemplateParameterDescriptor.class)})
public class ParameterDescriptor extends StringIdentifiable {
    private String name;
    private ParameterType type;
    JavaType dataType;
    private String defaultValue;
    private String description;
    private String label;
    private String unit;
    private String[] valueSet;
    private String format;
    private boolean notNull;
    private Validator customValidator;
    private Validator validator;
    private List<ParameterDependency> dependencies;
    private ParameterExpansionRule expansionRule;

    public ParameterDescriptor() { super(); }

    @JsonCreator
    public ParameterDescriptor(@JsonProperty("id") String id,
                               @JsonProperty("name") String name,
                               @JsonProperty("type") ParameterType type,
                               @JsonProperty("dataType") JavaType dataType,
                               @JsonProperty("defaultValue") String defaultValue,
                               @JsonProperty("description") String description,
                               @JsonProperty("label") String label,
                               @JsonProperty("unit") String unit,
                               @JsonProperty("valueSet") String[] valueSet,
                               @JsonProperty("format") String format,
                               @JsonProperty("expansionRule") ParameterExpansionRule rule,
                               @JsonProperty("notNull") boolean notNull) {
        super(id);
        this.name = name;
        this.type = type;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.label = label;
        this.valueSet = valueSet;
        this.format = format;
        this.expansionRule = rule;
        this.notNull = notNull;
    }

    public ParameterDescriptor(String id, String name, ParameterType type, Class<?> dataType, String defaultValue,
                               String description, String label, String unit, String[] valueSet, String format,
                               boolean notNull, ParameterExpansionRule expansionRule) {
        super(id);
        this.name = name;
        this.type = type;
        this.dataType = EnumUtils.getEnumConstantByValue(JavaType.class, dataType);
        this.defaultValue = defaultValue;
        this.description = description;
        this.label = label;
        this.unit = unit;
        this.valueSet = valueSet;
        this.format = format;
        this.notNull = notNull;
        this.expansionRule = expansionRule;
    }

    public ParameterDescriptor(String id) {
        super(id);
    }

    public String getName() { return name; }
    public void setName(String value) { this.name = value; }

    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    public Class<?> getDataType() {
        return dataType != null ? dataType.value() : null;
    }

    public void setDataType(Class<?> dataType) {
        if (dataType != null) {
            // first try to match by simple types
            this.dataType = EnumUtils.getEnumConstantByValue(JavaType.class, dataType);
            if (this.dataType == null) {
                // next try with a java class name
                this.dataType = EnumUtils.getEnumConstantByFriendlyName(JavaType.class, dataType.getName());
            }
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String[] getValueSet() {
        return valueSet;
    }

    public void setValueSet(String[] valueSet) {
        this.valueSet = valueSet;
    }

    /**
     * For a regular parameter, the format represents the string representation of the parameter (e.g. the date format).
     * For a template parameter, the format represents the type of the template of the parameter.
     *
     */
    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(boolean notNull) {
        this.notNull = notNull;
    }

    public String typeFriendlyName() { return type != null ? type.friendlyName() : null; }

    public Validator getValidator() { return customValidator; }

    public void setValidator(Validator customValidator) { this.customValidator = customValidator; }

    public void validate(Object value) throws ValidationException {
        createValidator().validate(this, value);
    }

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

    public ParameterExpansionRule getExpansionRule() { return expansionRule; }
    public void setExpansionRule(ParameterExpansionRule expansionRule) {
        this.expansionRule = expansionRule;
        /*if (this.expansionRule != null) {
            this.expansionRule.setParameterId(this.id);
        }*/
    }

    @Override
    public String defaultId() { return "NewParameter"; }

    @Override
    public ParameterDescriptor clone() throws CloneNotSupportedException {
        ParameterDescriptor clone = (ParameterDescriptor) super.clone();
        clone.id = UUID.randomUUID().toString();
        clone.valueSet = Arrays.copyOf(this.valueSet, this.valueSet.length);
        if (this.dependencies != null) {
            clone.dependencies = new ArrayList<>();
            clone.dependencies.addAll(this.dependencies);
        }
        clone.expansionRule = this.expansionRule;
        return clone;
    }

    public String expandValues(Object values) {
        if (this.type == null || values == null || !values.getClass().isArray()) {
            return values != null ? String.valueOf(values) : null;
        }
        if (this.expansionRule == null) {
            this.expansionRule = new ParameterExpansionRule();
            //this.expansionRule.setId(this.id);
            this.expansionRule.setJoinValues(true);
            this.expansionRule.setSeparator(" ");
        }
        final StringBuilder builder = new StringBuilder();
        final int length = Array.getLength(values);
        final String separator = this.expansionRule.getSeparator();
        final boolean joinValues = this.expansionRule.isJoinValues();
        for (int i = 0; i < length; i++) {
            if (i > 0 && !joinValues) {
                builder.append("-")
                       .append(StringUtilities.isGUID(this.id) ? this.label : this.id)
                       .append(this.expansionRule.getSeparator());
            }
            builder.append(Array.get(values, i)).append(separator);
        }
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ParameterDescriptor)) return false;
        if (!super.equals(o)) return false;
        ParameterDescriptor that = (ParameterDescriptor) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name);
    }

    public JavaType javaType() { return this.dataType; }

    protected Validator createValidator() {
        if (this.validator == null) {
            List<Validator> validators = new ArrayList<>(3);

            if (this.notNull) {
                validators.add(ValidatorRegistry.INSTANCE.getValidator(NotNullValidator.class));
            }
            validators.add(ValidatorRegistry.INSTANCE.getValidator(TypeValidator.class));
            if (this.notNull) {
                validators.add(ValidatorRegistry.INSTANCE.getValidator(NotNullValidator.class));
            }
            if (this.valueSet != null) {
                validators.add(ValidatorRegistry.INSTANCE.getValidator(ValueSetValidator.class));
            }
            if (this.customValidator != null) {
                validators.add(this.customValidator);
            }
            this.validator = validators.size() > 1 ?
                    new CompositeValidator(validators) :
                    validators.get(0);
        }
        return this.validator;
    }
}
