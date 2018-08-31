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
package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Validator that checks that the given value is of the type defined in the parameter descriptor.
 *
 * @author Cosmin Cara
 */
@XmlRootElement
public class TypeValidator extends Validator {

    TypeValidator() { }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (parameter == null) {
            throw new ValidationException("Cannot validate a null reference");
        }
        final Class<?> dataType = parameter.getDataType();
        if (!isAssignableFrom(dataType, value)) {
            throw new ValidationException(String.format("Value for [%s] must be of type %s",
                                                        parameter.getName(),
                                                        dataType.getSimpleName()));
        }
    }

    private boolean isAssignableFrom(Class<?> type, Object value) {
        if (value == null) {
            return !type.isPrimitive();
        }
        final Class<?> valueType = value.getClass();
        return type.isAssignableFrom(valueType)
                || type.isPrimitive()
                && (type.equals(Boolean.TYPE) && valueType.equals(Boolean.class)
                || type.equals(Character.TYPE) && valueType.equals(Character.class)
                || type.equals(Byte.TYPE) && valueType.equals(Byte.class)
                || type.equals(Short.TYPE) && valueType.equals(Short.class)
                || type.equals(Integer.TYPE) && valueType.equals(Integer.class)
                || type.equals(Long.TYPE) && valueType.equals(Long.class)
                || type.equals(Float.TYPE) && valueType.equals(Float.class)
                || type.equals(Double.TYPE) && valueType.equals(Double.class));
    }
}
