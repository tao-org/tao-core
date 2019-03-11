package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.Triple;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.RuntimeInfo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

class WindowsMonitor extends ProcessActivityMonitor {

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
            SimpleConsumer consumer = new SimpleConsumer();
            executor.setOutputConsumer(consumer);
            if ((retVal = executor.execute(false)) == 0) {
                List<String> messages = consumer.getMessages();
                for (String message : messages) {
                    if (!message.startsWith("IOReadBytesPersec")) {
                        String[] tokens = Arrays.stream(message.split(" ")).filter(t -> !t.isEmpty()).toArray(String[]::new);
                        // values in kilobytes / second
                        Triple<LocalDateTime, Long, Long> newRecord = new Triple<>(LocalDateTime.now(),
                                                                                   Long.parseLong(tokens[0]) / 1024,
                                                                                   Long.parseLong(tokens[1]) / 1024);
                        if (lastRecord != null) {
                            long seconds = Duration.between(lastRecord.getKeyOne(), newRecord.getKeyOne()).getSeconds();
                            if (seconds > 0) {
                                record = new Triple<>(newRecord.getKeyOne(),
                                                      (newRecord.getKeyTwo() - lastRecord.getKeyTwo()) / seconds,
                                                      (newRecord.getKeyThree() - lastRecord.getKeyThree()) / seconds);
                            }
                        }
                        lastRecord = newRecord;
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
        }
        return retVal;
    }
}
