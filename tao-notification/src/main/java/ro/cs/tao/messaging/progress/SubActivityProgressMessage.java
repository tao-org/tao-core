package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.util.Locale;
import java.util.Objects;

public class SubActivityProgressMessage extends Message {
    private final String taskName;
    private final double taskProgress;
    private final String subTaskName;
    private final double subTaskProgress;

    public SubActivityProgressMessage(String taskName, String subTaskName, double taskProgress, double subTaskProgress) {
        this.taskName = taskName;
        this.taskProgress = taskProgress;
        this.subTaskName = subTaskName;
        this.subTaskProgress = subTaskProgress;
        setPayload(String.format("[%s: %s] - %s: %s",
                                 taskName, String.format(Locale.US, "%.4f", taskProgress),
                                 subTaskName, String.format(Locale.US, "%.4f", subTaskProgress)));
    }

    public String getTaskName() { return taskName; }

    public String getSubTaskName() { return subTaskName; }

    public double getTaskProgress() { return taskProgress; }

    public double getSubTaskProgress() { return subTaskProgress; }

    public String getTaskProgressPercent() { return String.format(Locale.US, "%.2f", this.taskProgress * 100.0); }

    public String getSubTaskProgressPercent() { return String.format(Locale.US, "%.2f", this.subTaskProgress * 100.0); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubActivityProgressMessage that = (SubActivityProgressMessage) o;
        return Double.compare(that.taskProgress, taskProgress) == 0 &&
                Double.compare(that.subTaskProgress, subTaskProgress) == 0 &&
                taskName.equals(that.taskName) &&
                subTaskName.equals(that.subTaskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, taskProgress, subTaskName, subTaskProgress);
    }
}
