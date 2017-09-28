package ro.cs.tao.component.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cosmin on 9/21/2017.
 */
public class ExecutionJob {
    private Long id;
    private String resourceId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private ExecutionStatus executionStatus;
    private List<ExecutionTask> tasks = new ArrayList<>();

    public ExecutionJob() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceId() {
        return resourceId;
    }

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
}
