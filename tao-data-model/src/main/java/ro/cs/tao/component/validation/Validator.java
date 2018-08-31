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

import javax.xml.bind.annotation.XmlTransient;

/**
 * A validator ensures that the value given to a parameter respects some rules.
 *
 * @author Cosmin Cara
 */
@XmlTransient
public abstract class Validator {
    /**
     * Performs the validation of the given value.
     * @param parameter The parameter descriptor
     * @param value     The value to be validated.
     */
    public abstract void validate(ParameterDescriptor parameter, Object value) throws ValidationException;
}
