package ro.cs.tao.utils.executors;

import org.apache.commons.lang.SystemUtils;
import ro.cs.tao.utils.Triple;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public abstract class ProcessActivityMonitor {

    final List<Triple<LocalDateTime, Long, Long>> records;
    final int processId;
    private final Timer timer;
    private final Logger logger;
    String hostName;

    ProcessActivityMonitor(Process process, long sampleInterval) {
        this.logger = Logger.getLogger(ProcessActivityMonitor.class.getName());
        this.records = new ArrayList<>();
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

    public List<Triple<LocalDateTime, Long, Long>> getRecords() { return records; }

    abstract int readDiskStats();

    private static class WindowsMonitor extends ProcessActivityMonitor {

        WindowsMonitor(Process process, long sampleInterval) {
            super(process, sampleInterval);
        }

        @Override
        int readDiskStats() {
            int retVal = -1;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args,
                                   String.format("wmic path Win32_PerfRawData_PerfProc_Process where IDProcess=%d get IOReadBytesPersec,IOWriteBytesPersec", processId)
                                           .split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.hostName, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if ((retVal = executor.execute(false)) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (!message.startsWith("IOReadBytesPersec")) {
                            String[] tokens = Arrays.stream(message.split(" ")).filter(t -> !t.isEmpty()).toArray(String[]::new);
                            // values in kilobytes / second
                            records.add(new Triple<>(LocalDateTime.now(),
                                                     Long.parseLong(tokens[0]) / 1024,
                                                     Long.parseLong(tokens[1]) / 1024));
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return retVal;
        }
    }

    private static class LinuxMonitor extends ProcessActivityMonitor {

        LinuxMonitor(Process process, long sampleInterval) {
            super(process, sampleInterval);
        }

        @Override
        int readDiskStats() {
            int retVal = -1;
            try {
                List<String> args = new ArrayList<>();
                Collections.addAll(args, String.format("pidstat -d -p %d", processId).split(" "));
                Executor executor = Executor.create(ExecutorType.PROCESS, this.hostName, args);
                Consumer consumer = new Consumer();
                executor.setOutputConsumer(consumer);
                if ((retVal = executor.execute(false)) == 0) {
                    List<String> messages = consumer.getMessages();
                    for (String message : messages) {
                        if (!message.contains("PID")) {
                            String[] tokens = Arrays.stream(message.split(" ")).filter(t -> !t.isEmpty()).toArray(String[]::new);
                            Double read = Double.parseDouble(tokens[3]);
                            Double write = Double.parseDouble(tokens[4]);
                            // values are already in kilobytes/second
                            records.add(new Triple<>(LocalDateTime.now(), read.longValue(), write.longValue()));
                        }
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
            }
            return retVal;
        }
    }

    private static class Consumer implements OutputConsumer {
        private List<String> messages = new ArrayList<>();
        @Override
        public void consume(String message) {
            messages.add(message.replace("\r", ""));
        }
        List<String> getMessages() { return this.messages; }
    }
}
