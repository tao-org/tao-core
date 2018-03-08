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
package ro.cs.tao.component.validation;

import ro.cs.tao.component.ParameterDescriptor;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public class CompositeValidator extends Validator {
    private List<Validator> internalValidators;

    public CompositeValidator(List<Validator> validators) {
        this.internalValidators = validators;
    }

    @Override
    public void validate(ParameterDescriptor parameter, Object value) throws ValidationException {
        if (this.internalValidators != null) {
            for (Validator validator : this.internalValidators) {
                validator.validate(parameter, value);
            }
        }
    }
}
