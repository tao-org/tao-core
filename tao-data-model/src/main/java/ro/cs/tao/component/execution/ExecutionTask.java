package ro.cs.tao.component.execution;

import ro.cs.tao.component.ProcessingComponent;

import java.time.LocalDateTime;

/**
 * Created by cosmin on 9/19/2017.
 */
public class ExecutionTask {
    private Long id;
    private ProcessingComponent processingComponent;
    private ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;
    private String resourceId;
    private String executionNodeHostName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private ExecutionJob job;

    public ExecutionTask() {
    }

    public ExecutionTask(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
