package ro.cs.tao;

/**
 * @author Cosmin Cara
 */
public interface ProgressListener {
    void started(String taskName);
    void subActivityStarted(String subTaskName);
    void subActivityEnded(String subTaskName);
    void ended();
    void notifyProgress(double progressValue);
    void notifyProgress(String subTaskName, double subTaskProgress);
    void notifyProgress(String subTaskName, double subTaskProgress, double overallProgress);
}
