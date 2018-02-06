package ro.cs.tao.orchestration;

import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.execution.ExecutionJob;
import ro.cs.tao.component.execution.ExecutionStatus;
import ro.cs.tao.component.execution.ExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.ParameterValue;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Cosmin Cara
 */
public class Orchestrator {

    private PersistenceManager persistenceManager;

    public void setPersistenceManager(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public ExecutionJob create(WorkflowDescriptor workflow) throws PersistenceException {
        ExecutionJob job = null;
        if (workflow != null && workflow.isActive()) {
            job = new ExecutionJob();
            job.setStartTime(LocalDateTime.now());
            job.setWorkflowId(workflow.getId());
            job.setExecutionStatus(ExecutionStatus.UNDETERMINED);
            List<WorkflowNodeDescriptor> nodes = workflow.getNodes();
            for (WorkflowNodeDescriptor node : nodes) {
                job.addTask(createTask(node));
            }
        }
        return job;
    }

    private ExecutionTask createTask(WorkflowNodeDescriptor workflowNode) throws PersistenceException {
        ExecutionTask task = new ExecutionTask();
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
        return task;
    }
}
