package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Objects;

/**
 * Specialized message signaling the beginning of an activity
 *
 * @author Cosmin Cara
 */
public class ActivityStart extends Message {
    protected String taskName;

    public ActivityStart() {
        super();
    }

    public ActivityStart(String taskName) {
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
        ActivityStart that = (ActivityStart) o;
        return taskName.equals(that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName);
    }
}
