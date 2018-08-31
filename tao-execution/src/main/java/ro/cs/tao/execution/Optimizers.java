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

package ro.cs.tao.execution;

import ro.cs.tao.component.RuntimeOptimizer;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager class for execution optimization plugins.
 *
 * @see RuntimeOptimizer interface
 * @author Cosmin Cara
 */
public class Optimizers {
    private static final Optimizers instance;
    private final List<RuntimeOptimizer> optimizers;

    static {
        instance = new Optimizers();
    }

    private Optimizers() {
        ServiceRegistry<RuntimeOptimizer> registry = ServiceRegistryManager.getInstance()
                .getServiceRegistry(RuntimeOptimizer.class);
        this.optimizers = new ArrayList<>(registry.getServices());
    }

    /**
     * Returns an execution optimization plugin, if any, for the given container.
     *
     * @param container The container identifier.
     */
    public static RuntimeOptimizer findOptimizer(String container) {
        return instance.optimizers.stream().filter(o -> o.isIntendedFor(container)).findFirst().orElse(null);
    }
}
