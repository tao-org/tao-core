package ro.cs.tao.messaging;

import ro.cs.tao.ProgressListener;
import ro.cs.tao.security.SystemPrincipal;

import java.security.Principal;

//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;

/**
 * @author Cosmin Cara
 */
public class ProgressNotifier implements ProgressListener {
    private static final String TASK_START = "Started %s";
    private static final String TASK_END = "Completed %s";
    private static final String SUBTASK_START = "Started %s:%s";
    private static final String SUBTASK_END = "Completed %s:%s";
    private static final String TASK_PROGRESS = "%s: %s";
    private static final String SUBTASK_PROGRESS = "[%s: %s] - %s: %s";

//    private final ExecutorService worker;
    private final String topic;
    private final Object owner;
    private final Principal principal;
    private String taskName;
    private double taskCounter;
    private double subTaskCounter;

    public ProgressNotifier(Object source, String topic) {
        this(SystemPrincipal.instance(), source, topic);
    }

    public ProgressNotifier(Principal principal, Object source, String topic) {
        this.owner = source;
        this.topic = topic;
        this.principal = principal;
//        this.worker = Executors.newSingleThreadExecutor();
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
        if (taskName == null) {
            taskName = subTaskName;
        }
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
    public void notifyProgress(String subTaskName, double subTaskProgress) {
        notifyProgress(subTaskName, subTaskProgress, taskCounter);
    }

    @Override
    public void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress) {
        if (subTaskProgress < subTaskCounter) {
            throw new IllegalArgumentException(
                    String.format("Progress counter cannot go backwards [actual:%.2f%%, received:%.2f%%]",
                                  subTaskCounter, subTaskProgress));
        }
        subTaskCounter = subTaskProgress;
        taskCounter = overallProgress;
        if (subTaskCounter < 100) {
            sendMessage(SUBTASK_PROGRESS, taskName,
                        String.format("%.2f", taskCounter),
                        subTaskName,
                        String.format("%.2f", subTaskCounter));
        } else {
            subActivityEnded(subTaskName);
        }
    }

    private void sendMessage(String messageTemplate, Object...args) {
        //this.worker.submit(() -> DefaultMessageBus.send(1, this.topic, this.owner, String.format(messageTemplate, args)));
        Messaging.send(this.principal, this.topic, this.owner, String.format(messageTemplate, args));
    }
}
