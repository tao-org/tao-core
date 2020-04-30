package ro.cs.tao.utils.executors.monitoring;

import org.apache.commons.lang3.StringUtils;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.Triple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ActivityListener implements ProcessActivityListener {
    private final Logger logger = Logger.getLogger(ActivityListener.class.getName());
    private int sampleInterval;
    private RollingFile logFile;
    private final Map<Process, ProcessActivityMonitor> monitorMap;

    public ActivityListener() {
        this.monitorMap = new HashMap<>();
        this.sampleInterval = 10000;
    }

    public void setSampleInterval(int sampleInterval) { this.sampleInterval = sampleInterval; }

    public void setLogFile(Path file) {
        if (file != null) {
            if (!Files.exists(file)) {
                try {
                    Files.createDirectories(file.getParent());
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
            this.logFile = new RollingFile(file, 1024 * 1024 * 16);
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

    public void attachTo(Process process) {
        if (!this.monitorMap.containsKey(process)) {
            ProcessActivityMonitor monitor = ProcessActivityMonitor.createMonitor(process, this.sampleInterval);
            monitor.setListener(this);
            this.monitorMap.put(process, monitor);
        }
    }

    public void detachFrom(Process process) {
        this.monitorMap.remove(process);
    }
}
