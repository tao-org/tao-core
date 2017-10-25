package ro.cs.tao.notifications;

import ro.cs.tao.ProgressListener;

/**
 * @author Cosmin Cara
 */
public class ProgressNotifier implements ProgressListener {
    private static final String TASK_START = "%s started";
    private static final String TASK_END = "%s completed";
    private static final String SUBTASK_START = "%s:%s started";
    private static final String SUBTASK_END = "%s:%s started";
    private static final String TASK_PROGRESS = "%s %s";
    private static final String SUBTASK_PROGRESS = "%s:%s %s:%s";

    private String owner;
    private String taskName;
    private double taskCounter;
    private double subTaskCounter;

    public ProgressNotifier(String owner) {
        this.owner = owner;
    }

    @Override
    public void started(String taskName) {
        this.taskCounter = 0;
        this.taskName = taskName;
        sendMessage(TASK_START, taskName);
    }

    @Override
    public void subActivityStarted(String subTaskName) {
        this.subTaskCounter = 0;
        sendMessage(SUBTASK_START, taskName, subTaskName);
    }

    @Override
    public void subActivityEnded(String subTaskName) {
        this.subTaskCounter = 100;
        sendMessage(SUBTASK_END, taskName, subTaskName);
    }

    @Override
    public void ended() {
        this.taskCounter = 100;
        sendMessage(TASK_END, taskName);
    }

    @Override
    public void notifyProgress(double progressValue) {
        if (progressValue < taskCounter) {
            throw new IllegalArgumentException(
                    String.format("Progress taskCounter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  taskCounter, progressValue));
        }
        taskCounter = progressValue;
        if (taskCounter < 100) {
            sendMessage(TASK_PROGRESS, taskName, String.format("%.2f", progressValue));
        } else {
            ended();
        }
    }

    @Override
    public void notifyProgress(String subTaskName, double progressValue) {
        if (progressValue < subTaskCounter) {
            throw new IllegalArgumentException(
                    String.format("Progress counter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  subTaskCounter, progressValue));
        }
        subTaskCounter = progressValue;
        if (subTaskCounter < 100) {
            sendMessage(SUBTASK_PROGRESS, taskName, subTaskName,
                        String.format("%.2f", taskCounter),
                        String.format("%.2f", subTaskCounter));
        } else {
            subActivityEnded(subTaskName);
        }
    }

    private void sendMessage(String messageTemplate, Object...args) {
        MessageBus.send(MessageBus.PROGRESS, owner, String.format(messageTemplate, args));
    }
}
