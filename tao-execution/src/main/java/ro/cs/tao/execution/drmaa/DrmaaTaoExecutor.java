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
package ro.cs.tao.execution.drmaa;

import org.apache.commons.lang3.SystemUtils;
import org.ggf.drmaa.*;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.execution.Constants;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.local.DefaultSession;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.topology.NodeDescription;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cosmin Cara
 */
public class DrmaaTaoExecutor extends Executor<ProcessingExecutionTask> {
    private static final String DOCKER_BIND_MOUNT_CONFIG_KEY  = "tao.docker.bind_mount";
    private static final String FORCE_MEMORY_REQUIREMENTS_KEY = "tao.force.memory.requirements";
    private Session session;
    private boolean useDockerForExecution;
    private boolean forceMemoryRequirements;

    @Override
    public void initialize() throws ExecutionException {
        session = org.ggf.drmaa.SessionFactory.getFactory().getSession();
        try {
            session.init(null);
            super.initialize();
            useDockerForExecution = !SystemUtils.IS_OS_WINDOWS;
            forceMemoryRequirements = Boolean.parseBoolean(ConfigurationManager.getInstance().getValue(FORCE_MEMORY_REQUIREMENTS_KEY,
                                                                                                       "false"));
        } catch (DrmaaException e) {
            isInitialized = false;
            throw new ExecutionException("Error initiating DRMAA session", e);
        }
    }

    @Override
    public void close() throws ExecutionException {
        super.close();
        try {
            session.exit();
        } catch (DrmaaException e) {
            logger.severe(e.getMessage());
        }
    }

    @Override
    public void execute(ProcessingExecutionTask task) throws ExecutionException  {
        try {
            JobTemplate jt = createJobTemplate(task);

            String id = session.runJob(jt);
            if (id == null) {
                throw new ExecutionException(String.format("Unable to run job (id null) for task %s", task.getId()));
            }

            session.deleteJobTemplate(jt);

            task.setResourceId(id);
            task.setStartTime(LocalDateTime.now());
            List<NodeDescription> hosts = persistenceManager.getNodes();
            if (hosts.size() == 1) {
                task.setExecutionNodeHostName(hosts.get(0).getId());
            }
            changeTaskStatus(task,
                             session instanceof DefaultSession ? ExecutionStatus.RUNNING : ExecutionStatus.QUEUED_ACTIVE,
                             true);
            logger.fine(String.format("Succesfully submitted task with id %s [session %s]", task.getId(), id));
        } catch (DrmaaException | InternalException | PersistenceException e) {
            logger.severe(String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage()));
            throw new ExecutionException("Error executing DRMAA session operation", e);
        }
    }

    @Override
    public void stop(ProcessingExecutionTask task)  throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.TERMINATE);
            markTaskFinished(task, ExecutionStatus.CANCELLED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session terminate for task with id " + task.getId(), e);
        }
    }

    @Override
    public void suspend(ProcessingExecutionTask task) throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.SUSPEND);
            changeTaskStatus(task, ExecutionStatus.SUSPENDED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session suspend for task with id " + task.getId(), e);
        }
    }

    @Override
    public void resume(ProcessingExecutionTask task) throws ExecutionException {
        try {
            session.control(task.getResourceId(), Session.RESUME);
            changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session resume for task with id " + task.getId(), e);
        }
    }

    @Override
    public void monitorExecutions() {
        if(!isInitialized) {
            return;
        }
        List<ExecutionTask> tasks = persistenceManager.getExecutingTasks();
        if (tasks != null) {
            // For each job, get its status from DRMAA
            for (ExecutionTask task : tasks) {
                try {
                    int jobStatus = session.getJobProgramStatus(task.getResourceId());
                    switch (jobStatus) {
                        case Session.SYSTEM_ON_HOLD:
                        case Session.USER_ON_HOLD:
                        case Session.USER_SYSTEM_ON_HOLD:
                        case Session.SYSTEM_SUSPENDED:
                        case Session.USER_SUSPENDED:
                        case Session.USER_SYSTEM_SUSPENDED:
                        case Session.UNDETERMINED:
                        case Session.QUEUED_ACTIVE:
                            // nothing to do
                            break;
                        case Session.RUNNING:
                            try {
                                persistenceManager.updateTaskStatus(task, ExecutionStatus.RUNNING);
                            } catch (PersistenceException e) {
                                logger.severe(String.format("Status update for task %s failed. Reason: %s",
                                                            task.getId(), e.getMessage()));
                            }
                            break;
                        case Session.DONE:
                            // Just mark the task as finished with success status
                            if (task.getExecutionStatus() == ExecutionStatus.RUNNING) {
                                // Only a running task can complete
                                markTaskFinished(task, ExecutionStatus.DONE);
                            }
                            logger.fine(String.format("Task %s DONE", task.getId()));
                            break;
                        case Session.FAILED:
                            // Just mark the task as finished with failed status
                            markTaskFinished(task, ExecutionStatus.FAILED);
                            String error;
                            if (session instanceof DefaultSession) {
                                error = ((DefaultSession) session).getJobOutput(task.getResourceId());
                            } else {
                                error = "n/a";
                            }
                            logger.warning(String.format("Task %s FAILED. Process output: %s", task.getId(), error));
                            break;
                    }
                } catch (DrmaaException | InternalException e) {
                    logger.severe(String.format("%s: Cannot get the status for the task %s [%s]",
                                                e.getClass().getName(), task.getId(), e.getMessage()));
                    markTaskFinished(task, ExecutionStatus.FAILED);
                }
            }
        }
    }

    @Override
    public String defaultId() { return "DRMAAExecutor"; }

    private JobTemplate createJobTemplate(ProcessingExecutionTask task) throws DrmaaException, PersistenceException {
        // Get from the component the execution command
        Container container;
        String[] pArgs = null;
        ProcessingComponent component = task.getComponent();
        container = persistenceManager.getContainerById(component.getContainerId());
        Application app = container.getApplications()
                .stream()
                .filter(a -> component.getId().endsWith(a.getName().toLowerCase()))
                .findFirst().orElse(null);
        if (useDockerForExecution) {
            String location = task.getComponent().getFileLocation();
            if (!Paths.get(location).isAbsolute()) {
                task.getComponent().setExpandedFileLocation(Paths.get(container.getApplicationPath(), location).toString());
            }
        } else {
            if (app != null && app.hasParallelFlag()) {
                Class<?> type = app.parallelArgumentType();
                if (type.isAssignableFrom(Integer.class)) {
                    pArgs = app.parallelArguments(Integer.class, Runtime.getRuntime().availableProcessors() / 2);
                } else if (type.isAssignableFrom(Long.class)) {
                    pArgs = app.parallelArguments(Integer.class, Runtime.getRuntime().availableProcessors() / 2);
                } else {
                    pArgs = app.parallelArguments(Boolean.class, true);
                }
            }
            container = null;
        }
        String executionCmd = task.buildExecutionCommand();
        if (pArgs != null) {
            int idx = executionCmd.indexOf('\n');
            executionCmd = executionCmd.substring(0, idx) + "\n" + String.join("\n", pArgs) +
                    executionCmd.substring(idx);
        }
        JobTemplate jt = session.createJobTemplate();
        if (jt == null) {
            throw new ExecutionException("Error creating job template from the session!");
        }
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE) && app != null && forceMemoryRequirements) {
                job.setAttribute(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE, app.getMemoryRequirements());
            }
        }
        List<String> argsList = new ArrayList<>();
        String cmd;

        // split the execution command but preserving the entities between double quotes
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(executionCmd);
        while (m.find()) {
            argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
        }

        if (container != null) {
            cmd = "docker";
            String dockerBindMount = ConfigurationManager.getInstance().getValue(DOCKER_BIND_MOUNT_CONFIG_KEY).trim();
            List<String> dockerArgsList = new ArrayList<String>() {{
                add("run");
                add("-i");      // Keep STDIN open even if not attached
                //add("-t");      // Allocate a pseudo-TTY
                add("--rm");    // Automatically remove the container when it exits
                add("--volume-driver");    // Automatically remove the container when it exits
                add("cifs");    // Automatically remove the container when it exits
                add("-v");      // Bind mount a volume
                add(dockerBindMount);
            }};
            dockerArgsList.add(container.getId());
            dockerArgsList.addAll(argsList);
            argsList = dockerArgsList;
        } else {
            cmd = argsList.remove(0);
        }
        jt.setRemoteCommand(cmd);
        jt.setArgs(argsList);
        logger.fine(String.format("[Task %s ]: %s %s", String.valueOf(task.getId()), cmd, String.join(" ", argsList)));
        return jt;
    }
}
