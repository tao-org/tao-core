package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Objects;

/**
 * Specialized message signaling the end of an activity
 *
 * @author Cosmin Cara
 */
public class ActivityEnd extends Message {
    protected String taskName;

    public ActivityEnd() {
        super();
    }

    public ActivityEnd(String taskName) {
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
        ActivityEnd that = (ActivityEnd) o;
        return taskName.equals(that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName);
    }
}
