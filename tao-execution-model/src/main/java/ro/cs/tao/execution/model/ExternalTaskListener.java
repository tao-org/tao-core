package ro.cs.tao.execution.model;

public interface ExternalTaskListener {
    void onStarted(ExecutionTask task);
    void onUpdated(ExecutionTask task);
    void onCompleted(ExecutionTask task, String processOutput);
    void onError(ExecutionTask task, String reason, int errorCode, String processOutput);
}
