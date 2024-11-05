package ro.cs.tao.orchestration.status;

import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.execution.util.TaskUtilities;
import ro.cs.tao.messaging.Message;
import ro.cs.tao.messaging.Messaging;
import ro.cs.tao.messaging.Topic;
import ro.cs.tao.persistence.PersistenceException;

import java.time.LocalDateTime;

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
        if (task instanceof ProcessingExecutionTask) {
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
