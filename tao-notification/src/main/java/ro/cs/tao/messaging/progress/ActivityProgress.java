package ro.cs.tao.messaging.progress;

import ro.cs.tao.messaging.Message;

import java.beans.Transient;
import java.util.Locale;
import java.util.Objects;

/**
 * Specialized message signaling the progress change of an activity
 *
 * @author Cosmin Cara
 */
public class ActivityProgress extends Message {
    private final String taskName;
    private final double progress;

    public ActivityProgress(String taskName, double progress) {
        super();
        this.taskName = taskName;
        this.progress = progress;
        setPayload(this.taskName + ":" + String.format(Locale.US, "%.4f", this.progress));
    }

    @Transient
    public String getTaskName() { return taskName; }

    @Transient
    public double getProgress() { return progress; }

    @Transient
    public String getProgressPercent() { return String.format(Locale.US, "%.2f", this.progress * 100.0); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ActivityProgress that = (ActivityProgress) o;
        return Double.compare(that.progress, progress) == 0 &&
                taskName.equals(that.taskName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(taskName, progress);
    }
}
