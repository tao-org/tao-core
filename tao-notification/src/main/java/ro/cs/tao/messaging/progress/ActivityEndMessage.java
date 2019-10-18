package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Objects;

public class ActivityEndMessage extends Message {
    private String taskName;

    public ActivityEndMessage() {
        super();
    }

    public ActivityEndMessage(String taskName) {
        this();
        this.taskName = taskName;
        setPayload("Completed " + this.taskName);
    }

    @Transient
    public String getTaskName() { return taskName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityEndMessage that = (ActivityEndMessage) o;
        return taskName.equals(that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName);
    }
}
