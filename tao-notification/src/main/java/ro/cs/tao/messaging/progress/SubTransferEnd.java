package ro.cs.tao.messaging.progress;

public class SubTransferEnd extends SubActivityEnd {


    public SubTransferEnd(String taskName, String subTaskName) {
        super(taskName, subTaskName);
        setPayload("[" + this.taskName + "] Completed transfer for " + this.subTaskName);
    }
}
