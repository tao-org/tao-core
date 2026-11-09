package ro.cs.tao.utils.executors.monitoring;

import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.Triple;
import ro.cs.tao.utils.executors.MemoryUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A class that listens for and manages process activity events, such as disk I/O activities,
 * and logs the data if a log file is provided. This class implements the {@code ProcessActivityListener}
 * interface to handle disk activity records generated for monitored processes.
 */
public class ActivityListener implements ProcessActivityListener {
    private final Logger logger = Logger.getLogger(ActivityListener.class.getName());
    private int sampleInterval;
    private RollingFile logFile;
    private final Map<Process, ProcessActivityMonitor> monitorMap;

    public ActivityListener() {
        this.monitorMap = new HashMap<>();
        this.sampleInterval = 10000;
    }

    /**
     * Sets the sampling interval for monitoring activities.
     *
     * @param sampleInterval the sampling interval in milliseconds at which activity monitoring is performed
     */
    public void setSampleInterval(int sampleInterval) { this.sampleInterval = sampleInterval; }

    /**
     * Configures the log file where disk activity will be recorded. If the specified file does not exist,
     * it will be created, and necessary parent directories will also be generated as needed.
     * The log file will record disk read and write activities in a tabular format.
     * If the log file is successfully set, a confirmation will be logged.
     * Otherwise, a warning indicating failure to record disk activity will be issued.
     *
     * @param file the {@code Path} to the disk activity log file
     */
    public void setLogFile(Path file) {
        if (file != null) {
            if (!Files.exists(file)) {
                try {
                    FileUtilities.createDirectories(file.getParent());
                    Files.write(file,
                                (StringUtils.rightPad("Time", 19, " ") + "\t" +
                                 StringUtils.rightPad("Read [kB/s]", 15, " ") +
                                 StringUtils.rightPad("Write [kB/s]", 15, " ") + "\n").getBytes(),
                                StandardOpenOption.CREATE);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
            }
            this.logFile = new RollingFile(file, MemoryUnit.MB.value() * 16);
            if (this.logFile.getBase() != null) {
                logger.fine(String.format("Disk activity will be recorded in %s", this.logFile.getBase()));
            } else {
                logger.warning("Disk activity will not be recorded");
            }
        }
    }

    @Override
    public synchronized void onActivity(Triple<LocalDateTime, Long, Long> record) {
        if (this.logFile != null) {
            final StringBuilder builder = new StringBuilder();
            builder.append(record.getKeyOne().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .append("\t")
                    .append(StringUtils.leftPad(String.valueOf(record.getKeyTwo()), 15, " "))
                    .append(StringUtils.leftPad(String.valueOf(record.getKeyThree()), 15, " "))
                    .append("\n");
            try {
                this.logFile.write(builder.toString().getBytes());
            } catch (IOException e) {
                logger.severe(ExceptionUtils.getStackTrace(logger, e));
            }
        }
    }

    /**
     * Attaches a process to the activity listener, allowing the monitoring of disk activity
     * for the specified process. If the process is not already being monitored, a new
     * {@code ProcessActivityMonitor} is created and registered with this listener.
     *
     * @param process the {@code Process} instance to be monitored
     */
    public void attachTo(Process process) {
        if (!this.monitorMap.containsKey(process)) {
            ProcessActivityMonitor monitor = ProcessActivityMonitor.createMonitor(process, this.sampleInterval);
            monitor.setListener(this);
            this.monitorMap.put(process, monitor);
        }
    }

    /**
     * Detaches a process from the activity listener, stopping the monitoring of disk activity
     * for the specified process. This will remove the associated process from the internal
     * monitoring map if it is currently being monitored.
     *
     * @param process the {@code Process} instance to be detached
     */
    public void detachFrom(Process process) {
        this.monitorMap.remove(process);
    }
}
