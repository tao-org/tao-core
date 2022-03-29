package ro.cs.tao.messaging.progress;

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
