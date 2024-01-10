package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.EntityProvider;
import ro.cs.tao.persistence.PersistenceException;

import java.time.LocalDateTime;
import java.util.List;

public interface ExecutionTaskProvider extends EntityProvider<ExecutionTask, Long> {

    List<ExecutionTask> listRunning();
    List<ExecutionTask> listExecuting(String applicationId);
    List<ExecutionTask> listWPSExecuting();
    List<ExecutionTask> listWMSExecuting();
    List<ExecutionTask> listByHost(String hostName);
    int countByHostSince(String hostName, LocalDateTime since);
    List<ExecutionTask> getDataSourceTasks(long jobId);
    List<ExecutionTaskSummary> getTasksStatus(long jobId);
    ExecutionTaskSummary getTaskStatus(long taskId);
    ExecutionTask getByJobAndNode(long jobId, long nodeId, int instanceId);
    ExecutionTask getByGroupAndNode(long groupId, long nodeId, int instanceId);
    ExecutionTask getByResourceId(String id);
    int getRunningParents(long jobId, long taskId);
    ExecutionTask save(ExecutionTask task, ExecutionJob job) throws PersistenceException;
    ExecutionTask updateStatus(ExecutionTask task, ExecutionStatus newStatus, String reason) throws PersistenceException;
    ExecutionTask save(ExecutionTask task, ExecutionGroup taskGroup) throws PersistenceException;
    ExecutionTask saveWithSubTasks(ExecutionGroup taskGroup, ExecutionJob job) throws PersistenceException;
    int getCPUsForUser(String userId);
    int getMemoryForUser(String userId);
    void updateComponentTime(String id, int duration);
}
