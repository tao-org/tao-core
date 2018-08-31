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
import java.util.Arrays;

/**
 * Validator that checks if the given value belongs to the valueSet defined in the parameter descriptor.
 *
 * @author Cosmin Cara
 */
@XmlRootElement
public class ValueSetValidator extends Validator {

    ValueSetValidator() { }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (parameter == null) {
            throw new ValidationException("Cannot validate a null reference");
        }
        if (value != null && parameter.isNotNull()) {
            final String[] valueSet = parameter.getValueSet();
            if (valueSet != null && Arrays.stream(valueSet).noneMatch(v -> v.equals(value))) {
                throw new ValidationException(String.format("Value for [%s] is invalid.", parameter.getName()));
            }
        }
    }
}
