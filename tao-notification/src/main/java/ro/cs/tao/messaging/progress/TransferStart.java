package ro.cs.tao.messaging.progress;

/**
 * Specialized message signaling the start of a transfer activity
 *
 * @author Cosmin Cara
 */
public class TransferStart extends ActivityStart {

    public TransferStart() {
        super();
    }

    public TransferStart(String taskName) {
        this();
        this.taskName = taskName;
        setPayload("Start transferring " + this.taskName);
    }
}
