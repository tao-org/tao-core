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
package ro.cs.tao.spi;

import java.util.List;
import java.util.Set;

/**
 * A registry for services of a specific type.
 *
 * @param <T> The service type. All services are instances of that type.
 * @since 0.6
 */
public interface ServiceRegistry<T> {

    /**
     * Gets the service type. All services in this registry are instances of this type.
     *
     * @return The service type.
     */
    Class<T> getServiceType();

    /**
     * Gets all registered services.
     *
     * @return A set of all services.
     */
    Set<T> getServices();

    /**
     * Gets registered services passing the given filter.
     *
     * @return A set of all services.
     */
    Set<T> getServices(Filter filter);

    /**
     * Gets a registered service instance for the given class.
     *
     * @param tClass The class of the service.
     * @return The service instance or {@code null} if no such exists.
     */
    T getService(Class<? extends T> tClass);

    /**
     * Gets a registered service instance for the given class name.
     *
     * @param className The name of the service's class.
     * @return The service instance or {@code null} if no such exists.
     */
    T getService(String className);

    /**
     * Adds a new service to this registry. The method will automatically remove
     * an already registered service of the same type. If the registry changes
     * due to a call of this method, a change event will be fired.
     *
     * @param service The service to be added.
     * @return {@code true} if the service has been added.
     */
    boolean addService(T service);

    /**
     * Removes an existing service from this registry. If the registry changes
     * due to a call of this method, a change event will be fired.
     *
     * @param service The service to be removed.
     * @return {@code true} if the service has been removed.
     */
    boolean removeService(T service);

    /**
     * @return The list of registry listeners.
     */
    List<ServiceRegistryListener<T>> getListeners();

    /**
     * Adds a new registry listener.
     *
     * @param listener The registry listener to be added.
     */
    void addListener(ServiceRegistryListener<T> listener);

    /**
     * Removes an existing registry listener.
     *
     * @param listener The registry listener to be removed.
     */
    void removeListener(ServiceRegistryListener<T> listener);
    
    /** {@collect.stats}
     * A simple filter interface used by 
     * <code>ServiceRegistry.getServiceProviders</code> to select 
     * providers matching an arbitrary criterion.  Classes that 
     * implement this interface should be defined in order to make use 
     * of the <code>getServiceProviders</code> method of 
     * <code>ServiceRegistry</code> that takes a <code>Filter</code>. 
     * 
     * @see ServiceRegistry#getServiceProviders(Class, ServiceRegistry.Filter, boolean) 
     */ 
    interface Filter { 
 
        /** {@collect.stats}
         * Returns <code>true</code> if the given 
         * <code>provider</code> object matches the criterion defined 
         * by this <code>Filter</code>. 
         * 
         * @param provider a service provider <code>Object</code>. 
         * 
         * @return true if the provider matches the criterion. 
         */ 
        boolean filter(Object provider); 
    } 
}
