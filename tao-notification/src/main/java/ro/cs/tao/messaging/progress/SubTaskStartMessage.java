package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.util.Objects;

public class SubTaskStartMessage extends Message {
    private final String taskName;
    private final String subTaskName;

    public SubTaskStartMessage(String taskName, String subTaskName) {
        super();
        this.taskName = taskName;
        this.subTaskName = subTaskName;
        setMessage("Started " + this.taskName + ":" + this.subTaskName);
    }

    public String getTaskName() { return taskName; }

    public String getSubTaskName() { return subTaskName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubTaskStartMessage that = (SubTaskStartMessage) o;
        return taskName.equals(that.taskName) &&
                subTaskName.equals(that.subTaskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, subTaskName);
    }
}
