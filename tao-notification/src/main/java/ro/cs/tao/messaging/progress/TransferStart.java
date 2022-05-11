package ro.cs.tao.messaging.progress;

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
