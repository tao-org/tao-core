package ro.cs.tao.execution.persistence;

import ro.cs.tao.execution.model.*;
import ro.cs.tao.persistence.EntityProvider;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.workflow.enums.TransitionBehavior;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface for managing execution tasks, providing methods for querying, saving, and updating
 * execution task entities. This interface extends EntityProvider to add specific functionalities
 * related to execution tasks, including operations to manage running tasks, retrieve tasks by
 * various criteria, and update task states or related information.
 */
public interface ExecutionTaskProvider extends EntityProvider<ExecutionTask, Long> {

    /**
     * Retrieves a list of execution tasks that are currently in a running state.
     *
     * @return a list of {@code ExecutionTask} objects representing the currently running tasks.
     */
    List<ExecutionTask> listRunning();

    /**
     * Retrieves a list of execution tasks that are currently in execution for the specified application.
     *
     * @param applicationId the unique identifier of the application for which execution tasks are to be retrieved
     * @return a list of {@code ExecutionTask} objects representing tasks currently in execution for the given application
     */
    List<ExecutionTask> listExecuting(String applicationId);

    /**
     * Retrieves a list of execution tasks that are currently in execution for WPS (Web Processing Service).
     *
     * @return a list of {@code ExecutionTask} objects representing tasks that are being executed for WPS.
     */
    List<ExecutionTask> listWPSExecuting();

    /**
     * Retrieves a list of execution tasks that are currently in execution for WMS (Web Map Service).
     *
     * @return a list of {@code ExecutionTask} objects representing tasks that are being executed for WMS.
     */
    List<ExecutionTask> listWMSExecuting();

    /**
     * Retrieves a list of execution tasks that are currently in execution for OGC Processes.
     *
     * @return a list of {@code ExecutionTask} objects representing tasks that are being executed for OGC Processes.
     */
    List<ExecutionTask> listOGCProcessesExecuting();

    /**
     * Retrieves a list of execution tasks associated with a specific host.
     *
     * @param hostName the name of the host for which the execution tasks are to be retrieved
     * @return a list of {@code ExecutionTask} objects associated with the specified host
     */
    List<ExecutionTask> listByHost(String hostName);

    /**
     * Counts the number of execution tasks associated with a specific host that have been created or updated
     * since the specified date and time.
     *
     * @param hostName the name of the host for which the execution tasks are to be counted
     * @param since the {@code LocalDateTime} specifying the date and time after which the tasks must have
     *        been created or updated to be included in the count
     * @return the number of execution tasks matching the provided criteria
     */
    int countByHostSince(String hostName, LocalDateTime since);

    /**
     * Counts the currently running execution tasks and returns a map associating categories or types
     * of tasks with their respective counts.
     *
     * @return a map where the keys are strings representing categories or types of running tasks,
     *         and the values are integers representing the count of tasks in each category or type
     */
    Map<String, Integer> countRunning();

    /**
     * Retrieves the date and time of the most recent task executed on a specific host.
     *
     * @param hostName the name of the host for which the last execution time is to be retrieved
     * @return the {@code LocalDateTime} representing the last execution time for tasks on the specified host,
     *         or {@code null} if no tasks have been executed on the host
     */
    LocalDateTime getLastRunTask(String hostName);

    /**
     * Retrieves a list of execution tasks associated with the specified job ID.
     *
     * @param jobId the unique identifier of the job for which execution tasks are to be retrieved
     * @return a list of {@code ExecutionTask} objects corresponding to the specified job ID
     */
    List<ExecutionTask> getDataSourceTasks(long jobId);

    /**
     * Retrieves a list of execution task summaries associated with the specified job ID.
     *
     * @param jobId the unique identifier of the job for which the execution task summaries are to be retrieved
     * @return a list of {@code ExecutionTaskSummary} objects representing the status of tasks associated with the given job
     */
    List<ExecutionTaskSummary> getTasksStatus(long jobId);

    /**
     * Retrieves the status summary of a specific execution task based on its unique task ID.
     *
     * @param taskId the unique identifier of the task whose status is to be retrieved
     * @return an {@code ExecutionTaskSummary} object containing detailed information about the task's execution,
     *         including its status, assigned host, progress, resource usage, and other metadata
     */
    ExecutionTaskSummary getTaskStatus(long taskId);

    /**
     * Retrieves a specific execution task based on the given job ID, node ID, and instance ID.
     *
     * @param jobId the unique identifier of the job for which the execution task is to be retrieved
     * @param nodeId the unique identifier of the node associated with the execution task
     * @param instanceId the instance ID of the execution task within the job and node
     * @return an {@code ExecutionTask} object representing the execution task that matches the provided criteria,
     *         or {@code null} if no matching task is found
     */
    ExecutionTask getByJobAndNode(long jobId, long nodeId, int instanceId);

    /**
     * Retrieves a specific execution task based on the given group ID, node ID, and instance ID.
     *
     * @param groupId the unique identifier of the group associated with the execution task
     * @param nodeId the unique identifier of the node associated with the execution task
     * @param instanceId the instance ID of the execution task within the group and node
     * @return an {@code ExecutionTask} object representing the execution task that matches the provided criteria,
     *         or {@code null} if no matching task is found
     */
    ExecutionTask getByGroupAndNode(long groupId, long nodeId, int instanceId);

    /**
     * Retrieves a specific execution task based on the given resource ID.
     *
     * @param id the unique identifier of the resource for which the execution task is to be retrieved
     * @return an {@code ExecutionTask} object representing the task associated with the specified resource ID,
     *         or {@code null} if no task matches the provided ID
     */
    ExecutionTask getByResourceId(String id);

    /**
     * Retrieves the number of parent tasks currently in a running state for a given job and task.
     *
     * @param jobId the unique identifier of the job to which the task belongs
     * @param taskId the unique identifier of the task for which the running parent tasks are to be counted
     * @return the number of parent tasks that are currently in a running state, associated with the specified job and task
     */
    int getRunningParents(long jobId, long taskId);

    /**
     * Retrieves the list of parent IDs associated with the specified task.
     *
     * @param taskId the unique identifier of the task for which to retrieve the parent IDs
     * @return a list of parent IDs as {@code Long} objects associated with the specified task
     */
    List<Long> getParentIds(long taskId);

    /**
     * Retrieves a list of child tasks associated with the specified task.
     *
     * @param taskId the unique identifier of the parent task for which the child tasks are to be retrieved
     * @return a list of {@code ExecutionTask} objects representing the child tasks associated with the specified parent task
     */
    List<ExecutionTask> getChildrenTasks(long taskId);

    /**
     * Persists an {@code ExecutionTask} along with its associated {@code ExecutionJob}.
     *
     * @param task the {@code ExecutionTask} that needs to be saved
     * @param job the {@code ExecutionJob} associated with the task
     * @return the saved {@code ExecutionTask} instance, potentially with updated or assigned fields
     * @throws PersistenceException if any persistence-related error occurs while saving the task
     */
    ExecutionTask save(ExecutionTask task, ExecutionJob job) throws PersistenceException;

    /**
     * Updates the status of the specified execution task.
     *
     * @param task the {@code ExecutionTask} to be updated
     * @param newStatus the new {@code ExecutionStatus} to be assigned to the task
     * @param reason the reason for the status update, to provide additional context
     * @return the updated {@code ExecutionTask} object reflecting the new status and reason
     * @throws PersistenceException if an error occurs while persisting the status update
     */
    ExecutionTask updateStatus(ExecutionTask task, ExecutionStatus newStatus, String reason) throws PersistenceException;

    /**
     * Saves the given execution task along with its associated execution group. The task is persisted to the underlying
     * storage, and any necessary associations to the execution group are managed.
     *
     * @param task the {@code ExecutionTask} to be saved
     * @param taskGroup the {@code ExecutionGroup} associated with the task
     * @return the saved {@code ExecutionTask} instance, potentially with updated or assigned fields
     * @throws PersistenceException if an error occurs during the persistence process
     */
    ExecutionTask save(ExecutionTask task, ExecutionGroup taskGroup) throws PersistenceException;

    /**
     * Saves an {@code ExecutionTask} along with its associated {@code ExecutionGroup} and {@code ExecutionJob},
     * including any necessary sub-tasks. The provided {@code ExecutionGroup} and {@code ExecutionJob} are used
     * to define the context and associations for the task and its sub-tasks.
     *
     * @param taskGroup the {@code ExecutionGroup} representing the group context to which the task and its sub-tasks belong
     * @param job the {@code ExecutionJob} representing the job context associated with the task and its sub-tasks
     * @return the saved {@code ExecutionTask} instance, potentially with updated or assigned fields
     * @throws PersistenceException if a persistence-related error occurs during the save process
     */
    ExecutionTask saveWithSubTasks(ExecutionGroup taskGroup, ExecutionJob job) throws PersistenceException;

    /**
     * Retrieves the number of CPUs allocated to a specific user.
     *
     * @param userId the unique identifier of the user whose CPU allocation is to be retrieved
     * @return the number of CPUs allocated to the user
     */
    int getCPUsForUser(String userId);

    /**
     * Retrieves the amount of memory allocated to a specific user.
     *
     * @param userId the unique identifier of the user whose memory allocation is to be retrieved
     * @return the amount of memory allocated to the specified user, represented as an integer
     */
    int getMemoryForUser(String userId);

    /**
     * Updates the average execution time for a specific component identified by its unique ID.
     *
     * @param id       the unique identifier of the component whose time is being updated
     * @param duration the new duration value to update the average execution time with, in seconds
     */
    void updateComponentTime(String id, int duration);

    /**
     * Counts the number of runnable tasks associated with a specified job ID.
     *
     * @param jobId the unique identifier of the job for which runnable tasks are to be counted
     * @return the total number of runnable tasks for the given job ID
     */
    int countRunableTasks(long jobId);

    /**
     * Determines whether the specified task is a terminal task.
     * A terminal task represents the final step in a workflow or process.
     *
     * @param taskId the unique identifier of the task to be checked
     * @return true if the task is a terminal task, false otherwise
     */
    boolean isTerminalTask(long taskId);

    /**
     * Retrieves the TransitionBehavior associated with the given task ID.
     *
     * @param taskId the identifier of the task for which the transition behavior is to be retrieved
     * @return the TransitionBehavior instance corresponding to the specified task ID
     */
    TransitionBehavior getTransitionBehavior(long taskId);

    /**
     * Retrieves the execution task associated with the specified output path.
     *
     * @param outputPath the path of the output for which the corresponding execution task is to be retrieved
     * @return the execution task associated with the given output path, or null if no task matches the output path
     */
    ExecutionTask getTaskByOutput(String outputPath);
}
