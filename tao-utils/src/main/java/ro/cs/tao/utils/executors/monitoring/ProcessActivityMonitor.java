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

public abstract class ProcessActivityMonitor {

    private final Timer timer;
    private final Logger logger;
    private ProcessActivityListener listener;
    final int processId;
    String hostName;
    Triple<LocalDateTime, Long, Long> lastRecord;
    Triple<LocalDateTime, Long, Long> record;

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

    public static ProcessActivityMonitor createMonitor(Process process, int sampleInterval) {
        return SystemUtils.IS_OS_WINDOWS ?
                new WindowsMonitor(process, sampleInterval) : new LinuxMonitor(process, sampleInterval);
    }

    public Triple<LocalDateTime, Long, Long> getLastRecord() { return record; }

    void setListener(ProcessActivityListener listener) { this.listener = listener; }

    abstract int readDiskStats();
}
