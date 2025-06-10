package ro.cs.tao.utils.executors.monitoring;

import ro.cs.tao.utils.Triple;
import ro.cs.tao.utils.executors.Executor;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.RuntimeInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * A concrete implementation of the {@link ProcessActivityMonitor} for monitoring
 * disk activity of a process running on a Linux operating system.
 *
 * This class uses the `pidstat` utility to collect disk read and write statistics
 * for a specific process. The collected data includes the amount of data read
 * and written by the process, measured in kilobytes per second.
 *
 * The class periodically executes the `pidstat` command with the specified process ID
 * and parses its output to produce disk activity records. These records consist of
 * the timestamp, the number of bytes read, and the number of bytes written.
 */
class LinuxMonitor extends ProcessActivityMonitor {

    LinuxMonitor(Process process, long sampleInterval) {
        super(process, sampleInterval);
    }

    @Override
    int readDiskStats() {
        int retVal = -1;
        try {
            final List<String> args = new ArrayList<>();
            Collections.addAll(args, String.format("pidstat -d -p %d", processId).split(" "));
            final Executor<?> executor = Executor.create(ExecutorType.PROCESS, this.hostName, args);
            final SimpleConsumer consumer = new SimpleConsumer();
            executor.setOutputConsumer(consumer);
            if ((retVal = executor.execute(false)) == 0) {
                List<String> messages = consumer.getMessages();
                for (int i = 1; i < messages.size(); i++) {
                    String message = messages.get(i);
                    if (!message.contains("PID")) {
                        String[] tokens = Arrays.stream(message.split(" ")).filter(t -> !t.isEmpty()).toArray(String[]::new);
                        if (tokens.length > 5) {
                            final Double read = Double.parseDouble(tokens[4]);
                            final Double write = Double.parseDouble(tokens[5]);
                            // values are already in kilobytes/second
                            record = new Triple<>(LocalDateTime.now(), read.longValue(), write.longValue());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(RuntimeInfo.class.getName()).severe(ex.getMessage());
        }
        return retVal;
    }
}
