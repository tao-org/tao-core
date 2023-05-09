package ro.cs.tao.messaging.progress;

/**
 * Specialized message signaling the end of a transfer activity
 *
 * @author Cosmin Cara
 */
public class TransferEnd extends ActivityEnd {

    public TransferEnd() {
        super();
    }

    public TransferEnd(String taskName) {
        this();
        this.taskName = taskName;
        setPayload("Completed transfer for " + this.taskName);
    }
}
