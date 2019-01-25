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

import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.Optional;
import java.util.Set;

/**
 * Manager class for executions. It is responsible with:
 * 1) the creation of the proper execution for a task, depending on its type, and
 * 2) the control of the execution of a task.
 *
 * @author Cosmin Udroiu
 */
public class ExecutionsManager {

    private static ExecutionsManager instance = new ExecutionsManager();
    private Set<Executor> services;
    private ExecutionsManager() {
        ServiceRegistry<Executor> registry = ServiceRegistryManager.getInstance().getServiceRegistry(Executor.class);
        services = registry.getServices();
        services.forEach(Executor::initialize);
    }

    public static ExecutionsManager getInstance() {
        return instance;
    }

    public void execute(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.execute(task);
    }

    public void stop(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.stop(task);
    }

    public void suspend(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.suspend(task);
    }

    public void resume(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.resume(task);
    }

    public Set<Executor> getRegisteredExecutors() { return this.services; }

    private Executor getExecutor(ExecutionTask task) {
        Optional<Executor> optional;
        if (task instanceof ProcessingExecutionTask) {
            optional = services.stream()
                    .filter(x -> x.supports(((ProcessingExecutionTask) task).getComponent()))
                    .findFirst();
        } else {
            optional = services.stream()
                    .filter(x -> x.supports(((DataSourceExecutionTask) task).getComponent()))
                    .findFirst();
        }
        if(optional.isPresent()) {
            return optional.get();
        } else {
            throw new ExecutionException("The component does not have an associated executor!");
        }
    }
}
