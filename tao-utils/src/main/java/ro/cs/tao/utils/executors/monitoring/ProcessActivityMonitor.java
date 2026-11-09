package ro.cs.tao.utils.executors.monitoring;

import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.utils.Triple;
import ro.cs.tao.utils.executors.ProcessHelper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * An abstract class for monitoring process activity, specifically disk read and write operations.
 * The implementation of actual disk activity monitoring is platform-specific and should be provided
 * by subclasses.
 *
 * The class periodically samples the disk read/write activity of a given process and notifies a
 * listener, if registered, with the activity data. It relies on platform-specific mechanisms to
 * retrieve disk activity information.
 */
public abstract class ProcessActivityMonitor {

    private final Timer timer;
    private final Logger logger;
    private ProcessActivityListener listener;
    final int processId;
    String hostName;
    Triple<LocalDateTime, Long, Long> lastRecord;
    Triple<LocalDateTime, Long, Long> record;

    /**
     * Constructs a new ProcessActivityMonitor object to monitor the disk read/write activity
     * of a given process at specific intervals. This class schedules a timer task to periodically
     * sample activity data and notifies a registered listener with the activity record.
     *
     * @param process         The process to monitor. This must be a valid, running process.
     * @param sampleInterval  The interval, in milliseconds, at which the activity sampling takes place.
     *                        This value determines how frequently disk activity is recorded for the process.
     */
    ProcessActivityMonitor(Process process, long sampleInterval) {
        this.logger = Logger.getLogger(ProcessActivityMonitor.class.getName());
        this.processId = ProcessHelper.getPID(process);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                int retCode;
                if (process.isAlive()) {
                    if ((retCode = readDiskStats()) != 0) {
                        logger.severe(String.format("Could not get disk information for process %d [return code: %d]",
                                                    processId, retCode));
                        timer.cancel();
                    } else {
                        if (listener != null && record != null) {
                            listener.onActivity(record);
                            record = null;
                        }
                    }
                } else {
                    timer.cancel();
                }
            }
        };
        try {
            this.hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            this.hostName = "localhost";
        }
        this.timer = new Timer(String.format("Monitor [pid=%d]", this.processId), false);
        this.timer.scheduleAtFixedRate(timerTask, sampleInterval, sampleInterval);
    }

    /**
     * Creates a new instance of a ProcessActivityMonitor appropriate for the
     * operating system to monitor the disk activity of a given process.
     *
     * @param process        The process to monitor. This must be a valid, running process for
     *                       which disk I/O activity is to be observed.
     * @param sampleInterval The interval, in milliseconds, at which the activity sampling
     *                       takes place. Determines how frequently the monitor collects disk activity data.
     * @return A specific implementation of ProcessActivityMonitor, either WindowsMonitor or
     *         LinuxMonitor, depending on the operating system.
     */
    public static ProcessActivityMonitor createMonitor(Process process, int sampleInterval) {
        return SystemUtils.IS_OS_WINDOWS ?
                new WindowsMonitor(process, sampleInterval) : new LinuxMonitor(process, sampleInterval);
    }

    /**
     * Retrieves the last recorded disk activity for the monitored process.
     * The record is represented as a {@link Triple} containing:
     * - The timestamp of when the data was recorded (as a {@link LocalDateTime}),
     * - The total bytes read (as a {@link Long}),
     * - The total bytes written (as a {@link Long}).
     *
     * @return A {@link Triple} containing the timestamp, bytes read, and bytes written, or {@code null}
     *         if no activity has been recorded yet.
     */
    public Triple<LocalDateTime, Long, Long> getLastRecord() { return record; }

    /**
     * Sets the listener that will receive notifications about process disk activity.
     *
     * @param listener The {@link ProcessActivityListener} to be notified of activity.
     *                 The listener is responsible for handling records containing the
     *                 timestamp, amount of data read, and amount of data written by the process.
     */
    void setListener(ProcessActivityListener listener) { this.listener = listener; }

    /**
     * Reads the disk activity statistics for the monitored process and updates the current record
     * with the captured data.
     *
     * Disk activity data may include information about the amount of data read and written
     * by the monitored process. The details collected depend on the operating system's
     * implementation. This method leverages platform-specific mechanisms to gather the relevant
     * statistics.
     *
     * @return An integer indicating the result of the operation. Typically, a value of 0
     *         indicates success, while a non-zero value indicates an error or failure
     *         in retrieving the disk activity data.
     */
    abstract int readDiskStats();
}
