package ro.cs.tao.messaging.progress;

/**
 * Specialized message signaling the start of a nested (sub-) transfer activity
 *
 * @author Cosmin Cara
 */
public class SubTransferStart extends SubActivityStart {

    public SubTransferStart(String taskName, String subTaskName) {
        super(taskName, subTaskName);
        setPayload("[" + this.taskName + "] Start transferring " + this.subTaskName);
    }
}
