package ro.cs.tao.utils.executors;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Base class for process executors
 *
 * @author Cosmin Cara
 */
public abstract class Executor implements Runnable {
    public static final String SHELL_COMMAND_SEPARATOR = ";";
    public static final String SHELL_COMMAND_SEPARATOR_AMP = "&&";
    public static final String SHELL_COMMAND_SEPARATOR_BAR = "||";
    private static final ExecutorService executorService;
    private static final Logger mainLogger;

    static {
        mainLogger = Logger.getLogger(Executor.class.getSimpleName());
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    String host;
    String user;
    String password;
    volatile boolean isStopped;
    volatile boolean wasCancelled;
    List<String> arguments;
    Logger logger;
    boolean asSuperUser;
    OutputConsumer outputConsumer;

    private volatile int retCode = Integer.MAX_VALUE;
    private CountDownLatch counter;

    public static int execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit job) {
        int retCode;
        if (job != null) {
            CountDownLatch sharedCounter = new CountDownLatch(1);
            Executor executor = create(job.getType(),
                    job.getHost(),
                    job.getArguments(),
                    job.asSuperUser(),
                    sharedCounter,
                    job.getSshMode());
            executor.setUser(job.getUser());
            executor.setPassword(job.getPassword());
            executor.setOutputConsumer(outputConsumer);
            executorService.submit(executor);
            try {
                sharedCounter.await(timeout, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                mainLogger.severe("Operation timed out");
            }
            if (!executor.hasCompleted()) {
                mainLogger.info("[[" + executor.getHost() + "]] Node still running. Its output will not be complete.");
            }
            retCode = executor.getReturnCode();
        } else {
            retCode = Integer.MIN_VALUE;
        }
        return retCode;
    }

    /**
     * Executes the given commands setting a fixed timeout.
     * When the timeout expires, the executions will be terminated.
     *
     * @param outputConsumer    The consumer of the process output
     * @param timeout   The timeout in seconds
     * @param jobs      One or more command descriptors
     */
    public static int[] execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit... jobs) {
        int[] retCodes;
        if (jobs != null && jobs.length > 0) {
            Set<Executor> processes = new HashSet<>();
            CountDownLatch sharedCounter = new CountDownLatch(jobs.length);
            for (ExecutionUnit job : jobs) {
                Executor executor = create(job.getType(),
                        job.getHost(),
                        job.getArguments(),
                        job.asSuperUser(),
                        sharedCounter,
                        job.getSshMode());
                executor.setUser(job.getUser());
                executor.setPassword(job.getPassword());
                executor.setOutputConsumer(outputConsumer);
                executorService.submit(executor);
                processes.add(executor);
            }
            try {
                sharedCounter.await(timeout, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                mainLogger.severe("Operation timed out");
            }
            processes.stream()
                    .filter(executor -> !executor.hasCompleted())
                    .forEach(executor -> mainLogger.info("[[" + executor.getHost() + "]] Node still running. Its output will not be complete."));
            retCodes = processes.stream().mapToInt(Executor::getReturnCode).toArray();
            processes.clear();
        } else {
            retCodes = new int[0];
        }
        return retCodes;
    }

    private static Executor create(ExecutorType type, String host, List<String> arguments, CountDownLatch synchronisationCounter) {
        return create(type, host, arguments, false, synchronisationCounter, SSHMode.EXEC);
    }

    private static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, CountDownLatch synchronisationCounter) {
        return create(type, host, arguments, asSuperUser, synchronisationCounter, SSHMode.EXEC);
    }

    private static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, CountDownLatch synchronisationCounter, SSHMode mode) {
        Executor executor = null;
        switch (type) {
            case PROCESS:
                executor = new ProcessExecutor(host, arguments, asSuperUser, synchronisationCounter);
                break;
            case SSH2:
                executor = new SSHExecutor(host, arguments, asSuperUser, synchronisationCounter, mode);
                break;
        }
        return executor;
    }

    /**
     * Constructs an executor with given arguments and a shared latch counter.
     *
     * @param host      The execution node name
     * @param args          The arguments
     * @param sharedCounter The shared latch counter.
     */
    Executor(String host, List<String> args, boolean asSU, CountDownLatch sharedCounter) {
        this.isStopped = false;
        this.wasCancelled = false;
        this.host = host;
        this.arguments = args;
        this.counter = sharedCounter;
        this.asSuperUser = asSU;
        logger = Logger.getLogger(Executor.class.getSimpleName());
    }

    /**
     * Returns the process exit code.
     */
    public int getReturnCode() { return this.retCode; }

    private String getHost() { return this.host; }

    /**
     * Signals the stop of the execution.
     */
    void stop() {
        this.isStopped = true;
    }

    /**
     * Checks if the process is/has stopped.
     */
    boolean isStopped() {
        return this.isStopped;
    }

    private boolean hasCompleted() { return this.retCode != Integer.MAX_VALUE; }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOutputConsumer(OutputConsumer outputConsumer) {
        this.outputConsumer = outputConsumer;
    }

    @Override
    public void run() {
        Instant start = Instant.now();
        try {
            retCode = execute(true);
            logger.info(String.format("[[%s]] completed %s", host, retCode == 0 ? "OK" : "NOK (code " + String.valueOf(retCode) + ")"));
            if (this.counter != null) {
                counter.countDown();
                logger.info(String.format("Active nodes: %s", this.counter.getCount()));
            }
        } catch (Exception e) {
            retCode = -255;
            logger.severe(String.format("[[%s]] produced an error: %s", host, e.getMessage()));
        } finally {
            Instant end = Instant.now();
            long seconds = Duration.between(start, end).getSeconds();
            long hours = seconds / 3600;
            seconds -= hours * 3600;
            long minutes = seconds / 60;
            seconds -= minutes * 60;
            logger.info(String.format("[[%s]] Execution took %02dh%02dm%02ds", host, hours, minutes, seconds));
        }
    }

    /**
     * Performs the actual execution, optionally logging the execution output and returning the output as a list
     * of messages.
     *
     * @param logMessages   If <code>true</code>, the output will be logged
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public abstract int execute(boolean logMessages) throws Exception;
}
