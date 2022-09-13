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
import ro.cs.tao.component.*;
import ro.cs.tao.component.enums.ProcessingComponentType;
import ro.cs.tao.datasource.DataSourceTopic;
import ro.cs.tao.docker.Application;
import ro.cs.tao.docker.Container;
import ro.cs.tao.docker.DockerVolumeMap;
import ro.cs.tao.docker.ExecutionConfiguration;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.execution.DrmaaJobExtensions;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.callback.EndpointDescriptor;
import ro.cs.tao.execution.local.DefaultSession;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.monitor.NodeManager;
import ro.cs.tao.execution.monitor.NodeManager.NodeData;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.messaging.ProgressNotifier;
import ro.cs.tao.persistence.ContainerProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.BlockingQueueWorker;
import ro.cs.tao.utils.executors.container.ContainerCmdBuilder;
import ro.cs.tao.utils.executors.container.ContainerType;
import ro.cs.tao.utils.executors.container.ContainerUnit;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;
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
    private static ExecutionTaskProvider taskProvider;
    private static ContainerProvider containerProvider;
    private Session session;
    private final BlockingQueue<Tuple<JobTemplate, ProcessingExecutionTask>> queue;
    private final BlockingQueueWorker<Tuple<JobTemplate, ProcessingExecutionTask>> queueWorker;
    private final List<TaskListener> taskListeners;
    private final Map<String, Tuple<NodeDescription, AtomicLong>> hostMemoryRequests;
    private final Map<Path, Set<Long>> cachedInputs;

    public static void setTaskProvider(ExecutionTaskProvider provider) { taskProvider = provider; }

    public static void setContainerProvider(ContainerProvider provider) { containerProvider = provider; }

    public DRMAAExecutor() {
        super();
        if (taskProvider == null) {
            throw new ExecutionException("A TaskProvider is required to use this executor");
        }
        if (containerProvider == null) {
            throw new ExecutionException("A ContainerProvider is required to use this executor");
        }
        this.queue = new LinkedBlockingDeque<>();
        this.queueWorker = new BlockingQueueWorker<>(this.queue, this::executeImpl, NodeManager.getInstance().getActiveNodes());
        final Set<TaskListener> services = ServiceRegistryManager.getInstance().getServiceRegistry(TaskListener.class).getServices();
        this.taskListeners = new ArrayList<>();
        if (services != null) {
            this.taskListeners.addAll(services);
        }
        this.hostMemoryRequests = Collections.synchronizedMap(new HashMap<>());
        this.cachedInputs = Collections.synchronizedMap(new HashMap<>());
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
            isInitialized.set(false);
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
        } catch (InterruptedException | DrmaaException | IOException | PersistenceException e) {
            logger.severe(String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage()));
            throw new ExecutionException("Error executing DRMAA session operation", e);
        }
    }

    @Override
    public void stop(ProcessingExecutionTask task)  throws ExecutionException {
        try {
            if (task.getResourceId() != null) {
                session.control(task.getResourceId(), Session.TERMINATE);
            }
            markTaskFinished(task, ExecutionStatus.CANCELLED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session terminate for task with id " + task.getId(), e);
        }
    }

    @Override
    public void suspend(ProcessingExecutionTask task) throws ExecutionException {
        try {
            if (task.getResourceId() != null) {
                session.control(task.getResourceId(), Session.SUSPEND);
            }
            changeTaskStatus(task, ExecutionStatus.SUSPENDED);
        } catch (DrmaaException e) {
            throw new ExecutionException("Error executing DRMAA session suspend for task with id " + task.getId(), e);
        }
    }

    @Override
    public void resume(ProcessingExecutionTask task) throws ExecutionException {
        try {
            if (task.getResourceId() != null) {
                session.control(task.getResourceId(), Session.RESUME);
            }
            changeTaskStatus(task, ExecutionStatus.QUEUED_ACTIVE);
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
        final List<ExecutionTask> tasks = taskProvider.listExecuting();
        if (tasks != null) {
            // For each job, get its status from DRMAA
            for (ExecutionTask task : tasks) {
                final String taskName = TaskUtilities.getTaskName(task);
                try {
                    int jobStatus = session.getJobProgramStatus(task.getResourceId());
                    switch (jobStatus) {
                        case Session.SYSTEM_ON_HOLD:
                        case Session.USER_ON_HOLD:
                        case Session.USER_SYSTEM_ON_HOLD:
                        case Session.SYSTEM_SUSPENDED:
                        case Session.USER_SUSPENDED:
                        case Session.USER_SYSTEM_SUSPENDED:
                            decrementMemory(task.getExecutionNodeHostName(), task.getUsedRAM());
                            try {
                                notifyStatusListener(task, ExecutionStatus.SUSPENDED);
                            } catch (Exception e) {
                                logger.severe(String.format("Status update for task %s failed. Reason: %s",
                                                            taskName, e.getMessage()));
                            }
                            String out;
                            if (session instanceof DefaultSession) {
                                out = ((DefaultSession) this.session).getJobOutput(task.getResourceId());
                            } else {
                                out = "n/a";
                            }
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
                                    notifyStatusListener(task, ExecutionStatus.RUNNING);
                                } catch (Exception e) {
                                    logger.severe(String.format("Status update for task %s failed. Reason: %s",
                                                                taskName, e.getMessage()));
                                }
                            }
                            break;
                        case Session.DONE:
                            decrementMemory(task.getExecutionNodeHostName(), task.getUsedRAM());
                            if (session instanceof DefaultSession) {
                                task.setLog(((DefaultSession) this.session).getJobOutput(task.getResourceId()));
                            }
                            // Just mark the task as finished with success status
                            if (task.getExecutionStatus() == ExecutionStatus.RUNNING) {
                                // Only a running task can complete
                                markTaskFinished(task, ExecutionStatus.PENDING_FINALISATION);
                            }
                            if (task instanceof ProcessingExecutionTask) {
                                ProcessingExecutionTask executionTask = (ProcessingExecutionTask) task;
                                cleanupTmpFolder(executionTask);
                                notifyExternalListeners(executionTask, ExecutionStatus.PENDING_FINALISATION.friendlyName(), 0, null);
                            }
                            if (masterHost.equals(task.getExecutionNodeHostName()) && !SystemUtils.IS_OS_WINDOWS) {
                                String taskOutputLocation = task.getInstanceTargetOutput();
                                if (taskOutputLocation != null) {
                                    DockerVolumeMap volumeMap = ExecutionConfiguration.getMasterContainerVolumeMap();
                                    String location = taskOutputLocation;
                                    if (taskOutputLocation.startsWith(volumeMap.getContainerWorkspaceFolder())) {
                                        location = taskOutputLocation.replace(volumeMap.getContainerWorkspaceFolder(),
                                                                              volumeMap.getHostWorkspaceFolder());
                                    }
                                    FileUtilities.takeOwnership(Paths.get(location).getParent());
                                }
                            }
                            break;
                        case Session.FAILED:
                            String error;
                            int code;
                            decrementMemory(task.getExecutionNodeHostName(), task.getUsedRAM());
                            if (session instanceof DefaultSession) {
                                final DefaultSession defaultSession = (DefaultSession) this.session;
                                String resourceId = task.getResourceId();
                                error = defaultSession.getJobOutput(resourceId);
                                code = defaultSession.getJobExitCode(resourceId);
                                defaultSession.cleanupJob(resourceId);
                            } else {
                                error = "n/a";
                                code = -255;
                            }
                            task.setLog(error);
                            taskProvider.update(task);
                            markTaskFinished(task, ExecutionStatus.FAILED);
                            logger.warning(String.format("Task %s FAILED. Process output: %s", taskName, error));
                            if (task instanceof ProcessingExecutionTask) {
                                ProcessingExecutionTask executionTask = (ProcessingExecutionTask) task;
                                cleanupTmpFolder(executionTask);
                                notifyExternalListeners(executionTask, ExecutionStatus.FAILED.friendlyName(), code, error);
                            }
                            break;
                    }
                } catch (Exception  e) {
                    logger.severe(String.format("%s: Cannot get the status for the task %s [%s]",
                                                e.getClass().getName(), taskName, e.getMessage()));
                    markTaskFinished(task, ExecutionStatus.FAILED);
                }
            }
        }
    }

    @Override
    public String defaultId() { return "DRMAAExecutor"; }

    public String getDRMName() { return session != null ? session.serviceName() : "n/a"; }

    public String getDRMVersion() { return session != null ? session.getVersion().toString() : "n/a"; }

    private void decrementMemory(String host, long amount) {
        Tuple<NodeDescription, AtomicLong> tuple = this.hostMemoryRequests.get(host);
        if (tuple != null) {
            tuple.getKeyTwo().getAndAdd(-amount);
        }
    }

    private void cleanupTmpFolder(ProcessingExecutionTask task) throws IOException {
        final Iterator<Map.Entry<Path, Set<Long>>> iterator = this.cachedInputs.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Path, Set<Long>> entry = iterator.next();
            Set<Long> referredByTasks = entry.getValue();
            if (referredByTasks.contains(task.getId())) {
                referredByTasks.remove(task.getId());
                if (referredByTasks.isEmpty()) {
                    Path path = entry.getKey();
                    if (Files.isDirectory(path)) {
                        FileUtilities.deleteTree(path);
                    } else {
                        Files.delete(path);
                    }
                    iterator.remove();
                }
            }
        }
    }

    private Void executeImpl(Tuple<JobTemplate, ProcessingExecutionTask> pair) {
        final JobTemplate jt = pair.getKeyOne();
        final ProcessingExecutionTask task = pair.getKeyTwo();
        logger.finest(String.format("Task %s has been dequeued for execution", task.getId()));
        try {
            final Tuple<NodeDescription, AtomicLong> hostData = this.hostMemoryRequests.get(task.getExecutionNodeHostName());
            if (hostData != null) {
                int installed = hostData.getKeyOne().getFlavor().getMemory() * 1024;
                long requested = hostData.getKeyTwo().get();
                if (installed <= requested) {
                    logger.warning(String.format("Host %s may not have enough available memory (installed: %d, requested: %d)",
                                                 task.getExecutionNodeHostName(), installed, requested));
                }
            }
            // On master node, if the input of the task is on object storage, we need to copy it locally first
            List<String> parentIds = task.getJob().getTaskDependencies().get(String.valueOf(task.getId()));
            Map<Long, Boolean> map = task.getJob().getTasks().stream().collect(Collectors.toMap(ExecutionTask::getId,
                                                                                                t -> t instanceof DataSourceExecutionTask));
            if (masterHost.equals(task.getExecutionNodeHostName()) && parentIds.stream().anyMatch(i -> map.get(Long.parseLong(i)))) {
                List<Variable> inputs = task.getInputParameterValues();
                Set<String> outputs = task.getOutputParameterValues().stream().map(Variable::getKey).collect(Collectors.toSet());
                for (Variable input : inputs) {
                    if (!outputs.contains(input.getKey())) {
                        String value = input.getValue();
                        String newValue = copyLocally(value, task);
                        input.setValue(newValue);
                        if (!value.equals(newValue)) {
                            List args = jt.getArgs();
                            List<String> newArgs = new ArrayList<>();
                            for (Object arg : args) {
                                newArgs.add(arg.toString().replace(value, newValue));
                            }
                            jt.setArgs(newArgs);
                        }
                    }
                }
            }
            String id = session.runJob(jt);
            session.deleteJobTemplate(jt);
            if (id == null) {
                final String message = String.format("Unable to run job (id null) for task %s", task.getId());
                logger.severe(message);
                changeTaskStatus(task, ExecutionStatus.FAILED);
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
            changeTaskStatus(task, session instanceof DefaultSession
                                        ? task instanceof ScriptTask
                                            ? ExecutionStatus.DONE
                                            : ExecutionStatus.RUNNING
                                        : ExecutionStatus.QUEUED_ACTIVE);
            notifyExternalListeners(task, null, -1, null);
        } catch (Exception e) {
            final String message = String.format("Error submitting task with id %s: %s", task.getId(), e.getMessage());
            logger.severe(message);
            changeTaskStatus(task, ExecutionStatus.FAILED);
            notifyExternalListeners(task, message, -1, null);
        }
        return null;
    }

    private JobTemplate createJobTemplate(ProcessingExecutionTask task) throws DrmaaException, IOException, PersistenceException {
        // Get from the component the execution command
        JobTemplate jt;
        try {
            logger.entering(getClass().getSimpleName(), "createJobTemplate(" + task.getId() + ")");
            final String taskName = TaskUtilities.getTaskName(task);
            String[] pArgs = null;
            final ProcessingComponent component = task.getComponent();
            final Container container = containerProvider.get(component.getContainerId());
            final Application app = container.getApplications()
                    .stream()
                    .filter(a -> component.getId().endsWith(a.getName().toLowerCase()))
                    .findFirst().orElse(null);
            jt = session.createJobTemplate();
            if (jt == null) {
                throw new ExecutionException("Error creating job template from the session!");
            }
            if (jt instanceof JobTemplateExtension && task instanceof ScriptTask) {
                final JobTemplateExtension template = (JobTemplateExtension) jt;
                template.setAttribute(DrmaaJobExtensions.SIMULATE_EXECUTION_ATTRIBUTE, Boolean.TRUE);
                template.setAttribute(DrmaaJobExtensions.TASK_ID, task.getId());
                template.setAttribute(DrmaaJobExtensions.TASK_ANCESTOR_ID, TaskUtilities.getParentIds(task));
                template.setAttribute(DrmaaJobExtensions.TASK_NAME, TaskUtilities.getComponentFor(task).getLabel());
                template.setAttribute(DrmaaJobExtensions.JOB_ID, task.getJob().getId());
                final List<Variable> values = task.getOutputParameterValues();
                if (values != null) {
                    template.setAttribute(DrmaaJobExtensions.TASK_OUTPUT, values.stream().map(Variable::getValue).collect(Collectors.toList()));
                }
            }
            final long memory = app != null
                                ? app.getMemoryRequirements()
                                : container.getApplications().stream().mapToLong(Application::getMemoryRequirements).min().orElse(0L);
            setMemoryConstraint(jt, memory);
            final String preferredHostName = task.getExecutionNodeHostName();
            final NodeData nodeData;
            if (preferredHostName != null) {
                nodeData = getNode(preferredHostName);
            } else {
                if (task instanceof ScriptTask) {
                    nodeData = NodeManager.getInstance().getMasterNode();
                } else {
                    // the next call blocks until a node is available
                    nodeData = getAvailableNode(component.getParallelism(), memory);
                }
            }
            final NodeDescription node = nodeData.getNode();
            if (node == null) {
                throw new TryLaterException("Cannot obtain an available node [null]");
            } else {
                logger.info("Task " + taskName + " will be submitted to host " + node.getId());
            }
            setHost(jt, node.getId());
            task.setExecutionNodeHostName(node.getId());
            final int cpu = nodeData.getCpu();
            final long mem = nodeData.getMemory();
            String location = task.getComponent().getFileLocation();
            if ((SystemUtils.IS_OS_WINDOWS && !location.startsWith("/") && !Paths.get(location).isAbsolute()) ||
                    (SystemUtils.IS_OS_LINUX && !Paths.get(location).isAbsolute())) {
                String path = container.getApplicationPath();
                path = path == null ? "" : path;
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
                    pArgs = app.parallelArguments(Integer.class,
                                                  masterHost.equals(node.getId()) ? Runtime.getRuntime().availableProcessors() / 2 + 2 : cpu);
                } else if (type.isAssignableFrom(Long.class)) {
                    pArgs = app.parallelArguments(Integer.class,
                                                  masterHost.equals(node.getId()) ? Runtime.getRuntime().availableProcessors() / 2 + 2 : cpu);
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
                    e.printStackTrace();
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
                        unit.addVolumeMapping(hostFolder, volumeMount.getContainerTemporaryFolder());
                    }
                    hostFolder = volumeMount.getHostEODataFolder();
                    if (!StringUtilities.isNullOrEmpty(hostFolder)) {
                        unit.addVolumeMapping(hostFolder, volumeMount.getContainerEoDataFolder());
                    }
                    unit.addVolumeMapping(volumeMount.getHostConfigurationFolder(),"/home/.snap/:z");
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
                                                  task.getInstanceTargetOutput(), isLocal);
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
                        if (!StringUtilities.isNullOrEmpty(volumeMap.getHostTemporaryFolder())) {
                            add("-v");
                            add(volumeMap.getHostTemporaryFolder() + ":" + volumeMap.getContainerTemporaryFolder());
                        }
                        if (!StringUtilities.isNullOrEmpty(volumeMap.getHostConfigurationFolder())) {
                            add("-v");
                            add(volumeMap.getHostConfigurationFolder() + ":" + volumeMap.getContainerConfigurationFolder());
                        }
                        add("-v");
                        add(volumeMap.getHostConfigurationFolder() + ":/home/.snap/:z");
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
                    if (value != null) {
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
            if (!this.hostMemoryRequests.containsKey(node.getId())) {
                this.hostMemoryRequests.put(node.getId(), new Tuple<>(node, new AtomicLong(task.getUsedRAM())));
            } else {
                this.hostMemoryRequests.get(node.getId()).getKeyTwo().addAndGet(task.getUsedRAM());
            }
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

    private String wrapShellInvocation(ProcessingComponent component, final List<String> argsList, String initialCmd,
                                       final String tempOutput, final String finalOutput, final boolean isLocal) {
        final boolean isOutputManaged = component.isOutputManaged();
        final String tmpPath = isOutputManaged ? tempOutput : tempOutput.substring(0, tempOutput.lastIndexOf('/') + 1) + "*.*";
        //final String finalPath = isOutputManaged ? finalOutput : finalOutput.substring(0, finalOutput.lastIndexOf('/') + 1);
        final String finalPath = finalOutput.substring(0, finalOutput.lastIndexOf('/') + 1);
        final boolean isSnapDimap = component.getTargets().stream().anyMatch(t -> t.getDataDescriptor().getLocation().toLowerCase().endsWith(".dim")
                                                                                || "beam-dimap".equalsIgnoreCase(t.getDataDescriptor().getFormatName()))
                                    && finalOutput.endsWith(".dim");
        final boolean isSnap = initialCmd.equals("/opt/snap/bin/gpt");
        final boolean recursiveCondition = !isOutputManaged || isSnapDimap || component.getTargets().stream().anyMatch(t -> DataFormat.FOLDER == t.getDataDescriptor().getFormatType());
        argsList.add(0, initialCmd);
        if (!isLocal && component.getTargets().stream().anyMatch(t -> t.getDataDescriptor().getFormatType() == DataFormat.FOLDER)) {
            argsList.add(0, "mkdir");
            argsList.add(1, tmpPath);
            argsList.add(2, "&&");
        }
        if (isSnap && isOutputManaged && tempOutput.toLowerCase().endsWith(".tif")) {
            argsList.add("-f");
            argsList.add("GeoTIFF");
        }
        argsList.add("&&");
        argsList.add("cp");
        if (recursiveCondition) {
            argsList.add("-r");
        }
        if (isSnapDimap) {
            argsList.add(tmpPath.replace(tmpPath.substring(tmpPath.lastIndexOf('.')), ".*"));
        } else {
            argsList.add(tmpPath);
        }
        try {
            Files.createDirectory(Paths.get(finalPath));
        } catch (IOException e) {
            logger.warning(e.getMessage());
        }
        argsList.add(finalPath);
        argsList.add("&&");
        argsList.add("rm");
        if (recursiveCondition) {
            argsList.add("-r");
        }
        if (isSnapDimap) {
            argsList.add(tmpPath.replace(tmpPath.substring(tmpPath.lastIndexOf('.')), ".*"));
        } else {
            argsList.add(tmpPath);
        }
        // all the existing args are merged into a single string as argument to -c
        final String quoteChar = isLocal
                ? SystemUtils.IS_OS_WINDOWS
                    ? "\""
                    : ""
                : "\"";
        final String singleArg = quoteChar + String.join(" ", argsList) + quoteChar;
        argsList.clear();
        argsList.add("-c");
        argsList.add(singleArg);
        return "/bin/bash";
    }

    private void setMemoryConstraint(JobTemplate jt, long memory) throws DrmaaException {
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE) &&
                    ExecutionConfiguration.forceMemoryConstraint() && memory > 0) {
                job.setAttribute(DrmaaJobExtensions.MEMORY_REQUIREMENTS_ATTRIBUTE, memory);
            }
        }
    }

    private void setHost(JobTemplate jt, String host) throws DrmaaException {
        if (jt instanceof JobTemplateExtension) {
            JobTemplateExtension job = (JobTemplateExtension) jt;
            if (job.hasAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE)) {
                job.setAttribute(DrmaaJobExtensions.NODE_ATTRIBUTE, host);
            }
        }
    }

    private NodeData getAvailableNode(int cpus, long memory) throws DrmaaException {
    	NodeData nodeData = null;
        if (NodeManager.isAvailable() && NodeManager.getInstance() != null) {
        	nodeData = NodeManager.getInstance().getAvailableNode(cpus, memory, 0);
            logger.finest(String.format("Node [%s] was chosen for next execution", nodeData.getNode().getId()));
        }
        if (nodeData == null) {
            throw new DeniedByDrmException("No node was found for execution");
        }
        return nodeData;
    }

    private NodeData getNode(String nodeName) throws DrmaaException {
        NodeData nodeData = null;
        if (NodeManager.isAvailable() && NodeManager.getInstance() != null) {
            nodeData = NodeManager.getInstance().getNode(nodeName);
        }
        if (nodeData == null) {
            throw new DeniedByDrmException(String.format("Node '%s' was not found", nodeName));
        }
        return nodeData;
    }

    private String copyLocally(String source, ExecutionTask task) throws IOException {
        if (!source.startsWith("/")) {
            return source;
        }
        DockerVolumeMap masterVolumeMap = ExecutionConfiguration.getMasterContainerVolumeMap();
        final Path realPath;
        if (source.startsWith(masterVolumeMap.getHostWorkspaceFolder())) {
            realPath = Paths.get(source).toRealPath();
        } else {
            realPath = Paths.get(source.replace(masterVolumeMap.getContainerWorkspaceFolder(),
                                                SystemVariable.ROOT.value().replace("\\", "/"))).toRealPath();
        }
        if (realPath.startsWith(masterVolumeMap.getHostEODataFolder())) {
            Path src = realPath;
            String lastName = null;
            if (Files.isRegularFile(realPath)) {
                src = realPath.getParent();
                lastName = realPath.getFileName().toString();
            }
            Path tmpFolder = Paths.get(masterVolumeMap.getHostTemporaryFolder());
            Path tmpPath = tmpFolder.resolve(src.getFileName());
            if (!Files.exists(tmpPath)) {
                logger.fine(String.format("Copying %s to %s", src, tmpFolder));
                //FileUtilities.copy(src, tmpFolder);
                FileUtilities.copy(src, tmpFolder,
                                   new ProgressNotifier(() -> task.getJob().getUserName(),
                                                        task.getJob().getName(), DataSourceTopic.PRODUCT_PROGRESS));
            }
            if (!this.cachedInputs.containsKey(tmpPath)) {
                this.cachedInputs.put(tmpPath, new HashSet<>());
            }
            this.cachedInputs.get(tmpPath).add(task.getId());
            String out = lastName != null ? tmpPath.resolve(lastName).toString() : tmpPath.toString();
            return out.replace(masterVolumeMap.getHostTemporaryFolder(), masterVolumeMap.getContainerTemporaryFolder());
        } else {
            return source;
        }
    }

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
}
