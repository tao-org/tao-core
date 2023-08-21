package ro.cs.tao.messaging.progress;

import java.beans.Transient;
import java.util.Locale;

public class DownloadProgress extends ActivityProgress {
    private final double transferSpeedMB;
    private final int remaining;

    public DownloadProgress(String taskName, double progress, double transferSpeed, int remaining) {
        super(taskName, progress);
        this.id = System.currentTimeMillis();
        this.transferSpeedMB = transferSpeed;
        this.remaining = remaining;
    }

    @Transient
    public double getTransferSpeed() {
        return transferSpeedMB;
    }

    @Transient
    public String getTransferSpeedMB() { return String.format(Locale.US, "%.2f MB/s", this.transferSpeedMB); }

    @Transient
    public int getRemaining() { return remaining; }
}
