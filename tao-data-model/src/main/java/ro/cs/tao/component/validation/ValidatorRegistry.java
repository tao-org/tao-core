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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton factory class for registered validators.
 *
 * @author Cosmin Cara
 */
public enum ValidatorRegistry {
    INSTANCE;

    private final Map<Class<? extends Validator>, Validator> validators = new HashMap<Class<? extends Validator>, Validator>() {{
        put(NotNullValidator.class, new NotNullValidator());
        put(NotEmptyValidator.class, new NotEmptyValidator());
        put(TypeValidator.class, new TypeValidator());
        put(ValueSetValidator.class, new ValueSetValidator());
    }};

    public Validator getValidator(Class<? extends Validator> validatorClass) {
        return validators.get(validatorClass);
    }

    public void register(Class<? extends  Validator> validatorClass) {
        if (validatorClass != null) {
            if (CompositeValidator.class.equals(validatorClass)) {
                throw new IllegalArgumentException("CompositeValidator type is not registerable.");
            }
            if (!validators.containsKey(validatorClass)) {
                try {
                    final Constructor<? extends Validator> constructor = validatorClass.getConstructor();
                    validators.put(validatorClass, constructor.newInstance());
                } catch (NoSuchMethodException | IllegalAccessException
                        | InstantiationException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void unregister(Class<? extends  Validator> validatorClass) {
        if (validatorClass != null) {
            validators.remove(validatorClass);
        }
    }
}
