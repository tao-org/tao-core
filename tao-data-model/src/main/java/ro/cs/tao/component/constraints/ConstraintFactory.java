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
package ro.cs.tao.component.constraints;

import org.reflections.Reflections;

import java.util.*;

/**
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
                cache.put(className, (IOConstraint) Class.forName(className).newInstance());
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
