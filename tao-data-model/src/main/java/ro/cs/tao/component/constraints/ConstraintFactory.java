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
import ro.cs.tao.persistence.NamingRuleProvider;

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
    private static NamingRuleProvider namingRuleProvider;

    static {
        cache = new HashMap<>();
        aliases = new HashMap<>();
        initAliases();
    }

    /**
     * Creates or retrieves an instance of an {@code IOConstraint} based on the specified name.
     * If the constraint corresponding to the name is not already cached, it dynamically loads
     * the class, instantiates it, and stores it in a cache for future use.
     *
     * @param name the name of the desired constraint. If an alias is defined for this name, the alias is utilized.
     * @return an instance of the specified constraint if successfully created or retrieved; otherwise, {@code null} if an error occurs.
     */
    public static IOConstraint create(String name) {
        String className = aliases.getOrDefault(name, name);
        try {
            if (!cache.containsKey(className)) {
                Class<?> constraintClass = Class.forName(className);
                Constructor<?> constructor = constraintClass.getDeclaredConstructor();
                cache.put(className, (IOConstraint) constructor.newInstance());
            }
            return cache.get(className);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Creates or retrieves an instance of an {@code IOConstraint} based on the specified name and value.
     * If the constraint corresponding to the name is not already cached, it dynamically loads the class,
     * instantiates it using the value, and stores it in a cache for future use.
     *
     * @param name the name of the desired constraint. If an alias is defined for this name, the alias is used.
     * @param value the specific value to be associated with the constraint being created.
     * @return an instance of the specified constraint if successfully created or retrieved;
     *         otherwise, {@code null} if an error occurs during instantiation or retrieval.
     */
    public static IOConstraint create(String name, String value) {
        String className = aliases.getOrDefault(name, name);
        try {
            if (!cache.containsKey(className)) {
                if (className.equals("Specific sensor")) {
                    SpecificSensorConstraint constraint = new SpecificSensorConstraint(value);
                    if (namingRuleProvider != null) {
                        constraint.setSynonims(namingRuleProvider.getSynonims(value));
                    }
                    cache.put(className, constraint);
                } else {
                    Class<?> constraintClass = Class.forName(className);
                    Constructor<?> constructor;
                    try {
                        constructor = constraintClass.getDeclaredConstructor(String.class);
                        cache.put(className, (IOConstraint) constructor.newInstance(name));
                    } catch (NoSuchMethodException ignored) {
                        constructor = constraintClass.getDeclaredConstructor();
                        cache.put(className, (IOConstraint) constructor.newInstance());
                    }
                }
            }
            return cache.get(className);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves the list of all available constraint names.
     * The returned constraints represent the keys in the internal alias mapping.
     *
     * @return a list of constraint names available for use. The returned list is a new instance and modifications
     *         to it do not affect the internal state of the class.
     */
    public static List<String> getAvailableConstraints() {
        return new ArrayList<>(aliases.keySet());
    }

    /**
     * Sets the naming rule provider used to determine naming rules for constraints.
     *
     * @param provider the {@code NamingRuleProvider} instance to be set. This provider
     *                 is responsible for supplying naming rules and handling related operations.
     */
    public static void setNamingRuleProvider(NamingRuleProvider provider) {
        namingRuleProvider = provider;
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
