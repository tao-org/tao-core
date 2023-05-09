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
package ro.cs.tao.datasource.beans;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.enums.ParameterType;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.serialization.GenericAdapter;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Simplified bean for describing a parameter.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dsParameter")
public class Parameter {
    private String name;
    private String type;
    private String value;
    private String[] valueSet;

    public Parameter() { }

    public Parameter(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public Parameter(String name, String type, String value, String[] valueSet) {
        this.name = name;
        this.type = type;
        this.value = value;
        this.valueSet = valueSet;
    }

    @XmlElement(name = "name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @XmlElement(name = "type")
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    @XmlElement(name = "value")
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }

    @XmlElement(name = "valueSet")
    public String[] getValueSet() { return valueSet; }
    public void setValueSet(String[] values) { this.valueSet = values; }

    public Object typedValue() throws Exception {
        return String.class.getName().equals(type) ? value :
                new GenericAdapter(type).marshal(value);
    }

    public static String[] stringValueSet(Object[] values) {
        if (values == null || values.length == 0) {
            return null;
        }
        List<String> stringValues = new ArrayList<>();
        for (Object value : values) {
            if (!"null".equals(value.toString())) {
                stringValues.add(value.toString());
            }
        }
        return stringValues.size() > 0 ? stringValues.toArray(new String[0]) : null;
    }

    public ParameterDescriptor toParameterDescriptor() {
        final ParameterDescriptor descriptor = new ParameterDescriptor();
        descriptor.setId(this.name);
        descriptor.setName(this.name);
        descriptor.setDefaultValue(this.value);
        descriptor.setValueSet(this.valueSet);
        descriptor.setType(ParameterType.REGULAR);
        JavaType jType = EnumUtils.getEnumConstantByFriendlyName(JavaType.class, this.type);
        if (jType == null) {
            jType = JavaType.STRING;
        }
        descriptor.setDataType(jType.value());
        descriptor.setDescription(this.name);
        return descriptor;
    }
}
