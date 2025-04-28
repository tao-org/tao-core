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
import ro.cs.tao.execution.model.*;
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

    /**
     * Retrieves the singleton instance of the ExecutionsManager class.
     *
     * @return the singleton instance of ExecutionsManager
     */
    public static ExecutionsManager getInstance() {
        return instance;
    }

    /**
     * Executes the specified {@link ExecutionTask} using a determined {@link Executor}.
     *
     * @param task the execution task to be processed
     */
    public void execute(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.execute(task);
    }

    /**
     * Stops the execution of the specified {@link ExecutionTask}.
     *
     * @param task the execution task to be stopped
     */
    public void stop(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.stop(task);
    }

    /**
     * Suspends the execution of the specified {@link ExecutionTask}.
     *
     * @param task the execution task to be suspended
     */
    public void suspend(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.suspend(task);
    }

    /**
     * Resumes the execution of the specified {@link ExecutionTask}.
     *
     * @param task the execution task to be resumed
     */
    public void resume(ExecutionTask task) {
        Executor executor = getExecutor(task);
        executor.resume(task);
    }

    /**
     * Retrieves the set of currently registered executors.
     *
     * @return a set of {@link Executor} objects representing the registered executors
     */
    public Set<Executor> getRegisteredExecutors() { return this.services; }

    /**
     * Retrieves the output of a job that is identified by the specified ID.
     *
     * @param id the unique identifier of the job whose output is to be retrieved
     * @return the output of the job as a String
     */
    public String getJobOutput(long id) {
        return DRMAAExecutor.getInstance().getJobOutput(String.valueOf(id));
    }

    /**
     * Registers a post-execution action to be performed after the specified {@link ExecutionTask} completes execution.
     *
     * @param task   the execution task for which the post-execution action is registered
     * @param action a {@link Callable} representing the post-execution action to be performed
     */
    public void registerPostExecuteAction(ExecutionTask task, Callable<Void> action) {
        postExecuteTaskActions.put(task.getId(), action);
    }

    /**
     * Executes the post-execution action associated with the specified {@link ExecutionTask}, if one is registered.
     * Once the action is executed, it is removed from the internal storage.
     * If an error occurs during the execution of the action, it is logged as a warning.
     *
     * @param task the execution task whose associated post-execution action is to be executed
     */
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
        } else if (task instanceof WMSExecutionTask) {
            optional = services.stream()
                               .filter(x -> x.supports(((WMSExecutionTask) task).getComponent()))
                               .findFirst();
        } else if (task instanceof ConditionExecutionTask) {
            optional = services.stream()
                               .filter(x -> x.supports(((ConditionExecutionTask) task).getComponent()))
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
