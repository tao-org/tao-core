package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Locale;
import java.util.Objects;

/**
 * Specialized message signaling the progress change of a nested (sub-) activity
 *
 * @author Cosmin Cara
 */
public class SubActivityProgress extends Message {
    protected final String taskName;
    protected final double taskProgress;
    protected final String subTaskName;
    protected final double subTaskProgress;

    public SubActivityProgress(String taskName, String subTaskName, double taskProgress, double subTaskProgress) {
        this.taskName = taskName;
        this.taskProgress = taskProgress;
        this.subTaskName = subTaskName;
        this.subTaskProgress = subTaskProgress;
        this.id = System.currentTimeMillis();
        setPayload(String.format("[%s: %s] - %s: %s",
                                 taskName, String.format(Locale.US, "%.4f", taskProgress),
                                 subTaskName, String.format(Locale.US, "%.4f", subTaskProgress)));
    }

    @Transient
    public String getTaskName() { return taskName; }

    @Transient
    public String getSubTaskName() { return subTaskName; }

    @Transient
    public double getTaskProgress() { return taskProgress; }

    @Transient
    public double getSubTaskProgress() { return subTaskProgress; }

    @Transient
    public String getTaskProgressPercent() { return String.format(Locale.US, "%.2f", this.taskProgress * 100.0); }

    @Transient
    public String getSubTaskProgressPercent() { return String.format(Locale.US, "%.2f", this.subTaskProgress * 100.0); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubActivityProgress that = (SubActivityProgress) o;
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
