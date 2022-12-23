package ro.cs.tao.messaging.progress;

import java.beans.Transient;
import java.util.Locale;

public class DownloadProgress extends ActivityProgress {
    private final double transferSpeedMB;

    public DownloadProgress(String taskName, double progress, double transferSpeed) {
        super(taskName, progress);
        this.transferSpeedMB = transferSpeed;
    }

    @Transient
    public double getTransferSpeed() {
        return transferSpeedMB;
    }

    @Transient
    public String getTransferSpeedMB() { return String.format(Locale.US, "%.2f MB/s", this.transferSpeedMB); }
}
