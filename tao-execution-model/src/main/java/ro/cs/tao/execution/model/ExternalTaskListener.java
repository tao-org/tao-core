package ro.cs.tao.execution.model;

public interface ExternalTaskListener {
    void onStarted(ProcessingExecutionTask task);
    void onUpdated(ProcessingExecutionTask task);
    void onCompleted(ProcessingExecutionTask task, String processOutput);
    void onError(ProcessingExecutionTask task, String reason, int errorCode, String processOutput);
}
