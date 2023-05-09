package ro.cs.tao.utils.executors.monitoring;

public interface DownloadProgressListener extends ProgressListener {

    default void setLength(long value) {

    }

    default long getLength() { return 0; }

    /**
     * Signals the current progress of the download, together with the detected download speed.
     * @param progressValue The progress value (between 0 and 1).
     * @param transferSpeed The download speed in MB/s
     */
    default void notifyProgress(double progressValue, double transferSpeed) {
        System.out.printf("[%.2f%% %.2f MB/s]\r", progressValue * 100, transferSpeed);
    }

}
