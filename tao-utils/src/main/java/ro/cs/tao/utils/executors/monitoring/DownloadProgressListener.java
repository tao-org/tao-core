package ro.cs.tao.utils.executors.monitoring;

/**
 * An interface that provides methods for tracking download progress and reporting updates
 * including progress percentage, transfer speeds, and remaining items.
 */
public interface DownloadProgressListener extends ProgressListener {

    /**
     * Sets the length of the download being monitored.
     * The length typically represents the total size of the content.
     *
     * @param value The length to be set, expressed as a long value.
     */
    default void setLength(long value) {

    }

    /**
     * Retrieves the length of the download being monitored.
     *
     * @return the total length of the download as a long value, typically representing the size in bytes.
     */
    default long getLength() { return 0; }

    /**
     * Signals the current progress of the download, together with the detected download speed.
     * @param progressValue The progress value (between 0 and 1).
     * @param transferSpeed The download speed in MB/s
     */
    default void notifyProgress(double progressValue, double transferSpeed) {
        System.out.printf("[%.2f%% %.2f MB/s]\r", progressValue * 100, transferSpeed);
    }

    /**
     * Signals the current progress of the download, together with the detected download speed and remaining items.
     * @param progressValue The progress value (between 0 and 1).
     * @param transferSpeed The download speed in MB/s
     * @param queuedItems   The items remaining to be downloaded
     */
    default void notifyProgress(double progressValue, double transferSpeed, int queuedItems) {
        System.out.printf("1 of %d [%.2f%% %.2f MB/s]\r", queuedItems, progressValue * 100, transferSpeed);
    }
}
