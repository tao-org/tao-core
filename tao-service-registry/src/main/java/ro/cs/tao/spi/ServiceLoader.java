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

import java.util.Iterator;
import java.util.ServiceConfigurationError;
import java.util.logging.Logger;

/**
 * <b>IMPORTANT NOTE:</b>
 * This class does not belong to the public API.
 * It is not intended to be used by clients.
 *
 * @author Marco Peters
 */
public class ServiceLoader {

    public static <T> void loadServices(ServiceRegistry<T> registry) {
        Iterable<T> iterable = loadServices(registry.getServiceType());
        final Iterator<T> iterator = iterable.iterator();

        //noinspection WhileLoopReplaceableByForEach
        while (iterator.hasNext()) {
            try {
                T next = iterator.next();
                registry.addService(next);
            } catch (ServiceConfigurationError e) {
                Logger.getLogger(ServiceLoader.class.getName()).severe(e.getMessage());
            }
        }
    }
    
    /**
     * Loads services from all <code>META-INF/services/</code> resources.
     *
     * @param serviceType the type of the service to be loaded.
     * @return the services of type <code>serviceType</code> found.
     */
    public static <S> Iterable<S> loadServices(Class<S> serviceType) {
        return java.util.ServiceLoader.load(serviceType);
    }

    /**
     * Loads services from all <code>META-INF/services/</code> resources.
     *
     * @param serviceType the type of the service to be loaded.
     * @param classLoader the class loader.
     * @return the services of type <code>serviceType</code> found.
     */
    public static <S> Iterable<S> loadServices(Class<S> serviceType, ClassLoader classLoader) {
        return java.util.ServiceLoader.load(serviceType, classLoader);
    }



}
