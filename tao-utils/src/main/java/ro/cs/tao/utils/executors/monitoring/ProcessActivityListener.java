package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.Triple;

import java.time.LocalDateTime;

/**
 * An interface for monitoring and responding to process activity.
 * Implementations of this interface are expected to handle disk activity records generated for monitored processes.
 */
public interface ProcessActivityListener {
    /**
     * Processes a disk activity record that contains the timestamp,
     * the amount of data read, and the amount of data written by a process.
     *
     * @param record a triple containing:
     *               - the first value (LocalDateTime) representing the timestamp of the activity,
     *               - the second value (Long) representing the amount of data read in kilobytes,
     *               - the third value (Long) representing the amount of data written in kilobytes.
     */
    void onActivity(Triple<LocalDateTime, Long, Long> record);
}
