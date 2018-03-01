package ro.cs.tao.orchestration;

import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.execution.ExecutionGroup;
import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.ExecutionsManager;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class Orchestrator {

    private final Logger logger = Logger.getLogger(Orchestrator.class.getSimpleName());
    private PersistenceManager persistenceManager;

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public void startWorkflow(long workflowId) throws ExecutionException {
        try {
            ExecutionJob job = persistenceManager.getJob(workflowId);
            if (job != null) {
                ExecutionStatus status = job.getExecutionStatus();
                if (status == ExecutionStatus.RUNNING || status == ExecutionStatus.QUEUED_ACTIVE) {
                    throw new ExecutionException("Workflow already running");
                }
            } else {
                WorkflowDescriptor descriptor = persistenceManager.getWorkflowDescriptor(workflowId);
                job = create(descriptor);
            }
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            ExecutionTask firstTask = null;
            switch (job.getExecutionStatus()) {
                case UNDETERMINED:
                    firstTask = job.getNext();
                    executionsManager.execute(firstTask);
                    job.setExecutionStatus(ExecutionStatus.QUEUED_ACTIVE);
                    persistenceManager.saveExecutionJob(job);
                    break;
                case SUSPENDED:
                    List<ExecutionTask> tasks = job.getTasks();
                    for (ExecutionTask task : tasks) {
                        if (task.getExecutionStatus() == ExecutionStatus.SUSPENDED) {
                            firstTask = task;
                            break;
                        }
                    }
                    if (firstTask == null) {
                        throw new ExecutionException("Inconsistent job state (job SUSPENDED but no task in this state");
                    }
                    executionsManager.resume(firstTask);
                    break;
            }
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
            throw new ExecutionException(e.getMessage());
        }
    }

    public void stopWorkflow(long workflowId) {
        try {
            ExecutionsManager executionsManager = ExecutionsManager.getInstance();
            ExecutionJob job = persistenceManager.getJob(workflowId);
            if (job == null) {
                throw new ExecutionException(String.format("No job exists for workflow %s", workflowId));
            }
            ExecutionStatus status = job.getExecutionStatus();
            if (status != ExecutionStatus.RUNNING && status != ExecutionStatus.QUEUED_ACTIVE) {
                throw new ExecutionException(String.format("The job %s for workflow %s is in a state that doesn't allow stopping",
                        job.getId(), workflowId));
            }
            List<ExecutionTask> tasks = job.getTasks();
            ExecutionTask currentTask = null;
            for (ExecutionTask task : tasks) {
                if (task.getExecutionStatus() == ExecutionStatus.RUNNING) {
                    currentTask = task;
                    break;
                }
            }
            if (currentTask == null) {
                throw new ExecutionException("Inconsistent job state (job SUSPENDED but no task in this state");
            }
            executionsManager.stop(currentTask);
            job.setExecutionStatus(ExecutionStatus.DONE);
        } catch (PersistenceException e) {
            logger.severe(e.getMessage());
        }
    }

    private ExecutionJob create(WorkflowDescriptor workflow) throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            List<WorkflowNodeDescriptor> nodes = workflow.getNodes();
            for (WorkflowNodeDescriptor node : nodes) {
                ExecutionTask task = createTask(node);
                job.addTask(task);
                persistenceManager.saveExecutionTask(task, job);
            }
            persistenceManager.saveExecutionJob(job);
        }
        return job;
    }

    private ExecutionTask createGroup(WorkflowNodeGroupDescriptor groupNode) throws PersistenceException {
        ExecutionGroup group = new ExecutionGroup();
        List<WorkflowNodeDescriptor> nodes = groupNode.getNodes();
        for (WorkflowNodeDescriptor node : nodes) {
            ExecutionTask task = createTask(node);
            group.addTask(task);
        }
        group.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        return group;
    }

    private ExecutionTask createTask(WorkflowNodeDescriptor workflowNode) throws PersistenceException {
        ExecutionTask task;
        if (workflowNode instanceof WorkflowNodeGroupDescriptor) {
            task = createGroup((WorkflowNodeGroupDescriptor) workflowNode);
        } else {
            task = new ExecutionTask();
            task.setWorkflowNodeId(workflowNode.getId());
            ProcessingComponent component = persistenceManager.getProcessingComponentById(workflowNode.getComponentId());
            List<ParameterValue> customValues = workflowNode.getCustomValues();
            task.setProcessingComponent(component);
            List<ComponentLink> links = workflowNode.getIncomingLinks();
            if (links != null) {
                links.forEach(link -> {
                    String name = link.getInput().getId();
                    String value = link.getInput().getDataDescriptor().getLocation();
                    task.setParameterValue(name, value);
                });
            }
            if (customValues != null) {
                customValues.forEach(v -> task.setParameterValue(v.getParameterName(), v.getParameterValue()));
            }
            String nodeAffinity = component.getNodeAffinity();
            if (nodeAffinity != null) {
                task.setExecutionNodeHostName(nodeAffinity);
            }
            task.setExecutionStatus(ExecutionStatus.UNDETERMINED);
        }
        return task;
    }
}
