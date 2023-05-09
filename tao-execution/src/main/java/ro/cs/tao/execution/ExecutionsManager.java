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

import ro.cs.tao.execution.drmaa.DRMAAExecutor;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.execution.model.WPSExecutionTask;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;

/**
 * Manager class for executions. It is responsible with:
 * 1) the creation of the proper execution for a task, depending on its type, and
 * 2) the control of the execution of a task.
 *
 * @author Cosmin Udroiu
 */
public class ExecutionsManager {

    private static final ExecutionsManager instance = new ExecutionsManager();
    private final Set<Executor> services;
    private final Map<Long, Callable<Void>> postExecuteTaskActions;
    private ExecutionsManager() {
        ServiceRegistry<Executor> registry = ServiceRegistryManager.getInstance().getServiceRegistry(Executor.class);
        services = registry.getServices();
        services.forEach(Executor::initialize);
        postExecuteTaskActions = new HashMap<>();
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

    public String getJobOutput(long id) {
        return DRMAAExecutor.getInstance().getJobOutput(String.valueOf(id));
    }

    public void registerPostExecuteAction(ExecutionTask task, Callable<Void> action) {
        postExecuteTaskActions.put(task.getId(), action);
    }

    public void doPostExecuteAction(ExecutionTask task) {
        Callable<Void> action = postExecuteTaskActions.get(task.getId());
        if (action != null) {
            try {
                action.call();
                postExecuteTaskActions.remove(task.getId());
            } catch (Throwable e) {
                Logger.getLogger(ExecutionsManager.class.getName()).warning(e.getMessage());
            }
        }
    }

    private Executor getExecutor(ExecutionTask task) {
        Optional<Executor> optional;
        if (task instanceof ProcessingExecutionTask) {
            optional = services.stream()
                    .filter(x -> x.supports(((ProcessingExecutionTask) task).getComponent()))
                    .findFirst();
        } else if (task instanceof WPSExecutionTask) {
            optional = services.stream()
                    .filter(x -> x.supports(((WPSExecutionTask) task).getComponent()))
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
