/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.component;

import ro.cs.tao.component.validation.CompositeValidator;
import ro.cs.tao.component.validation.NotEmptyValidator;
import ro.cs.tao.component.validation.NotNullValidator;
import ro.cs.tao.component.validation.TypeValidator;
import ro.cs.tao.component.validation.ValidationException;
import ro.cs.tao.component.validation.Validator;
import ro.cs.tao.component.validation.ValidatorRegistry;
import ro.cs.tao.component.validation.ValueSetValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class ParameterDescriptor extends Identifiable {
    private ParameterType type;
    private Class<?> dataType;
    private String defaultValue;
    private String description;
    private String label;
    private String unit;
    private String[] valueSet;
    private String format;
    private Boolean notNull;
    private Boolean notEmpty;
    private Validator customValidator;
    private Validator validator;

    public ParameterDescriptor() { super(); }

    private ParameterDescriptor(String name, ParameterType type, Class<?> dataType, String defaultValue,
                                String description, String label, String unit, String[] valueSet,
                                String format, Boolean notNull, Boolean notEmpty) {
        super(name);
        this.type = type;
        this.dataType = dataType;
        this.defaultValue = defaultValue;
        this.description = description;
        this.label = label;
        this.unit = unit;
        this.valueSet = valueSet;
        this.format = format;
        this.notNull = notNull;
        this.notEmpty = notEmpty;
    }

    public ParameterDescriptor(String name) {
        super(name);
    }

    public ParameterType getType() {
        return type;
    }

    public void setType(ParameterType type) {
        this.type = type;
    }

    public Class<?> getDataType() {
        return dataType;
    }

    public void setDataType(Class<?> dataType) {
        this.dataType = dataType;
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

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Boolean isNotNull() {
        return notNull;
    }

    public void setNotNull(Boolean notNull) {
        this.notNull = notNull;
    }

    public Boolean isNotEmpty() {
        return notEmpty;
    }

    public void setNotEmpty(Boolean notEmpty) {
        this.notEmpty = notEmpty;
    }

    public void setValidator(Validator customValidator) { this.customValidator = customValidator; }

    public void validate(Object value) throws ValidationException {
        createValidator().validate(this, value);
    }

    @Override
    public String defaultName() {
        return "NewParameter";
    }

    @Override
    public ParameterDescriptor clone() throws CloneNotSupportedException {
        ParameterDescriptor clone = (ParameterDescriptor) super.clone();
        clone.name = defaultName();
        clone.valueSet = Arrays.copyOf(this.valueSet, this.valueSet.length);
        return clone;
    }

    protected Validator createValidator() {
        if (this.validator == null) {
            List<Validator> validators = new ArrayList<>(3);

            if (this.notNull) {
                validators.add(ValidatorRegistry.INSTANCE.getValidator(NotNullValidator.class));
            }
            validators.add(ValidatorRegistry.INSTANCE.getValidator(TypeValidator.class));
            if (this.notEmpty) {
                validators.add(ValidatorRegistry.INSTANCE.getValidator(NotEmptyValidator.class));
            }
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
