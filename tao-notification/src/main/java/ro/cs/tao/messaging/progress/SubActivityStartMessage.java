package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Objects;

public class SubActivityStartMessage extends Message {
    private final String taskName;
    private final String subTaskName;

    public SubActivityStartMessage(String taskName, String subTaskName) {
        super();
        this.taskName = taskName;
        this.subTaskName = subTaskName;
        setPayload("Started " + this.taskName + ":" + this.subTaskName);
    }

    @Transient
    public String getTaskName() { return taskName; }

    @Transient
    public String getSubTaskName() { return subTaskName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubActivityStartMessage that = (SubActivityStartMessage) o;
        return taskName.equals(that.taskName) &&
                subTaskName.equals(that.subTaskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, subTaskName);
    }
}
