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
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.execution.Constants;
import ro.cs.tao.execution.ExecutionConfiguration;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.local.DefaultSession;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.execution.monitor.NodeManager.NodeData;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.BlockingQueueWorker;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Executor class that delegates the execution to a DRMAA session.
 * This implementation uses a blocking queue for tasks, to serialize them one by one to the DRMAA infrastructure.
 *
 * @author Cosmin Cara
 * @author Cosmin Udroiu
 */
public class DrmaaTaoExecutor extends Executor<ProcessingExecutionTask> {
    private static String masterHost;
    private Session session;
    private final BlockingQueue<Tuple<JobTemplate, ProcessingExecutionTask>> queue;
    private final BlockingQueueWorker<Tuple<JobTemplate, ProcessingExecutionTask>> queueWorker;

    public DrmaaTaoExecutor() {
        super();
        this.queue = new LinkedBlockingDeque<>();
        this.queueWorker = new BlockingQueueWorker<>(this.queue, this::executeImpl);
    }

    @Override
    public void initialize() throws ExecutionException {
        session = org.ggf.drmaa.SessionFactory.getFactory().getSession();
        try {
            session.init(null);
            super.initialize();
            masterHost = Inet4Address.getLocalHost().getHostName();
            this.queueWorker.start();
        } catch (UnknownHostException | DrmaaException e) {
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
            changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE, true);
            this.queue.put(new Tuple<>(jt, task));
            logger.finest(String.format("Task %s has been added to the wait queue", task.getId()));
        } catch (InterruptedException | DrmaaException | IOException e) {
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
        final List<ExecutionTask> tasks = persistenceManager.getExecutingTasks();
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
                            try {
                                persistenceManager.updateTaskStatus(task, ExecutionStatus.SUSPENDED);
                            } catch (PersistenceException e) {
                                logger.severe(String.format("Status update for task %s failed. Reason: %s",
                                                            task.getId(), e.getMessage()));
                            }
                            break;
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

    public String getDRMName() { return session != null ? session.serviceName() : "n/a"; }

    public String getDRMVersion() { return session != null ? session.getVersion().toString() : "n/a"; }

    private Void executeImpl(Tuple<JobTemplate, ProcessingExecutionTask> pair) {
        final JobTemplate jt = pair.getKeyOne();
        final ProcessingExecutionTask task = pair.getKeyTwo();
        logger.finest(String.format("Task %s has been dequeued for execution", task.getId()));
        try {
            String id = session.runJob(jt);
            session.deleteJobTemplate(jt);
            if (id == null) {
                logger.severe(String.format("Unable to run job (id null) for task %s", task.getId()));
                changeTaskStatus(task, ExecutionStatus.FAILED);
                return null;
            }
            task.setResourceId(id);
            task.setStartTime(LocalDateTime.now());
            if (jt instanceof JobTemplateExtension) {
                JobTemplateExtension job = (JobTemplateExtension) jt;
                if (job.hasAttribute(Constants.NODE_ATTRIBUTE)) {
                    task.setExecutionNodeHostName((String) job.getAttribute(Constants.NODE_ATTRIBUTE));
                }
            }
            changeTaskStatus(task,
                             session instanceof DefaultSession ? ExecutionStatus.RUNNING : ExecutionStatus.QUEUED_ACTIVE);
        } catch (DrmaaException | InternalException e) {
            logger.severe(String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage()));
        }
        return null;
    }

    private JobTemplate createJobTemplate(ProcessingExecutionTask task) throws DrmaaException, IOException {
        // Get from the component the execution command
        String[] pArgs = null;
        final ProcessingComponent component = task.getComponent();
        final Container container = persistenceManager.getContainerById(component.getContainerId());
        Application app = container.getApplications()
                .stream()
                .filter(a -> component.getId().endsWith(a.getName().toLowerCase()))
                .findFirst().orElse(null);
        final JobTemplate jt = session.createJobTemplate();
        if (jt == null) {
            throw new ExecutionException("Error creating job template from the session!");
        }
        final long memory = app != null ? app.getMemoryRequirements() : 0L;
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE) && app != null &&
                    ExecutionConfiguration.forceMemoryConstraint()) {
                job.setAttribute(Constants.MEMORY_REQUIREMENTS_ATTRIBUTE, memory);
            }
        }
        // the next call blocks until a node is available
        final NodeData nodeData = getAvailableNode(component.getParallelism(), memory);
        NodeDescription node = nodeData.getNode();
        if (node == null) {
            throw new TryLaterException("Cannot obtain an available node [null]");
        }
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(Constants.NODE_ATTRIBUTE)) {
                job.setAttribute(Constants.NODE_ATTRIBUTE, node.getId());
            }
        }
        task.setExecutionNodeHostName(node.getId());
        //final int cpu = Math.min(component.getParallelism(), node.getProcessorCount());
        final int cpu = nodeData.getCpu();
        final long mem = nodeData.getMemory();
        String location = task.getComponent().getFileLocation();
        if (!Paths.get(location).isAbsolute()) {
            String path = container.getApplicationPath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            path += location;
            task.getComponent().setExpandedFileLocation(path);
        }
        // if parallel flags are defined, use them
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
        // Assemble the regular execution command, as produced by the processing component after applying values
        String executionCmd = task.buildExecutionCommand();
        // Insert parallel arguments, if any
        if (pArgs != null) {
            int idx = executionCmd.indexOf('\n');
            executionCmd = executionCmd.substring(0, idx) + "\n" + String.join("\n", pArgs) +
                    executionCmd.substring(idx);
        }
        // Insert common parameters, if defined at container level
        final String commonParameters = container.getCommonParameters();
        if (commonParameters != null) {
            int idx = executionCmd.indexOf('\n');
            executionCmd = executionCmd.substring(0, idx) + "\n" + String.join("\n", commonParameters.split(" ")) +
                    executionCmd.substring(idx);
        }
        // Insert output format name, if supported by the container and defined in component or by user
        final String formatNameParameter = container.getFormatNameParameter();
        if (formatNameParameter != null) {
            final Set<String> formats = container.getFormat();
            // check if the format name was given by user
            Variable formatVariable = task.getOutputParameterValues().stream()
                    .filter(v -> "formatName".equals(v.getKey())).findFirst().orElse(null);
            if (formatVariable == null) {
                // then check if the format name was defined in the component
                TargetDescriptor target = component.getTargets().stream()
                                                    .filter(t -> t.getDataDescriptor().getFormatName() != null).findFirst().orElse(null);
                if (target != null) {
                    formatVariable = new Variable("formatName", target.getDataDescriptor().getFormatName());
                }
            }
            if (formatVariable != null && (formats == null || formats.contains(formatVariable.getValue()))) {
                int idx = executionCmd.indexOf('\n');
                executionCmd = executionCmd.substring(0, idx) + "\n" + String.join("\n", formatNameParameter, formatVariable.getValue()) +
                                executionCmd.substring(idx);
            }
        }
        List<String> argsList = new ArrayList<>();
        final String cmd;

        // split the execution command but preserving the entities between double quotes
        final Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(executionCmd);
        while (m.find()) {
            argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
        }

        if (ExecutionConfiguration.useDocker()) {
            cmd = "docker";
            final String dockerBindMount;
            if (task.getExecutionNodeHostName().equals(masterHost)) {
                dockerBindMount = ExecutionConfiguration.getDockerMasterBindMount();
            } else {
                dockerBindMount = ExecutionConfiguration.getDockerNodeBindMount();
            }
            final List<String> dockerArgsList = new ArrayList<String>() {{
                add("run");
                add("-i");              // Keep STDIN open even if not attached
                //add("-t");            // Allocate a pseudo-TTY
                add("--rm");            // Automatically remove the container when it exits
                add("--volume-driver"); // Automatically remove the container when it exits
                add("cifs");            // Automatically remove the container when it exits
                add("-v");              // Bind mount a volume
                add(dockerBindMount);
                if (cpu > 0) {
                    add(SystemUtils.IS_OS_WINDOWS ? "--cpus" : "--cpus");      // Enforce the number of CPUs to be used
                    add(String.valueOf(cpu));
                    task.setUsedCPU(cpu);
                }
                if (mem > 0) {
                    add("--memory");    // Enforce the memory limit to be used
                    // memory, if not 0, is expressed in MB
                    add(mem + "MB");
                    task.setUsedRAM((int) mem);
                }
            }};
            String value = ExecutionConfiguration.getDockerRegistry();
            if (value != null) {
                dockerArgsList.add(value + "/" + container.getName());
            } else {
                dockerArgsList.add(container.getId());
            }
            dockerArgsList.addAll(argsList);
            argsList = dockerArgsList;
        } else {
            cmd = argsList.get(0);
            argsList.remove(0);
        }
        jt.setRemoteCommand(cmd);
        jt.setArgs(argsList);
        logger.fine(String.format("[Task %s ]: %s %s", task.getId(), cmd, String.join(" ", argsList)));
        return jt;
    }

    private NodeData getAvailableNode(int cpus, long memory) throws DrmaaException {
    	NodeData nodeData = null;
        if (NodeManager.isAvailable() && NodeManager.getInstance() != null) {
        	nodeData = NodeManager.getInstance().getAvailableNode(cpus, memory, 0);
            logger.fine(String.format("Node [%s] was chosen for next execution", nodeData.getNode().getId()));
        }
        if (nodeData == null) {
            throw new DeniedByDrmException("No node was found for execution");
        }
        return nodeData;
    }
}
