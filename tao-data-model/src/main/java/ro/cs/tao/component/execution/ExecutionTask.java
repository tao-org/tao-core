package ro.cs.tao.component.execution;

import ro.cs.tao.component.ProcessingComponent;

import java.time.LocalDateTime;

/**
 * Created by cosmin on 9/19/2017.
 */
public class ExecutionTask {
    private ProcessingComponent processingComponent;
    private ExecutionStatus executionStatus = ExecutionStatus.UNDETERMINED;
    private String id;
    private String taskName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ExecutionTask() {
    }

    public ExecutionTask(ProcessingComponent processingComponent) {
        this.processingComponent = processingComponent;
    }

    public ExecutionTask(ProcessingComponent processingComponent, String taskName) {
        this.processingComponent = processingComponent;
        this.taskName = taskName;
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

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return taskName;
    }
}
