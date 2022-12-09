package ro.cs.tao.execution;

import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;

public interface TaskStatusListener {
    void taskStatusChanged(ExecutionTask task, ExecutionStatus status, String reason);
    void taskChanged(ExecutionTask task);
}
