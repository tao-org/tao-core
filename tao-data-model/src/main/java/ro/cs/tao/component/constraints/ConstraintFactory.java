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
package ro.cs.tao.component.constraints;

import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Factory singletot for creating Constraint instances.
 * The constraint classes are detected via reflection by inspecting their @Constraint annotation.
 *
 * @author Cosmin Cara
 */
public final class ConstraintFactory {

    private static final Map<String, IOConstraint> cache;
    private static final Map<String, String> aliases;

    static {
        cache = new HashMap<>();
        aliases = new HashMap<>();
        initAliases();
    }

    public static IOConstraint create(String name) {
        String className = aliases.getOrDefault(name, name);
        try {
            if (!cache.containsKey(className)) {
                Class<?> constraintClass = className.equals("Same sensor") ? SpecificSensorConstraint.class : Class.forName(className);
                Constructor<?> constructor;
                try {
                    constructor = constraintClass.getDeclaredConstructor(String.class);
                    cache.put(className, (IOConstraint) constructor.newInstance(name));
                } catch (NoSuchMethodException ignored) {
                    constructor = constraintClass.getDeclaredConstructor();
                    cache.put(className, (IOConstraint) constructor.newInstance());
                }

            }
            return cache.get(className);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<String> getAvailableConstraints() {
        return new ArrayList<>(aliases.keySet());
    }

    private static void initAliases() {
        Reflections reflections = new Reflections("ro.cs.tao.component.constraints");
        final Set<Class<?>> annotatedTypes = reflections.getTypesAnnotatedWith(Constraint.class);
        annotatedTypes.forEach(type -> {
            String alias = type.getAnnotation(Constraint.class).name();
            aliases.put(alias, type.getName());
        });
    }

}
