package ro.cs.tao.component.execution;

import ro.cs.tao.component.ParameterDescriptor;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.component.validation.ValidationException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by cosmin on 9/19/2017.
 */
public class ExecutionTask {
    private long id;
    private long workflowNodeId;
    private ProcessingComponent processingComponent;
    private ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;
    private String resourceId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<Variable> inputParameterValues;
    private ExecutionJob job;

    public ExecutionTask() { }

    public ExecutionTask(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getWorkflowNodeId() { return workflowNodeId; }
    public void setWorkflowNodeId(long workflowNodeId) { this.workflowNodeId = workflowNodeId; }

    public void setProcessingComponent(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }
    public ProcessingComponent getProcessingComponent() {
        return processingComponent;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }
    public String getResourceId() {
        return resourceId;
    }

    public String getExecutionNodeHostName() {
        return executionNodeHostName;
    }
    public void setExecutionNodeHostName(String executionNodeHostName) {
        this.executionNodeHostName = executionNodeHostName;
    }

    public List<Variable> getInputParameterValues() {
        return inputParameterValues;
    }
    public void setInputParameterValues(List<Variable> inputParameterValues) {
        this.inputParameterValues = inputParameterValues;
    }
    public void setParameterValue(String parameterId, String value) {
        List<ParameterDescriptor> descriptorList = this.processingComponent.getParameterDescriptors();
        boolean descriptorExists = false;
        for(ParameterDescriptor descriptor: descriptorList) {
            if (descriptor.getId().equals(parameterId)) {
                descriptorExists = true;
                break;
            }
        }
        if(!descriptorExists) {
            throw new ValidationException("The parameter ID " + parameterId +
                    " does not exists in the processing component " +
                    processingComponent.getLabel());
        }
        if (this.inputParameterValues == null) {
            this.inputParameterValues = new ArrayList<>();
        }
        this.inputParameterValues.add(new Variable(parameterId, value));
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public ExecutionJob getJob() {
        return job;
    }
    public void setJob(ExecutionJob job) {
        this.job = job;
    }

    public String buildExecutionCommand() {
        if (processingComponent == null) {
            return null;
        }
        Map<String, String> inputParams = new HashMap<>();
        if (inputParameterValues != null) {
            inputParams.putAll(inputParameterValues.stream()
                                       .collect(Collectors.toMap(Variable::getKey, Variable::getValue)));
        }
        return this.processingComponent.buildExecutionCommand(inputParams);
    }
}
