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
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.DockerVolumeMap;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.drmaa.Environment;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.execution.DrmaaJobExtensions;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.callback.EndpointDescriptor;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.UserPrincipal;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.TopologyException;
import ro.cs.tao.utils.ExecutionUnitFormat;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.BlockingQueueWorker;
import ro.cs.tao.utils.executors.MemoryUnit;
import ro.cs.tao.utils.executors.container.ContainerCmdBuilder;
import ro.cs.tao.utils.executors.container.ContainerType;
import ro.cs.tao.utils.executors.container.ContainerUnit;

import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Executor class that delegates the execution to a DRMAA session.
 * This implementation uses a blocking queue for tasks, to serialize them one by one to the DRMAA infrastructure.
 *
 * @author Cosmin Cara
 * @author Cosmin Udroiu
 */
public class DRMAAExecutor extends Executor<ProcessingExecutionTask> {
    private static String masterHost;
    private static ContainerProvider containerProvider;
    private static DRMAAExecutor instance;
    private static String appId;
    //private Session session;
    private final EnumMap<Environment, Session> sessions;
    private final BlockingQueue<Tuple<JobTemplate, ProcessingExecutionTask>> queue;
    private final BlockingQueueWorker<Tuple<JobTemplate, ProcessingExecutionTask>> queueWorker;
    private final List<TaskListener> taskListeners;
    private final Map<String, Tuple<NodeDescription, AtomicLong>> hostMemoryRequests;

    /**
     * Sets the container provider for the {@code DRMAAExecutor}.
     * This allows the executor to manage and interact with the associated containers.
     *
     * @param provider the {@link ContainerProvider} instance to be set
     */
    public static void setContainerProvider(ContainerProvider provider) { containerProvider = provider; }

    /**
     * Sets the orchestrator id.
     * This allows multiple instances, from different machines, not to overlap on executing tasks.
     *
     * @param id    The orchestrator identifier
     */
    public static void setApplicationId(String id) { appId = id; }

    /**
     * Sets the number of workers that can process tasks in parallel within the queue.
     * Adjusting this value modifies the level of parallelism for the task execution.
     *
     * @param value the number of parallel workers to be set for task processing
     */
    public static void setQueueWorkers(int value) {
        if (instance != null) {
            instance.queueWorker.setParallelism(value);
        }
    }

    /**
     * Retrieves the singleton instance of the {@code DRMAAExecutor} class.
     * This instance is used to manage and execute tasks using the DRMAA (Distributed Resource Management Application API) framework.
     *
     * @return the singleton instance of {@code DRMAAExecutor}
     */
    public static DRMAAExecutor getInstance() { return instance; }

    public DRMAAExecutor() {
        super();
        if (taskProvider == null) {
            throw new ExecutionException("A TaskProvider is required to use this executor");
        }
        if (containerProvider == null) {
            throw new ExecutionException("A ContainerProvider is required to use this executor");
        }
        this.queue = new LinkedBlockingDeque<>();
        this.queueWorker = new BlockingQueueWorker<>(this.queue, this::executeImpl,
                                                     NodeManager.isAvailable() ? NodeManager.getInstance().getActiveNodesCount() : 1);
        final Set<TaskListener> services = ServiceRegistryManager.getInstance().getServiceRegistry(TaskListener.class).getServices();
        this.taskListeners = new ArrayList<>();
        if (services != null) {
            this.taskListeners.addAll(services);
        }
        this.sessions = new EnumMap<>(Environment.class);
        this.hostMemoryRequests = Collections.synchronizedMap(new HashMap<>());
        instance = this;
    }

    @Override
    public void initialize() throws ExecutionException {
        final Set<Environment> environments = SessionFactory.getEnvironments();
        if (environments == null) {
            throw new ExecutionException("DRMAA subsystem not initialized");
        }
        synchronized (isInitialized) {
            for (Environment env : environments) {
                try {
                    Session session = SessionFactory.getFactory(env).getSession();
                    session.init(null);
                    super.initialize();
                    masterHost = Inet4Address.getLocalHost().getHostName();
                    sessions.put(env, session);
                } catch (Exception e) {
                    logger.severe("Error initiating DRMAA " + env + " session: " + e.getMessage());
                }
            }
            isInitialized.set(sessions.size() > 0);
            if (isInitialized.get()) {
                this.queueWorker.start();
            }
        }
    }

    @Override
    public void close() throws ExecutionException {
        super.close();
        for (Session session : this.sessions.values()) {
            try {
                session.exit();
            } catch (DrmaaException e) {
                logger.severe(e.getMessage());
            }
        }
    }

    @Override
    public void execute(ProcessingExecutionTask task) throws ExecutionException  {
        try {
            final ExecutionJob job = task.getJob();
            /*final String userId = job.getUserId();
            if (job.getJobType() == JobType.EXECUTION && job.getEnvironment() == Environment.DEFAULT) {
                logger.fine("Trying to provision a worker node for user " + userId);
                NodeDescription node;
                if ((node = NodeManager.getInstance().createWorkerNode(userId)) != null) {
                    final String message = "Node " + node.getId() + " created";
                    logger.fine(message);
                    sendMessage(task, message);
                }
            }*/
            JobTemplate jt = createJobTemplate(task);
            changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE, true, ExecutionStatus.QUEUED_ACTIVE.friendlyName());
            this.queue.put(new Tuple<>(jt, task));
            logger.finest(String.format("Task %s has been added to the wait queue", task.getId()));
        } catch (InterruptedException | DrmaaException | IOException | PersistenceException | TopologyException e) {
            logger.severe(String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage()));
            throw new ExecutionException("Error executing DRMAA session operation", e);
        }
    }

    @Override
    public void stop(ProcessingExecutionTask task)  throws ExecutionException {
        try {
            if (task.getResourceId() != null) {
                final Session session = getSession(task.getJob().getEnvironment());
                session.control(task.getResourceId(), Session.TERMINATE);
                //session.control(task.getResourceId(), Session.TERMINATE);
            }
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session terminate for task with id " + task.getId(), e);
        } finally {
            markTaskFinished(task, ExecutionStatus.CANCELLED, ExecutionStatus.CANCELLED.friendlyName());
        }
    }

    @Override
    public void suspend(ProcessingExecutionTask task) throws ExecutionException {
        try {
            if (task.getResourceId() != null) {
                Session session = getSession(task.getJob().getEnvironment());
                session.control(task.getResourceId(), Session.SUSPEND);
            }
            changeTaskStatus(task, ExecutionStatus.SUSPENDED, ExecutionStatus.SUSPENDED.friendlyName());
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session suspend for task with id " + task.getId(), e);
        }
    }

    @Override
    public void resume(ProcessingExecutionTask task) throws ExecutionException {
        try {
            if (task.getResourceId() != null) {
                Session session = getSession(task.getJob().getEnvironment());
                session.control(task.getResourceId(), Session.RESUME);
            }
            changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE, "Resumed");
        } catch (DrmaaException e) {
            // maybe we got here because the task came from an interrupted job
            if (e instanceof NoActiveSessionException) {
                execute(task);
            } else {
                throw new ExecutionException("Error executing DRMAA session resume for task with id " + task.getId(), e);
            }
        }
    }

    @Override
    public void monitorExecutions() {
        if(!isInitialized.get()) {
            return;
        }
        final List<ExecutionTask> tasks = taskProvider.listExecuting(appId);
        if (tasks != null) {
            // For each job, get its status from DRMAA
            for (ExecutionTask task : tasks) {
                final String taskName = TaskUtilities.getTaskDescriptiveName(task);
                try {
                    Session session = getSession(task.getJob().getEnvironment());
                    int jobStatus = session.getJobProgramStatus(task.getResourceId());
                    logger.finest("DRMAA session returned " + jobStatus + " for task " + taskName);
                    final String userId = task.getJob().getUserId();
                    switch (jobStatus) {
                        case Session.SYSTEM_ON_HOLD:
                        case Session.USER_ON_HOLD:
                        case Session.USER_SYSTEM_ON_HOLD:
                        case Session.SYSTEM_SUSPENDED:
                        case Session.USER_SUSPENDED:
                        case Session.USER_SYSTEM_SUSPENDED:
                            decrementMemory(task.getExecutionNodeHostName(), task.getUsedRAM());
                            try {
                                notifyStatusListener(task, ExecutionStatus.SUSPENDED, ExecutionStatus.SUSPENDED.friendlyName());
                            } catch (Exception e) {
                                logger.severe(String.format("Status update for task %s failed. Reason: %s",
                                                            taskName, e.getMessage()));
                            }
                            String out = getJobOutput(task.getResourceId());
                            task.setLog(out);
                            if (task instanceof ProcessingExecutionTask) {
                                notifyExternalListeners((ProcessingExecutionTask) task, ExecutionStatus.SUSPENDED.friendlyName(), -1, out);
                            }
                            break;
                        case Session.UNDETERMINED:
                        case Session.QUEUED_ACTIVE:
                            // nothing to do
                            break;
                        case Session.RUNNING:
                            task.setLastUpdated(LocalDateTime.now());
                            taskProvider.update(task);
                            if (task.getExecutionStatus() != ExecutionStatus.RUNNING) {
                                try {
                                    notifyStatusListener(task, ExecutionStatus.RUNNING, null);
                                } catch (Exception e) {
                                    logger.severe(String.format("Status update for task %s failed. Reason: %s",
                                                                taskName, e.getMessage()));
                                }
                            }
                            sendProgressMessage(task);
                            UserQuotaManager.getInstance().updateUserCPU(new UserPrincipal(userId));
                            break;
                        case Session.DONE:
                            if (!(task instanceof ScriptTask)) {
                                decrementMemory(task.getExecutionNodeHostName(), task.getUsedRAM());
                            }
                            task.setLog(getJobOutput(task.getResourceId()));
                            // Just mark the task as finished with success status
                            if (task.getExecutionStatus() == ExecutionStatus.RUNNING) {
                                // Only a running task can complete
                                if (session instanceof JobExitHandler) {
                                    final JobExitHandler handler = (JobExitHandler) session;
                                    String resourceId = task.getResourceId();
                                    handler.cleanupJob(resourceId);
                                }
                                markTaskFinished(task, ExecutionStatus.PENDING_FINALISATION, ExecutionStatus.PENDING_FINALISATION.friendlyName());
                            }
                            if (task instanceof ProcessingExecutionTask) {
                                ProcessingExecutionTask executionTask = (ProcessingExecutionTask) task;
                                notifyExternalListeners(executionTask, ExecutionStatus.PENDING_FINALISATION.friendlyName(), 0, null);
                            }
                            if (masterHost.equals(task.getExecutionNodeHostName()) && !SystemUtils.IS_OS_WINDOWS) {
                                String taskOutputLocation = task.getInstanceTargetOutput();
                                if (taskOutputLocation != null) {
                                    DockerVolumeMap volumeMap = ExecutionConfiguration.getMasterContainerVolumeMap();
                                    String location = taskOutputLocation;
                                    if (!location.startsWith(volumeMap.getHostWorkspaceFolder()) &&
                                        taskOutputLocation.startsWith(volumeMap.getContainerWorkspaceFolder())) {
                                        location = taskOutputLocation.replace(volumeMap.getContainerWorkspaceFolder(),
                                                                              volumeMap.getHostWorkspaceFolder());
                                    }
                                    FileUtilities.takeOwnership(Paths.get(location).getParent());
                                }
                            }
                            UserQuotaManager.getInstance().updateUserCPU(new UserPrincipal(userId));
                            break;
                        case Session.FAILED:
                            String error;
                            int code;
                            decrementMemory(task.getExecutionNodeHostName(), task.getUsedRAM());
                            if (session instanceof JobExitHandler) {
                                final JobExitHandler handler = (JobExitHandler) session;
                                String resourceId = task.getResourceId();
                                error = handler.getJobOutput(resourceId);
                                code = handler.getJobExitCode(resourceId);
                                handler.cleanupJob(resourceId);
                            } else {
                                error = "n/a";
                                code = -255;
                            }
                            task.setLog(error);
                            taskProvider.update(task);
                            markTaskFinished(task, ExecutionStatus.FAILED, error);
                            logger.warning(String.format("Task %s FAILED. Process output: %s", taskName, error));
                            if (task instanceof ProcessingExecutionTask) {
                                ProcessingExecutionTask executionTask = (ProcessingExecutionTask) task;
                                notifyExternalListeners(executionTask, ExecutionStatus.FAILED.friendlyName(), code, error);
                            }
                            UserQuotaManager.getInstance().updateUserCPU(new UserPrincipal(userId));
                            break;
                    }
                } catch (Exception  e) {
                    logger.severe(String.format("%s: Cannot get the status for the task %s [%s]",
                                                e.getClass().getName(), taskName, e.getMessage()));
                    markTaskFinished(task, ExecutionStatus.FAILED, e.getMessage());
                }
            }
        } else {
            logger.finest("No tasks are currently executing");
        }
    }

    @Override
    public String defaultId() { return "DRMAAExecutor"; }

    public String getDRMName() {
        return this.sessions != null
               ? this.sessions.values().stream().map(Session::serviceName).collect(Collectors.joining(","))
               : "n/a";
    }


    public String getDRMVersion() {
        return this.sessions != null
               ? this.sessions.values().stream().map(s -> s.getVersion().toString()).collect(Collectors.joining(","))
               : "n/a";
    }

    /**
     * Retrieves the output of a job associated with the specified resource ID.
     *
     * @param resourceId the unique identifier of the resource whose job output is to be retrieved
     * @return the job's output as a string, or "n/a" if the session does not implement JobExitHandler
     * @throws ExecutionException if no task is found for the provided resourceId
     */
    public String getJobOutput(String resourceId) {
        String out;
        final ExecutionTask task = taskProvider.getByResourceId(resourceId);
        if (task == null) {
            throw new ExecutionException("No such resourceId: " + resourceId);
        }
        Session session = this.sessions.get(task.getJob().getEnvironment());
        if (session instanceof JobExitHandler) {
            out = ((JobExitHandler) session).getJobOutput(resourceId);
        } else {
            out = "n/a";
        }
        return out;
    }

    /**
     * Increments the amount of memory requested for a specified node.
     * If the node's host does not already exist in the memory tracking map,
     * it is added with the specified memory amount. If the host exists, the
     * memory amount is added to the existing value.
     *
     * @param node the {@link NodeDescription} representing a node, which contains
     *        information about the host whose memory request is being tracked
     * @param amount the amount of memory, in megabytes, to add to the requested memory
     *        for the specified host
     */
    private void incrementMemory(NodeDescription node, long amount) {
        String host = node.getId();
        if (!this.hostMemoryRequests.containsKey(host)) {
            this.hostMemoryRequests.put(host, new Tuple<>(node, new AtomicLong(amount)));
        } else {
            this.hostMemoryRequests.get(host).getKeyTwo().addAndGet(amount);
        }
        logger.fine("Host " + host + " has " + this.hostMemoryRequests.get(host).getKeyTwo().get() + "MB requested memory");
    }

    /**
     * Reduces the amount of memory requested for a specified host.
     * If the host exists in the memory tracking map, the method decrements
     * the corresponding memory value by the specified amount.
     *
     * @param host the name of the host whose requested memory is to be decremented
     * @param amount the amount of memory, in megabytes, to decrement from the host's requested memory
     */
    public void decrementMemory(String host, long amount) {
        Tuple<NodeDescription, AtomicLong> tuple = this.hostMemoryRequests.get(host);
        if (tuple != null) {
            AtomicLong aLong = tuple.getKeyTwo();
            aLong.getAndAdd(-amount);
            if (aLong.get() <= 0) {
                aLong.set(0);
            }
            logger.fine("Host " + host + " has " + this.hostMemoryRequests.get(host).getKeyTwo().get() + "MB requested memory");
        }
    }

    /**
     * Determines whether a task can be submitted to a specified host based on its memory constraints.
     *
     * @param host the name of the host where the task is to be submitted
     * @param newMemory the additional memory required by the task in MB
     * @return true if the task can be submitted to the host based on available memory; false otherwise
     */
    private boolean canSubmitTask(String host, long newMemory) {
        final Tuple<NodeDescription, AtomicLong> hostData = this.hostMemoryRequests.get(host);
        final boolean canSubmit;
        if (hostData != null) {
            long installed = hostData.getKeyOne().getFlavor().getMemory() * hostData.getKeyOne().getFlavor().getMemoryUnit().value() / MemoryUnit.MB.value();
            long requested = hostData.getKeyTwo().get();
            long reserved = Long.parseLong(ConfigurationManager.getInstance().getValue("execution.reserved.memory", "4096"));
            if (installed <= requested + newMemory + reserved) {
                logger.finest(String.format("Host %s may not have enough available memory (installed: %d, reserved: %d, requested: %d)",
                                             host, installed, reserved, requested));
                canSubmit = false;
            } else {
                canSubmit = true;
            }
        } else {
            canSubmit = true;
        }
        return canSubmit;
    }

    /**
     * Executes a task by submitting its associated job template to the system,
     * monitoring its status, and handling any errors or updates during execution.
     *
     * @param pair a tuple containing the {@link JobTemplate} associated with the task
     *             and the {@link ProcessingExecutionTask} to be executed. The job template
     *             defines the configuration for the execution, while the task represents
     *             the execution unit.
     * @return always returns {@code null}. The operation executes its logic without
     *         returning a meaningful value.
     */
    private Void executeImpl(Tuple<JobTemplate, ProcessingExecutionTask> pair) {
        final JobTemplate jt = pair.getKeyOne();
        final ProcessingExecutionTask task = pair.getKeyTwo();
        logger.finest(String.format("Task %s has been dequeued for execution", task.getId()));
        try {
            Session session = getSession(task.getJob().getEnvironment());
            String id = session.runJob(jt);
            session.deleteJobTemplate(jt);
            if (id == null) {
                final String message = String.format("Unable to run job (id null) for task %s", task.getId());
                logger.severe(message);
                changeTaskStatus(task, ExecutionStatus.FAILED, message);
                notifyExternalListeners(task, message, -1, null);
                return null;
            }
            task.setResourceId(id);
            task.setStartTime(LocalDateTime.now());
            if (jt instanceof JobTemplateExtension) {
                JobTemplateExtension job = (JobTemplateExtension) jt;
                if (job.hasAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE)) {
                    task.setExecutionNodeHostName((String) job.getAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE));
                }
            }
            taskProvider.update(task);
            ExecutionStatus newStatus = task instanceof ScriptTask
                                        ? ExecutionStatus.DONE
                                        : session.hasOwnQueue()
                                          ? ExecutionStatus.QUEUED_ACTIVE
                                          : ExecutionStatus.RUNNING;
            changeTaskStatus(task, newStatus, newStatus.friendlyName());
            notifyExternalListeners(task, null, -1, null);
        } catch (Exception e) {
            final String message = String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage());
            logger.severe(message);
            changeTaskStatus(task, ExecutionStatus.FAILED, message);
            notifyExternalListeners(task, message, -1, null);
        }
        return null;
    }

    /**
     * Creates a job template for the specified processing execution task.
     *
     * @param task the processing execution task for which the job template is to be created.
     * @return the created JobTemplate instance configured based on the provided task and its associated parameters.
     * @throws DrmaaException if there is an error while communicating with the DRMAA session.
     * @throws IOException if an I/O error occurs during job template creation or configuration.
     * @throws PersistenceException if there is an error persisting the task updates (e.g., updating the task command).
     */
    private JobTemplate createJobTemplate(ProcessingExecutionTask task) throws DrmaaException, IOException, PersistenceException {
        // Get from the component the execution command
        JobTemplate jt;
        try {
            logger.entering(getClass().getSimpleName(), "createJobTemplate(" + task.getId() + ")");
            final String taskName = TaskUtilities.getTaskDescriptiveName(task);
            String[] pArgs = null;
            final ProcessingComponent component = task.getComponent();
            final Container container = containerProvider.get(component.getContainerId());
            final Application app = container.getApplications()
                    .stream()
                    .filter(a -> component.getId().endsWith(a.getName().toLowerCase()))
                    .findFirst().orElse(null);
            Session session = getSession(task.getJob().getEnvironment());
            jt = session.createJobTemplate();
            if (jt == null) {
                throw new ExecutionException("Error creating job template from the session!");
            }
            ExecutionJob job = task.getJob();
            if (jt instanceof JobTemplateExtension && task instanceof ScriptTask) {
                final JobTemplateExtension template = (JobTemplateExtension) jt;
                template.setAttribute(DrmaaJobExtensions.SIMULATE_EXECUTION_ATTRIBUTE, Boolean.TRUE);
                template.setAttribute(DrmaaJobExtensions.TASK_ID, task.getId());
                List<Long> parentIds = TaskUtilities.getParentIds(task);
                template.setAttribute(DrmaaJobExtensions.TASK_ANCESTOR_ID, parentIds);
                if (parentIds != null && !parentIds.isEmpty()) {
                    template.setAttribute(DrmaaJobExtensions.TASK_ANCESTOR_OUTPUT,
                                          taskProvider.list(parentIds).stream().map(ExecutionTask::getInstanceTargetOutput).collect(Collectors.joining(",")));
                } else {
                    template.setAttribute(DrmaaJobExtensions.TASK_ANCESTOR_OUTPUT, null);
                }
                template.setAttribute(DrmaaJobExtensions.TASK_NAME, TaskUtilities.getComponentFor(task).getLabel());
                template.setAttribute(DrmaaJobExtensions.JOB_ID, job.getId());
                template.setAttribute(DrmaaJobExtensions.USER, job.getUserId());
                JobType jobType = job.getJobType();
                final ExecutionUnitFormat value;
                switch (jobType) {
                    case JSON_EXPORT:
                      value = ExecutionUnitFormat.JSON;
                      break;
                    case BASH_EXPORT:
                        value = ExecutionUnitFormat.BASH;
                        break;
                    case CWL_EXPORT:
                        value = ExecutionUnitFormat.CWL;
                        break;
                    case ARGO_EXPORT:
                        value = ExecutionUnitFormat.ARGO;
                        break;
                    default:
                        value = ExecutionUnitFormat.TAO;
                }
                template.setAttribute(DrmaaJobExtensions.SCRIPT_FORMAT, value.name());
                Path basePath = Paths.get(job.getJobOutputPath());
                template.setAttribute(DrmaaJobExtensions.SCRIPT_PATH,
                                      basePath.resolve(basePath.getFileName().toString() + value.getExtension()).toString());
                final List<Variable> values = task.getOutputParameterValues();
                if (values != null) {
                    template.setAttribute(DrmaaJobExtensions.TASK_OUTPUT, values.stream().collect(Collectors.toMap(Variable::getKey, Variable::getValue)));
                }
                template.setAttribute(DrmaaJobExtensions.IS_TERMINAL_TASK, TaskUtilities.isTerminalTask(task));
            }
            final long memory = app != null
                                ? app.getMemoryRequirements()
                                : container.getApplications().stream().mapToLong(Application::getMemoryRequirements).min().orElse(0L);
            setMemoryConstraint(jt, memory);

            NodeDescription node;
            final int cpu;
            final long mem;
            if(job.getEnvironment() == Environment.DEFAULT || job.getEnvironment() == Environment.SIMPIPE) {
                NodeData nodeData;
                final Integer parallelism = component.getParallelism();
                final ExecutionStrategy strategy = ExecutionStrategy.getExecutionStrategy(task);
                nodeData = strategy.getNode(memory);
                while (nodeData == null || !canSubmitTask(nodeData.getNode().getId(), memory)) {
                    try {
                        TimeUnit.SECONDS.sleep(15);
                    } catch (InterruptedException ignored) { }
                    //if (nodeData == null) {
                        nodeData = strategy.getNode(memory);
                    //}
                }
                node = nodeData.getNode();
                if (node == null) {
                    throw new TryLaterException("Cannot obtain an available node [null]");
                } else {
                    if (!(task instanceof ScriptTask)) {
                        incrementMemory(node, memory);
                    }
                    logger.info("Task " + taskName + " will be submitted to host " + node.getId());
                }
                setHost(jt, node.getId());
                task.setExecutionNodeHostName(node.getId());
                cpu = Math.min(nodeData.getCpu(), parallelism != null ? parallelism : 4);
                mem = nodeData.getMemory();
            } else {
                // Kubernetes
                node = new NodeDescription();
                node.setId("k8s-master");
                setHost(jt, node.getId());
                task.setExecutionNodeHostName(node.getId());
                cpu = 0;
                mem = 0;
            }

            String location = task.getComponent().getFileLocation();
            if ((SystemUtils.IS_OS_WINDOWS && !location.startsWith("/") && !Paths.get(location).isAbsolute()) ||
                    (SystemUtils.IS_OS_LINUX && !Paths.get(location).isAbsolute())) {
                String path = container.getApplicationPath();
                path = path == null ? "" : path;
                if (!path.isEmpty() && !path.endsWith("/")) {
                    path += "/";
                }
                path += location;
                task.getComponent().setExpandedFileLocation(path);
            }
            // if parallel flags are defined, use them (but not for Kubernetes since we don't know the worker flavor)
            if (app != null && app.hasParallelFlag() && job.getEnvironment() != Environment.KUBERNETES) {
                Class<?> type = app.parallelArgumentType();
                if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(Long.class)) {
                    pArgs = app.parallelArguments(Integer.class, node.getFlavor().getCpu());
                } else {
                    pArgs = app.parallelArguments(Boolean.class, true);
                }
            }
            String executionCmd = task.getCommand();
            if (executionCmd == null) {
                // Assemble the regular execution command, as produced by the processing component after applying values
                executionCmd = task.buildExecutionCommand();
                task.setCommand(executionCmd);
                try {
                    taskProvider.update(task);
                } catch (PersistenceException e) {
                    logger.warning(e.getMessage());
                }
            }
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
            String cmd;

            // split the execution command but preserving the entities between double quotes
            final Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(executionCmd);
            while (m.find()) {
                argsList.add(m.group(1)); // Add .replace("\"", "") to remove surrounding quotes.
            }
            final boolean isUtilityTask = task.getComponent().getComponentType() == ProcessingComponentType.UTILITY;
            final boolean useDocker = ExecutionConfiguration.useDocker() || task instanceof ScriptTask;
            ContainerUnit unit = null;
            final boolean isLocal = task.getExecutionNodeHostName().equals(masterHost);
            if (useDocker && !isUtilityTask) {
                if (jt instanceof JobTemplateExtension) {
                    cmd = argsList.get(0);
                    argsList.remove(0);
                    final JobTemplateExtension template = (JobTemplateExtension) jt;
                    final Map<String, String> dbSources = component.getSources().stream()
                            .filter(s -> DataFormat.DB_CONNECTION == s.getDataDescriptor().getFormatType())
                            .collect(Collectors.toMap(SourceDescriptor::getName, d -> d.getDataDescriptor().getLocation()));
                    unit = new ContainerUnit(ContainerType.DOCKER);
                    unit.setArguments(new ArrayList<String>() {{
                        add("-i");
                        add("--sig-proxy=true");
                        add("--rm");
                        add("--volume-driver");
                        add("cifs");
                        // -u $(id -u ${USER}):$(id -g ${USER})
                        /*if (!masterHost.equals(node.getId())) {
                            add("-u");
                            add("$(id -u ${USER}):$(id -g ${USER})");
                        }*/
                    }});
                    unit.setEnvironmentVariables(dbSources);
                    final DockerVolumeMap volumeMount;
                    if (isLocal) {
                        volumeMount = ExecutionConfiguration.getMasterContainerVolumeMap();
                    } else {
                        volumeMount = ExecutionConfiguration.getWorkerContainerVolumeMap();
                    }
                    unit.addVolumeMapping(volumeMount.getHostWorkspaceFolder(), volumeMount.getContainerWorkspaceFolder());
                    String hostFolder = volumeMount.getHostConfigurationFolder();
                    if (!StringUtilities.isNullOrEmpty(hostFolder)) {
                        unit.addVolumeMapping(hostFolder, volumeMount.getContainerConfigurationFolder());
                    }
                    hostFolder = volumeMount.getHostTemporaryFolder();
                    if (!StringUtilities.isNullOrEmpty(hostFolder)) {
                        unit.addVolumeMapping(hostFolder + (hostFolder.endsWith("/") ? "" : "/") + task.getId(),
                                              volumeMount.getContainerTemporaryFolder());
                    }
                    hostFolder = volumeMount.getHostEODataFolder();
                    if (!StringUtilities.isNullOrEmpty(hostFolder)) {
                        unit.addVolumeMapping(hostFolder, volumeMount.getContainerEoDataFolder());
                    }
                    final Map<String, String> additionalMappings = volumeMount.getAdditionalMappings();
                    if (additionalMappings != null && !additionalMappings.isEmpty()) {
                        for (Map.Entry<String, String> entry : additionalMappings.entrySet()) {
                            unit.addVolumeMapping(entry.getKey(), entry.getValue());
                        }
                    }
                    unit.setContainerName(container.getName());
                    unit.setContainerRegistry(ExecutionConfiguration.getDockerRegistry());
                    if (cpu > 0) {
                        // Enforce the number of CPUs to be used
                        unit.addArgument("--cpus", String.valueOf(cpu));
                        task.setUsedCPU(cpu);
                    }
                    if (mem > 0) {
                        long arg = memory > 0 ? memory : Math.min(8192, mem);
                        unit.addArgument("--memory", arg + "MB");
                        task.setUsedRAM((int) arg);
                    }
                    template.setAttribute(DrmaaJobExtensions.CONTAINER_ATTRIBUTE, unit);
                    if (task.getInstanceTemporaryOutput() != null) {
                        // replace the original command with /bin/bash since we're going to execute
                        // multiple commands inside docker run
                        cmd = wrapShellInvocation(component, argsList, cmd,
                                                  task.getInstanceTemporaryOutput(),
                                                  isLocal
                                                    ? task.getInstanceTargetOutput()
                                                    : task.getInstanceTargetOutput()
                                                          .replace(ExecutionConfiguration.getMasterContainerVolumeMap(job.getEnvironment() == Environment.KUBERNETES)
                                                                                         .getHostWorkspaceFolder(),
                                                                   volumeMount.getContainerWorkspaceFolder()),
                                                  isLocal);
                    }
                } else {
                    cmd = "docker";
                    final DockerVolumeMap volumeMap;
                    if (isLocal) {
                        volumeMap = ExecutionConfiguration.getMasterContainerVolumeMap();
                    } else {
                        volumeMap = ExecutionConfiguration.getWorkerContainerVolumeMap();
                    }
                    final List<String> dockerArgsList = new ArrayList<String>() {{
                        add("run");
                        add("-i");              // Keep STDIN open even if not attached
                        add("--rm");            // Automatically remove the container when it exits
                        add("--sig-proxy=true");// Proxy all received signals to the process
                        add("--volume-driver");
                        add("cifs");
                        //-u $(id -u ${USER}):$(id -g ${USER})
                        /*if (!masterHost.equals(node.getId())) {
                            add("-u");
                            add("$(id -u ${USER}):$(id -g ${USER})");
                        }*/
                        add("-v");
                        add(volumeMap.getHostWorkspaceFolder() + ":" + volumeMap.getContainerWorkspaceFolder());
                        String folder = volumeMap.getHostTemporaryFolder();
                        if (!StringUtilities.isNullOrEmpty(folder)) {
                            add("-v");
                            add(folder + (folder.endsWith("/") ? "" : "/") + task.getId() + ":" + volumeMap.getContainerTemporaryFolder());
                        }
                        folder = volumeMap.getHostConfigurationFolder();
                        if (!StringUtilities.isNullOrEmpty(folder)) {
                            add("-v");
                            add(folder + ":" + volumeMap.getContainerConfigurationFolder());
                        }
                        final Map<String, String> additionalMappings = volumeMap.getAdditionalMappings();
                        if (additionalMappings != null && !additionalMappings.isEmpty()) {
                            for (Map.Entry<String, String> entry : additionalMappings.entrySet()) {
                                add("-v");
                                add(entry.getKey() + ":" + entry.getValue());
                            }
                        }
                        if (cpu > 0) {
                            // Enforce the number of CPUs to be used
                            add("--cpus");
                            add(String.valueOf(cpu));
                            task.setUsedCPU(cpu);
                        }
                        if (mem > 0) {
                            long arg = memory > 0 ? memory : Math.min(8192, mem);
                            add("--memory");    // Enforce the memory limit to be used
                            // memory, if not 0, is expressed in MB
                            add(arg + "MB");
                            task.setUsedRAM((int) arg);
                        }
                    }};
                    String value = ExecutionConfiguration.getDockerRegistry();
                    if (!StringUtilities.isNullOrEmpty(value)) {
                        dockerArgsList.add(value + "/" + container.getName());
                    } else {
                        dockerArgsList.add(container.getId());
                    }
                    dockerArgsList.addAll(argsList);
                    argsList = dockerArgsList;
                }
            } else {
                cmd = argsList.get(0);
                argsList.remove(0);
            }
            jt.setRemoteCommand(cmd);
            jt.setArgs(argsList);

            setJobId(jt, job.getId());
            setTaskId(jt, task.getId());

            taskProvider.update(task);
            logger.finest(String.format("Task %s: %s%s %s",
                    taskName,
                    unit != null ? String.join(" ", ContainerCmdBuilder.buildCommandLineArguments(unit)) + " " : "",
                    cmd,
                    String.join(" ", argsList)));
        } catch (Throwable t) {
            logger.severe(t.getMessage());
            throw t;
        }
        return jt;
    }

    /**
     * Prepares and wraps a shell invocation command for processing components.
     *
     * @param component the processing component that requires shell invocation
     * @param argsList the list of arguments to be passed to the shell command
     * @param initialCmd the initial command to run in the shell
     * @param tempOutput the temporary output directory or file path
     * @param finalOutput the final output directory or file path
     * @param isLocal a boolean indicating whether the execution is local
     * @return the shell executable (e.g., "/bin/bash") for the invocation
     */
    private String wrapShellInvocation(ProcessingComponent component, final List<String> argsList, String initialCmd,
                                       final String tempOutput, final String finalOutput, final boolean isLocal) {
        final boolean isOutputManaged = component.isOutputManaged();
        final String tmpPath = isOutputManaged ? tempOutput : tempOutput.substring(0, tempOutput.lastIndexOf('/') + 1) + "*";
        String finalPath = finalOutput.substring(0, finalOutput.lastIndexOf('/') + 1);
        final DockerVolumeMap volumeMap = isLocal ? ExecutionConfiguration.getMasterContainerVolumeMap() : ExecutionConfiguration.getWorkerContainerVolumeMap();
        final String workspaceFolder = SystemUtils.IS_OS_WINDOWS && !volumeMap.getHostWorkspaceFolder().startsWith("/")
                                       ? volumeMap.getHostWorkspaceFolder().substring(2).replace("\\", "/")
                                       : volumeMap.getHostWorkspaceFolder();
        final String containerWorkspaceFolder = volumeMap.getContainerWorkspaceFolder();
        if (finalPath.startsWith(workspaceFolder)) {
            if (isLocal) {
                try {
                    FileUtilities.ensureExists(Paths.get(finalPath));
                } catch (IOException e) {
                    logger.warning(e.getMessage());
                }
            }
            final String replacement = containerWorkspaceFolder.endsWith("/")
                                       ? containerWorkspaceFolder
                                       : containerWorkspaceFolder + "/";
            finalPath = finalPath.replace(workspaceFolder, replacement);
        }
        finalPath = finalPath.replace("//", "/");
        final boolean isSnapDimap = component.getTargets().stream().anyMatch(t -> 
                 (t.getDataDescriptor().getLocation() != null && t.getDataDescriptor().getLocation().toLowerCase().endsWith(".dim")) ||
                         "beam-dimap".equalsIgnoreCase(t.getDataDescriptor().getFormatName()))
                                    || finalOutput.endsWith(".dim");
        final boolean isSnap = initialCmd.equals("/opt/snap/bin/gpt");
        final boolean recursiveCondition = !isOutputManaged || isSnapDimap || component.getTargets().stream().anyMatch(t -> DataFormat.FOLDER == t.getDataDescriptor().getFormatType());
        argsList.add(0, initialCmd);
        if (isLocal) {
            Path p = Path.of(tmpPath);
            try {
                if (Files.isDirectory(p)) {
                    FileUtilities.createDirectories(p);
                } else {
                    FileUtilities.createDirectories(p.getParent());
                }
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        } else {
            argsList.add(0, "mkdir");
            argsList.add(1, "-p");
            if (component.getTargets().stream().anyMatch(t -> t.getDataDescriptor().getFormatType() == DataFormat.FOLDER)) {
                argsList.add(2, tmpPath);
            } else {
                argsList.add(2, tmpPath.substring(0, tmpPath.lastIndexOf('/')));
            }
            argsList.add(3, "&&");
        }
        if (isSnap && isOutputManaged && tempOutput.toLowerCase().endsWith(".tif")) {
            argsList.add("-f");
            argsList.add("GeoTIFF");
        }
        argsList.add("&&");
        argsList.add("cp");
        if (recursiveCondition) {
            argsList.add("-fR");
        }
        // If the output is managed by the component, copy all the folder contents
        if (!isOutputManaged) {
            argsList.add(tmpPath);
        } else {
            // for SNAP BEAM-DIMAP, copy both the metadata and the data folder
            if (isSnapDimap) {
                argsList.add(tmpPath.replace(tmpPath.substring(tmpPath.lastIndexOf('.')), ".*"));
            } else {
                argsList.add(tmpPath);
            }
        }
        argsList.add(finalPath);
        // Execute rm even if the previous failed, to clear the workdir
        argsList.add(";");
        argsList.add("rm");
        //if (recursiveCondition) {
        argsList.add("-r");
        argsList.add(tmpPath.substring(0, tmpPath.lastIndexOf('/')) + "/*");
        //}
        /*if (!isOutputManaged) {
            argsList.add(tmpPath.substring(0, tmpPath.lastIndexOf('/')) + "/*");
        } else {
            if (isSnapDimap) {
                argsList.add(tmpPath.replace(tmpPath.substring(tmpPath.lastIndexOf('.')), ".*"));
            } else {
                argsList.add(tmpPath);
            }
        }*/
        // all the existing args are merged into a single string as argument to -c
        final String quoteChar = isLocal
                ? SystemUtils.IS_OS_WINDOWS
                    ? "\""
                    : ""
                : "\"";
        // before joining the arguments, check replace double quoted arguments with single quotes
        argsList.replaceAll(new UnaryOperator<String>() {
            @Override
            public String apply(String arg) {
                return arg.replace("\"", "'");
            }
        });
        final String singleArg = quoteChar + String.join(" ", argsList) + quoteChar;
        argsList.clear();
        argsList.add("-c");
        argsList.add(singleArg);
        return "/bin/bash";
    }

    /**
     * Sets a memory constraint for a given job template.
     * If the specified job template supports memory constraints and the memory value is positive
     * and memory constraint enforcement is enabled, this method sets the memory requirement attribute
     * based on the provided value.
     *
     * @param jt the job template to be configured. Must be an instance of {@code JobTemplateExtension}.
     * @param memory the amount of memory to be set as a requirement for the job.
     * @throws DrmaaException if an error occurs while setting the memory constraint.
     */
    private void setMemoryConstraint(JobTemplate jt, long memory) throws DrmaaException {
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE) &&
                    ExecutionConfiguration.forceMemoryConstraint() && memory > 0) {
                job.setAttribute(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE, memory);
            }
        }
    }

    /**
     * Sets the host attribute for a given job template. If the job template is an instance of
     * {@code JobTemplateExtension} and it supports the {@code NODE_ATTRIBUTE}, this method assigns
     * the specified host value to the attribute.
     *
     * @param jt   the job template to configure. Must be an instance of {@code JobTemplateExtension}.
     * @param host the host name or address to be set for the job.
     * @throws DrmaaException if an error occurs while setting the host attribute.
     */
    private void setHost(JobTemplate jt, String host) throws DrmaaException {
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE)) {
                job.setAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE, host);
            }
        }
    }

    /**
     * Sets the job ID for a given job template. If the job template is an instance of
     * {@code JobTemplateExtension}, and it contains the {@code JOB_ID} attribute, this method
     * assigns the provided job ID to that attribute.
     *
     * @param jt the job template to configure. Must be an instance of {@code JobTemplateExtension}.
     * @param id the job ID to be assigned.
     * @throws DrmaaException if an error occurs while setting the job ID attribute.
     */
    private void setJobId(JobTemplate jt, Long id) throws DrmaaException {
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(DrmaaJobExtensions.JOB_ID)) {
                job.setAttribute(DrmaaJobExtensions.JOB_ID, id);
            }
        }
    }

    /**
     * Sets the task ID for a given job template. If the job template is an instance of
     * {@code JobTemplateExtension} and it supports the {@code TASK_ID} attribute, this method assigns
     * the provided task ID to that attribute.
     *
     * @param jt the job template to configure. Must be an instance of {@code JobTemplateExtension}.
     * @param id the task ID to be assigned.
     * @throws DrmaaException if an error occurs while setting the task ID attribute.
     */
    private void setTaskId(JobTemplate jt, Long id) throws DrmaaException {
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(DrmaaJobExtensions.TASK_ID)) {
                job.setAttribute(DrmaaJobExtensions.TASK_ID, id);
            }
        }
    }

    /**
     * Notifies external task listeners based on the current execution status of the given task.
     * The notification is sent only to the listeners supporting the protocol defined in the
     * job's callback descriptor.
     *
     * @param task the {@link ProcessingExecutionTask} instance representing the task whose listeners need to be notified
     * @param message a string message providing additional context, typically used in case of errors
     * @param exitCode the exit code of the task process, relevant for error handling
     * @param processOutput the output generated by the task's process
     */
    private void notifyExternalListeners(ProcessingExecutionTask task, String message, int exitCode, String processOutput) {
        final ExecutionJob job = task.getJob();
        final EndpointDescriptor jobCallback = job.getCallbackDescriptor();
        if (jobCallback != null && this.taskListeners != null) {
            for (TaskListener listener : this.taskListeners) {
                if (listener.supportsProtocol(jobCallback.getProtocol())) {
                    // only call the listener corresponding to the external listener descriptor of the job
                    switch (task.getExecutionStatus()) {
                        case QUEUED_ACTIVE:
                            listener.onStarted(task);
                            break;
                        case DONE:
                            listener.onCompleted(task, processOutput);
                            break;
                        case FAILED:
                            listener.onError(task, message, exitCode, processOutput);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    /**
     * Retrieves the {@code Session} associated with the specified {@code Environment}.
     * If the provided {@code Environment} is {@code null}, the default environment is used.
     * Throws an exception if no session is found for the given environment.
     *
     * @param environment the {@link Environment} instance for which to retrieve the session.
     *                     If {@code null}, the default environment will be used.
     * @return the {@link Session} associated with the specified or default environment.
     * @throws DrmaaException if no session exists for the specified environment
     *                        or if there is an issue retrieving the session.
     */
    private Session getSession(Environment environment) throws DrmaaException {
        if (environment == null) {
            environment = Environment.DEFAULT;
        }
        Session session = this.sessions.get(environment);
        if (session == null) {
            throw new DrmsInitException("No session for DRM " + environment);
        }
        return session;
    }
}
