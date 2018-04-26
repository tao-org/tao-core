/*
 * Copyright (C) 2017 CS ROMANIA
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
package ro.cs.tao.orchestration;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.component.Variable;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.MetadataInspector;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Notifiable;
import ro.cs.tao.messaging.Topics;
import ro.cs.tao.orchestration.commands.JobCommand;
import ro.cs.tao.orchestration.commands.TaskCommand;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.security.SystemPrincipal;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * @author Cosmin Cara
 */
public class Orchestrator extends Notifiable {

    private static final Orchestrator instance;
    private final ExecutorService backgroundWorker;
    private final Map<Long, InternalStateHandler> groupStateHandlers;
    private final BlockingQueue<Message> queue;

    static {
        instance = new Orchestrator();
    }

    public static Orchestrator getInstance() { return instance; }

    private final Logger logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    private PersistenceManager persistenceManager;
    private TaskSelector<ExecutionGroup> groupTaskSelector;
    private TaskSelector<ExecutionJob> jobTaskSelector;
    private JobFactory jobFactory;
    private MetadataInspector metadataInspector;

    private Orchestrator() {
        this.backgroundWorker = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        this.groupStateHandlers = new HashMap<>();
        this.queue = new LinkedBlockingDeque<>();
        subscribe(Topics.TASK_STATUS_CHANGED);
    }

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        JobCommand.setPersistenceManager(persistenceManager);
        TaskCommand.setPersistenceManager(persistenceManager);
        Set<TaskSelector> selectors = ServiceRegistryManager.getInstance().getServiceRegistry(TaskSelector.class).getServices();
        final Function<Long, WorkflowNodeDescriptor> workflowProvider = this.persistenceManager::getWorkflowNodeById;
        final BiFunction<Long, Long, ExecutionTask> taskByGroupNodeProvider = this.persistenceManager::getTaskByGroupAndNode;
        final BiFunction<Long, Long, ExecutionTask> taskByJobNodeProvider = this.persistenceManager::getTaskByJobAndNode;
        final BiFunction<Long, String, List<WorkflowNodeDescriptor>> nodesByComponentProvider = this.persistenceManager::getWorkflowNodesByComponentId;
        this.groupTaskSelector = selectors.stream()
                                          .filter(s -> ExecutionGroup.class.equals(s.getTaskContainerClass()))
                                          .map(s -> (TaskSelector<ExecutionGroup>)s)
                                          .findFirst()
                                          .orElse(new DefaultGroupTaskSelector());
        this.groupTaskSelector.setWorkflowProvider(workflowProvider);
        this.groupTaskSelector.setTaskByNodeProvider(taskByGroupNodeProvider);
        this.groupTaskSelector.setNodesByComponentProvider(nodesByComponentProvider);
        this.jobTaskSelector = selectors.stream()
                                        .filter(s -> ExecutionJob.class.equals(s.getTaskContainerClass()))
                                        .map(s -> (TaskSelector<ExecutionJob>)s)
                                        .findFirst()
                                        .orElse(new DefaultJobTaskSelector());
        this.jobTaskSelector.setWorkflowProvider(workflowProvider);
        this.jobTaskSelector.setTaskByNodeProvider(taskByJobNodeProvider);
        this.jobTaskSelector.setNodesByComponentProvider(nodesByComponentProvider);
        this.jobFactory = new JobFactory(this.persistenceManager);
        Set<MetadataInspector> services = ServiceRegistryManager.getInstance()
                                                                .getServiceRegistry(MetadataInspector.class)
                                                                .getServices();
        if (services != null) {
            this.metadataInspector = services.stream().findFirst().get();
        }
        QueueMonitor monitor = new QueueMonitor();
        monitor.setName("orchestrator");
        monitor.start();
    }

    /**
     * Creates a job from a workflow definition and starts its execution.
     *
     * @param workflowId    The workflow identifier
     *
     * @throws ExecutionException   In case anything goes wrong or a job for this workflow was already created
     */
    public long startWorkflow(long workflowId, Map<String, String> inputs) throws ExecutionException {
        try {
            List<ExecutionJob> jobs = persistenceManager.getJobs(workflowId);
            if (jobs == null || jobs.size() == 0 || jobs.stream().noneMatch(j -> checkExistingJob(j, inputs))) {
                WorkflowDescriptor descriptor = persistenceManager.getWorkflowDescriptor(workflowId);
                if (descriptor == null) {
                    throw new ExecutionException(String.format("Non-existent workflow [%s]", workflowId));
                }
                final ExecutionJob executionJob = this.jobFactory.createJob(descriptor, inputs);
                backgroundWorker.submit(() -> JobCommand.START.applyTo(executionJob));
                return executionJob.getId();
            } else {
                throw new ExecutionException(
                        String.format("A job for the workflow [%s] with the same input values is already running",
                                      workflowId));
            }
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    /**
     * Stops the execution of the job corresponding to this workflow.
     *
     * @param workflowId    The workflow identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job for this workflow
     */
    public void stopWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.STOP.applyTo(job);
    }

    /**
     * Pauses (suspends) the execution of the job corresponding to this workflow.
     *
     * @param workflowId    The workflow identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job for this workflow
     */
    public void pauseWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.SUSPEND.applyTo(job);
    }

    /**
     * Resumes the execution of the job corresponding to this workflow.
     *
     * @param workflowId    The workflow identifier
     * @throws ExecutionException   In case anything goes wrong or there was no job for this workflow
     */
    public void resumeWorkflow(long workflowId) throws ExecutionException {
        ExecutionJob job = checkWorkflow(workflowId);
        JobCommand.RESUME.applyTo(job);
    }

    @Override
    protected void onMessageReceived(Message message) {
        try {
            queue.put(message);
        } catch (InterruptedException e) {
            logger.severe(e.getMessage());
        }
    }

    private void processMessage(Message message) {
        try {
            String taskId = message.getItem(Message.SOURCE_KEY);
            ExecutionStatus status = ExecutionStatus.getEnumConstantByValue(Integer.parseInt(message.getItem(Message.PAYLOAD_KEY)));
            if (status == null) {
                throw new PersistenceException(String.format("Invalid status received: %s",
                                                             message.getItem(Message.PAYLOAD_KEY)));
            }
            ExecutionTask task = persistenceManager.getTaskById(Long.parseLong(taskId));
            logger.fine(String.format("Status change for task %s [node %s]: %s",
                                      taskId,
                                      task.getWorkflowNodeId(),
                                      status.name()));
            statusChanged(task);
            if (status == ExecutionStatus.DONE) {
                WorkflowNodeDescriptor taskNode = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
                // For DataSourceExecutionTask, it is the executor that sets the outputs,
                // hence we need to "confirm" here the outputs of a processing task.
                if (task instanceof ProcessingExecutionTask) {
                    ProcessingExecutionTask pcTask = (ProcessingExecutionTask) task;
                    List<TargetDescriptor> targets = pcTask.getComponent().getTargets();
                    ExecutionGroup groupTask = pcTask.getGroupTask();
                    boolean lastFromGroup = groupTask != null &&
                            groupTask.getOutputParameterValues().stream()
                                    .allMatch(o -> targets.stream().anyMatch(t -> t.getName().equals(o.getKey())));
                    targets.forEach(t -> {
                        String targetOuptut = pcTask.getInstanceTargetOuptut(t);
                        pcTask.setOutputParameterValue(t.getName(), targetOuptut);
                        if (taskNode.getPreserveOutput()) {
                            try {
                                persistOutputProducts(pcTask);
                            } catch (PersistenceException e) {
                                logger.warning(e.getMessage());
                            }
                        }
                        if (lastFromGroup) {
                            groupTask.setOutputParameterValue(t.getName(), targetOuptut);
                        }
                        logger.fine(String.format("Task %s output: %s=%s",
                                                  task.getId(), t.getName(),
                                                  t.getDataDescriptor().getLocation()));
                    });
                    persistenceManager.updateExecutionTask(task);
                    if (lastFromGroup) {
                        persistenceManager.updateExecutionTask(groupTask);
                    }
                }
                List<ExecutionTask> nextTasks = findNextTasks(task);
                if (nextTasks != null && nextTasks.size() > 0) {
                    logger.fine(String.format("Has %s next tasks", nextTasks.size()));
                    for (ExecutionTask nextTask : nextTasks) {
                        if (nextTask != null) {
                            if (task instanceof DataSourceExecutionTask && nextTask instanceof ExecutionGroup) {
                                ExecutionGroup groupTask = (ExecutionGroup) nextTask;
                                // A DataSourceExecutionTask outputs the list of results as a JSON
                                List<Variable> values = task.getOutputParameterValues();
                                int cardinality = 0;
                                if (values != null && values.size() > 0) {
                                    cardinality = new StringListAdapter().marshal(values.get(0).getValue()).size();
                                }
                                groupTask.setStateHandler(new LoopStateHandler(new LoopState(cardinality, 1)));
                                // we need to keep this mapping because ExecutionTask.stateHandler is transient
                                this.groupStateHandlers.put(groupTask.getId(), groupTask.getStateHandler());
                                if (values != null && values.size() > 0) {
                                    Variable theOnlyValue = values.get(0);
                                    groupTask.setInputParameterValue(groupTask.getInputParameterValues().get(0).getKey(),
                                                                     theOnlyValue.getValue());
                                }
                                persistenceManager.updateExecutionTask(groupTask);
                            }
                            logger.fine(String.format("Task %s about to start.", nextTask.getId()));
                            nextTask.getInputParameterValues().forEach(
                                    v -> logger.fine(String.format("Input: %s=%s", v.getKey(), v.getValue()))
                                                                      );
                            ExecutionGroup parentGroupTask = nextTask.getGroupTask();
                            if (parentGroupTask != null) {
                                String state = parentGroupTask.getInternalState();
                                if (state != null) {
                                    InternalStateHandler handler = this.groupStateHandlers.get(parentGroupTask.getId());
                                    parentGroupTask.setStateHandler(handler);
                                    nextTask.setInternalState(String.valueOf(((LoopState) handler.currentState()).getCurrent()));
                                }
                            }
                            TaskCommand.START.applyTo(nextTask);
                        }
                    }
                } else {
                    logger.fine("No more child tasks to execute after the current task");
                    ExecutionJob job = task.getJob();
                    job.setExecutionStatus(ExecutionStatus.DONE);
                    persistenceManager.updateExecutionJob(job);
                    if (taskNode.getPreserveOutput()) {
                        persistOutputProducts(task);
                    }
                    WorkflowDescriptor workflow = persistenceManager.getWorkflowDescriptor(job.getWorkflowId());
                    Duration time = null;
                    if (job.getStartTime() != null && job.getEndTime() != null) {
                        time = Duration.between(job.getStartTime(), job.getEndTime());
                    }
                    String msg = String.format("Job [%s] for workflow [%s]" +
                                                       (job.getExecutionStatus() == ExecutionStatus.DONE ?
                                                               " completed in %ss" :
                                                               " failed after %ss"),
                                               job.getId(), workflow.getName(), time != null ? time.getSeconds() : "<unknown>");
                    Messaging.send(SystemPrincipal.instance(), Topics.INFORMATION, this, msg);
                }
            }
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
        }
    }

    /**
     * An execution job is considered duplicate if:
     * 1) it is created for the same workflow and
     * 2) it has the same inputs and
     * 3) the previously created job is in one of the following states: UNDETERMINED, QUEUED_ACTIVE, RUNNING
     *
     * @param job       An existing job to check
     * @param inputs    The inputs to be checked against
     * @return          <code>true</code> if the given job doesn't match the inputs, <code>false</code> otherwise
     */
    private boolean checkExistingJob(ExecutionJob job, Map<String, String> inputs) {
        boolean existing = false;
        switch (job.getExecutionStatus()) {
            case UNDETERMINED:
            case QUEUED_ACTIVE:
            case RUNNING:
                List<ExecutionTask> tasks = job.getTasks();
                if (tasks != null && tasks.size() > 0) {
                    ExecutionTask first = tasks.get(0);
                    existing = first.getInputParameterValues().stream()
                            .allMatch(i -> inputs.containsKey(i.getKey()) && inputs.get(i.getKey()).equals(i.getValue()));
                }
                break;
            default:
                break;
        }
        return existing;
    }

    private void statusChanged(ExecutionTask task) {
        ExecutionGroup groupTask = task.getGroupTask();
        if (groupTask != null) {
            notifyTaskGroup(groupTask, task);
        } else {
            ExecutionJob job = task.getJob();
            notifyJob(job, task);
        }
    }

    private void notifyTaskGroup(ExecutionGroup groupTask, ExecutionTask task) {
        ExecutionStatus previousStatus = groupTask.getExecutionStatus();
        List<ExecutionTask> tasks = groupTask.getTasks();
        ExecutionStatus taskStatus = task.getExecutionStatus();
        try {
            switch (taskStatus) {
                case QUEUED_ACTIVE:
                case RUNNING:
                    if (groupTask.getStartTime() == null) {
                        groupTask.setStartTime(task.getStartTime() != null ? task.getStartTime() : LocalDateTime.now());
                    }
                    groupTask.setExecutionStatus(taskStatus);
                    break;
                case SUSPENDED:
                case CANCELLED:
                case FAILED:
                    int startIndex = IntStream.range(0, tasks.size())
                                              .filter(i -> task.getId().equals(tasks.get(i).getId()))
                                              .findFirst().orElse(0);
                    bulkSetStatus(tasks.subList(startIndex, tasks.size()), taskStatus);
                    groupTask.setExecutionStatus(taskStatus);
                    break;
                case DONE:
                    // If the task is the last one executing of this group
                    if (tasks.stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
                        if (groupTask.getStateHandler() == null) {
                            groupTask.setStateHandler(groupStateHandlers.get(groupTask.getId()));
                        }
                        LoopState nextState = (LoopState) groupTask.nextInternalState();
                        if (nextState != null) {
                            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                            bulkSetStatus(tasks, ExecutionStatus.UNDETERMINED);
                            groupTask.setExecutionStatus(ExecutionStatus.UNDETERMINED);
                            try {
                                InternalStateHandler handler = groupTask.getStateHandler();
                                if (handler != null) {
                                    groupTask.setInternalState(handler.serializeState());
                                }
                            } catch (SerializationException e) {
                                e.printStackTrace();
                            }
                        } else {
                            groupTask.setExecutionStatus(ExecutionStatus.DONE);
                            groupTask.setEndTime(LocalDateTime.now());
                        }
                    }
                    break;
                default:
                    // do nothing for other states
                    break;
            }
            ExecutionStatus currenStatus = groupTask.getExecutionStatus();
            if (previousStatus != null && previousStatus != currenStatus) {
                persistenceManager.updateExecutionTask(groupTask);
                ExecutionJob job = groupTask.getJob();
                notifyJob(job, groupTask);
            }
        } catch (PersistenceException pex) {
            logger.severe(pex.getMessage());
        }
    }

    private void notifyJob(ExecutionJob job, ExecutionTask changedTask) {
        ExecutionStatus taskStatus = changedTask.getExecutionStatus();
        try {
            switch (taskStatus) {
                case SUSPENDED:
                case CANCELLED:
                case FAILED:
                    List<ExecutionTask> tasks = job.getTasks();
                    bulkSetStatus(tasks, taskStatus);
                    job.setExecutionStatus(taskStatus);
                    persistenceManager.updateExecutionJob(job);
                    break;
                case RUNNING:
                    ExecutionStatus jobStatus = job.getExecutionStatus();
                    if (jobStatus == ExecutionStatus.QUEUED_ACTIVE || jobStatus == ExecutionStatus.UNDETERMINED) {
                        job.setExecutionStatus(ExecutionStatus.RUNNING);
                        persistenceManager.updateExecutionJob(job);
                    }
                    break;
                case DONE:
                    if (job.getTasks().stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
                        job.setExecutionStatus(ExecutionStatus.DONE);
                        job.setEndTime(LocalDateTime.now());
                        persistenceManager.updateExecutionJob(job);
                    }
                    break;
                default:
                    // do nothing for other states
                    break;
            }
        } catch (PersistenceException pex) {
            logger.severe(pex.getMessage());
        }
    }

    private ExecutionJob checkWorkflow(long workflowId) {
        List<ExecutionJob> jobs = persistenceManager.getJobs(workflowId);
        if (jobs == null || jobs.isEmpty()) {
            throw new ExecutionException(String.format("No job exists for workflow %s", workflowId));
        }
        return jobs.stream().filter(j -> j.getExecutionStatus() != ExecutionStatus.DONE
                && j.getExecutionStatus() != ExecutionStatus.CANCELLED
                && j.getExecutionStatus() != ExecutionStatus.FAILED).findFirst().orElse(null);
    }

    private void bulkSetStatus(Collection<ExecutionTask> tasks, ExecutionStatus status) throws PersistenceException {
        if (tasks == null) {
            return;
        }
        tasks.forEach(t -> {
            t.setExecutionStatus(status);
            try {
                persistenceManager.updateExecutionTask(t);
            } catch (PersistenceException e) {
                logger.severe(e.getMessage());
            }
        });
    }

    private List<ExecutionTask> findNextTasks(ExecutionTask task) {
        ExecutionGroup groupTask = task instanceof ExecutionGroup ? (ExecutionGroup) task : task.getGroupTask();
        ExecutionJob job = task.getJob();
        return groupTask != null ? this.groupTaskSelector.chooseNext(groupTask, task) :
                this.jobTaskSelector.chooseNext(job, task);
    }

    private void persistOutputProducts(ExecutionTask task) throws PersistenceException {
        if (metadataInspector != null) {
            List<Variable> values = task.getOutputParameterValues();
            WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
            TaoComponent component;
            if (node instanceof WorkflowNodeGroupDescriptor) {
                component = persistenceManager.getGroupComponentById(node.getComponentId());
            } else {
                component = persistenceManager.getProcessingComponentById(node.getComponentId());
            }
            List<EOProduct> products = new ArrayList<>();
            for (Variable value : values) {
                products.addAll(createProducts(component, value));
            }
            products.forEach(p -> {
                try {
                    p.setUserName(SystemPrincipal.instance().getName());
                    persistenceManager.saveEOProduct(p);
                } catch (PersistenceException e) {
                    logger.severe(e.getMessage());
                }
            });
        }
    }

    private List<EOProduct> createProducts(TaoComponent component, Variable outParam) {
        List<EOProduct> products = new ArrayList<>();
        List<TargetDescriptor> targets = component.getTargets();
        String value = outParam.getValue();
        TargetDescriptor descriptor = targets.stream()
                .filter(t -> t.getName().equals(outParam.getKey()))
                .findFirst()
                .get();
        if (value != null) {
            try {
                // first try to see if it's a list
                List<String> list = new StringListAdapter().marshal(value);
                for (String v : list) {
                    products.add(createProduct(descriptor, v));
                }
            } catch (Exception e) {
                products.add(createProduct(descriptor, value));
            }
        }
        return products;
    }

    private EOProduct createProduct(TargetDescriptor descriptor, String outValue) {
        EOProduct product = null;
        try {
            if (descriptor.getDataDescriptor().getFormatType() == DataFormat.RASTER) {
                Path path = Paths.get(outValue);
                MetadataInspector.Metadata metadata = metadataInspector.getMetadata(path);
                if (metadata != null) {
                    product = metadata.toProductDescriptor(path);
                }
            }
        } catch (Exception e2) {
            logger.severe(e2.getMessage());
        }
        return product;
    }

    private class QueueMonitor extends Thread {
        @Override
        public void run() {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    processMessage(queue.take());
                } catch (InterruptedException e) {
                    logger.severe(e.getMessage());
                }
            }
        }
    }
}
