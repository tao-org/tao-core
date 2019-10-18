package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Objects;

public class ActivityStartMessage extends Message {
    private String taskName;

    public ActivityStartMessage() {
        super();
    }

    public ActivityStartMessage(String taskName) {
        this();
        this.taskName = taskName;
        setPayload("Started " + this.taskName);
    }

    @Transient
    public String getTaskName() { return taskName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityStartMessage that = (ActivityStartMessage) o;
        return taskName.equals(that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName);
    }
}
