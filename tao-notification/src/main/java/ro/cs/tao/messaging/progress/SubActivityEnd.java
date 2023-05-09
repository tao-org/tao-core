package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Objects;

/**
 * Specialized message signaling the end of a nested (sub-) activity
 *
 * @author Cosmin Cara
 */
public class SubActivityEnd extends Message {
    protected final String taskName;
    protected final String subTaskName;

    public SubActivityEnd(String taskName, String subTaskName) {
        super();
        this.taskName = taskName;
        this.subTaskName = subTaskName;
        setPayload("[" + this.taskName + "] Completed " + this.subTaskName);
    }

    @Transient
    public String getTaskName() { return taskName; }

    @Transient
    public String getSubTaskName() { return subTaskName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubActivityEnd that = (SubActivityEnd) o;
        return taskName.equals(that.taskName) &&
                subTaskName.equals(that.subTaskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, subTaskName);
    }
}
