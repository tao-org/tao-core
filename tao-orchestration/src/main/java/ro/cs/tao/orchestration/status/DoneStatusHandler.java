package ro.cs.tao.orchestration.status;

import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.execution.model.*;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceException;

import java.time.LocalDateTime;

/**
 * Handles the status transition of tasks to the "DONE" state and updates job states
 * accordingly. This implementation ensures that jobs composed entirely of completed tasks
 * are marked as "DONE" while jobs still in progress or with no failures transition
 * to the "RUNNING" state.
 *
 * The handler also processes additional notifications for tasks being marked as "DONE."
 * Example responsibilities include updating the execution status of tasks and jobs,
 * logging relevant information, and sending notifications through the messaging system.
 *
 * Inherits functionality from the {@link TaskStatusHandler} abstract class, leveraging
 * shared mechanisms for task management.
 */
public class DoneStatusHandler extends TaskStatusHandler {
    protected DoneStatusHandler(ExecutionJobProvider jobProvider, ExecutionTaskProvider taskProvider) {
        super(jobProvider, taskProvider);
    }

    @Override
    protected void handleTask(ExecutionTask task, String reason) throws PersistenceException {
        ExecutionJob job = task.getJob();
        if (job.orderedTasks().stream().allMatch(t -> t.getExecutionStatus() == ExecutionStatus.DONE)) {
            job.setExecutionStatus(ExecutionStatus.DONE);
            job.setEndTime(LocalDateTime.now());
            jobProvider.update(job);
        } else if (job.getExecutionStatus() != ExecutionStatus.RUNNING &&
                job.orderedTasks().stream().noneMatch(t -> t.getExecutionStatus() == ExecutionStatus.FAILED)) {
            job.setExecutionStatus(ExecutionStatus.RUNNING);
            jobProvider.update(job);
        }
        if (task instanceof ProcessingExecutionTask || task instanceof ExecutionGroup) {
            final String strMsg = "Task " + TaskUtilities.getTaskDescriptiveName(task) +
                    (StringUtils.isNotEmpty(task.getExecutionNodeHostName()) ? " on node " + task.getExecutionNodeHostName() : "") +
                    " changed to " + ExecutionStatus.DONE.friendlyName();
            final Message message = Message.create(task.getJob().getUserId(), task.getId(),
                                                   strMsg, ExecutionStatus.DONE.name(), true);
            message.setTopic(Topic.EXECUTION.value());
            message.addItem("host", task.getExecutionNodeHostName());
            Messaging.send(message);
            logger.fine(strMsg);
        }
    }
}
