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

import ro.cs.tao.lifecycle.ComponentLifeCycle;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * This class is responsible with the invocation of (possible) component activator invocations.
 *
 * @author Cosmin Cara
 */
public class LifeCycleProcessor {

    private static final ExecutorService executor;
    private static final List<ComponentLifeCycle> detectedComponents;

    static {
        detectedComponents = new ArrayList<>();
        executor = new NamedThreadPoolExecutor("lifecycle-thread", 1);//Executors.newSingleThreadExecutor();
        Runtime.getRuntime().addShutdownHook(new Thread(LifeCycleProcessor::onShutdown));
    }

    public static void activate() {
        final ServiceRegistry<ComponentLifeCycle> registry = ServiceRegistryManager.getInstance().getServiceRegistry(ComponentLifeCycle.class);
        if (registry != null) {
            detectedComponents.addAll(registry.getServices());
        }
        onStartUp();
    }

    private static void onStartUp() {
        detectedComponents.forEach(c -> {
            executor.submit(() -> {
                try {
                    c.onStartUp();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void onShutdown() {
        detectedComponents.forEach(c -> {
            executor.submit(() -> {
                try {
                    c.onShutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }
}
