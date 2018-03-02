package ro.cs.tao.component.execution;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by cosmin on 9/21/2017.
 */
public class ExecutionJob implements StatusChangeListener {
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

    public List<ExecutionTask> find(ExecutionStatus status) {
        List<ExecutionTask> running = null;
        if (this.tasks != null && this.tasks.size() > 0) {
            running = this.tasks.stream()
                                .filter(t -> t.getExecutionStatus() == status)
                                .collect(Collectors.toList());
        }
        return running;
    }

    public ExecutionTask getNext() {
        ExecutionTask next = null;
        if (this.tasks != null && this.tasks.size() > 0) {
            switch (this.executionStatus) {
                case UNDETERMINED:
                case QUEUED_ACTIVE:
                    next = this.tasks.get(0);
                    break;
                case SUSPENDED:
                    next = this.tasks.stream()
                            .filter(t -> t.getExecutionStatus() == ExecutionStatus.SUSPENDED)
                            .findFirst().orElse(null);
                    break;
                case RUNNING:
                    for (ExecutionTask task : this.tasks) {
                        if (task.getExecutionStatus() != ExecutionStatus.RUNNING) {
                            next = task;
                            break;
                        }
                    }
                    break;
                case DONE:
                case FAILED:
                case CANCELLED:
                default:
                    break;
            }
        }
        return next;
    }

    @Override
    public void statusChanged(ExecutionTask changedTask) {
        ExecutionStatus previous = this.executionStatus;
        ExecutionStatus taskStatus = changedTask.getExecutionStatus();
        switch (taskStatus) {
            case SUSPENDED:
            case CANCELLED:
            case FAILED:
                bulkSetStatus(changedTask, taskStatus);
                this.executionStatus = taskStatus;
                break;
            case DONE:
                if (this.tasks.get(this.tasks.size() - 1).getId().equals(changedTask.getId())) {
                    this.executionStatus = ExecutionStatus.DONE;
                }
                break;
            default:
                // do nothing for other states
                break;
        }
    }

    private void bulkSetStatus(ExecutionTask firstExculde, ExecutionStatus status) {
        if (this.tasks == null) {
            return;
        }
        int idx = 0;
        boolean found = false;
        while (idx < this.tasks.size()) {
            if (!found) {
                found = this.tasks.get(idx).getId().equals(firstExculde.getId());
            } else {
                this.tasks.get(idx).internalStatusChange(status);
            }
            idx++;
        }
        if (!found) {
            throw new IllegalArgumentException("Task not found");
        }
    }
}
