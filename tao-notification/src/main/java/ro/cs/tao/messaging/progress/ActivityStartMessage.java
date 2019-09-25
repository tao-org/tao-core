package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.util.Objects;

public class ActivityStartMessage extends Message {
    private final String taskName;

    public ActivityStartMessage(String taskName) {
        super();
        this.taskName = taskName;
        setPayload("Started " + this.taskName);
    }

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
