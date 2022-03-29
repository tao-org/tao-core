package ro.cs.tao.messaging.progress;

public class SubTransferStart extends SubActivityStart {

    public SubTransferStart(String taskName, String subTaskName) {
        super(taskName, subTaskName);
        setPayload("[" + this.taskName + "] Start transferring " + this.subTaskName);
    }
}
