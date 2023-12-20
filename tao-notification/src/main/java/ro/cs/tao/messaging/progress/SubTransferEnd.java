package ro.cs.tao.messaging.progress;

/**
 * Specialized message signaling the end of a nested (sub-) transfer activity
 *
 * @author Cosmin Cara
 */
public class SubTransferEnd extends SubActivityEnd {


    public SubTransferEnd(String taskName, String subTaskName) {
        super(taskName, subTaskName);
        setPayload("[" + this.taskName + "] Completed transfer for " + this.subTaskName);
    }

    public SubTransferEnd(String taskName, String subTaskName, boolean result) {
        super(taskName, subTaskName);
        if (result) {
            setPayload("[" + this.taskName + "] Completed transfer for " + this.subTaskName);
        } else {
            setPayload("[" + this.taskName + "] Failed transfer for " + this.subTaskName);
        }
    }
}
