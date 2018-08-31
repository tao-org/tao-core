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
package ro.cs.tao.spi;

import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;

/**
 * Resource locators are service providers used collect resources across multiple code bases in multi-module environments.
 *
 * @author Norman Fomferra
 * @since Ceres 2.0
 */
public abstract class ResourceLocator {

    /**
     * Gets all resources from all registered {@link ResourceLocator} services for the given name.
     * @param name The resource name
     * @return The collection of all resources found
     */
    public static Collection<Path> getResources(String name) {
        ServiceLoader<ResourceLocator> providers = ServiceLoader.load(ResourceLocator.class);
        ResourceLocator resourceLocator = null;
        for (ResourceLocator provider : providers) {
            if (!provider.getClass().equals(DefaultResourceLocator.class)) {
                resourceLocator = provider;
                break;
            } else if (resourceLocator == null) {
                resourceLocator = provider;
            }
        }
        if (resourceLocator == null) {
            resourceLocator = new DefaultResourceLocator();
        }
        return resourceLocator.locateResources(name);
    }

    /**
     * Locates all resources with the given name.
     * @param name The resource name
     * @return The collection of all resources located
     */
    public abstract Collection<Path> locateResources(String name);
}
