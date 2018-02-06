package ro.cs.tao.component.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cosmin on 9/21/2017.
 */
public class ExecutionJob {
    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long workflowId;
    private ExecutionStatus executionStatus;
    private List<ExecutionTask> tasks;

    public ExecutionJob() {}

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    public long getWorkflowId() { return workflowId; }
    public void setWorkflowId(long workflowId) { this.workflowId = workflowId; }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setTasks(List<ExecutionTask> tasks) {
        this.tasks = tasks;
    }
    public List<ExecutionTask> getTasks() {
        return tasks;
    }

    public void addTask(ExecutionTask task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        this.tasks.add(task);
    }
}
