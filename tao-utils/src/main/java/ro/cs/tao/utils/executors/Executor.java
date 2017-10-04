package ro.cs.tao.utils.executors;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

    static {
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    String host;
    String user;
    String password;
    volatile boolean isStopped;
    volatile boolean isSuspended;
    List<String> arguments;
    Logger logger;
    boolean asSuperUser;
    OutputConsumer outputConsumer;

    private volatile int retCode = Integer.MAX_VALUE;
    private final CountDownLatch counter;

    public static Executor execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit job) {
        if (job == null) {
            throw new IllegalArgumentException("ExecutionUnit must not be null");
        }
        Executor executor = create(job.getType(),
                                   job.getHost(),
                                   job.getArguments(),
                                   job.asSuperUser(),
                                   job.getSshMode());
        executor.setUser(job.getUser());
        executor.setPassword(job.getPassword());
        executor.setOutputConsumer(outputConsumer);
        executorService.submit(executor);
        return executor;
    }

    /**
     * Executes the given commands setting a fixed timeout.
     * When the timeout expires, the executions will be terminated.
     *
     * @param outputConsumer    The consumer of the process output
     * @param timeout   The timeout in seconds
     * @param jobs      One or more command descriptors
     */
    public static Executor[] execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit... jobs) {
        if (jobs == null || jobs.length == 0) {
            throw new IllegalArgumentException("At least one ExecutionUnit expected");
        }
        Executor[] executors = new Executor[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            ExecutionUnit job = jobs[i];
            Executor executor = create(job.getType(),
                                       job.getHost(),
                                       job.getArguments(),
                                       job.asSuperUser(),
                                       job.getSshMode());
            executor.setUser(job.getUser());
            executor.setPassword(job.getPassword());
            executor.setOutputConsumer(outputConsumer);
            executorService.submit(executor);
            executors[i] = executor;
        }
        return executors;
    }

    private static Executor create(ExecutorType type, String host, List<String> arguments) {
        return create(type, host, arguments, false, SSHMode.EXEC);
    }

    private static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser) {
        return create(type, host, arguments, asSuperUser, SSHMode.EXEC);
    }

    private static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, SSHMode mode) {
        Executor executor = null;
        switch (type) {
            case PROCESS:
                executor = new ProcessExecutor(host, arguments, asSuperUser);
                break;
            case SSH2:
                executor = new SSHExecutor(host, arguments, asSuperUser, mode);
                break;
        }
        return executor;
    }

    /**
     * Constructs an executor with given arguments and a shared latch counter.
     *
     * @param host      The execution node name
     * @param args          The arguments
     */
    Executor(String host, List<String> args, boolean asSU) {
        this.isStopped = false;
        this.isSuspended = false;
        this.host = host;
        this.arguments = args;
        this.counter = new CountDownLatch(1);
        this.asSuperUser = asSU;
        logger = Logger.getLogger(Executor.class.getSimpleName());
    }

    /**
     * Returns the process exit code.
     */
    public int getReturnCode() { return this.retCode; }

    /**
     * Signals the stop of the execution.
     */
    public void stop() { this.isStopped = true; }

    /**
     * Checks if the process is/has stopped.
     */
    public boolean isStopped() {
        return this.isStopped;
    }

    public void suspend() { this.isSuspended = true; }

    public void resume() { this.isSuspended = false; }

    public boolean isSuspended() { return this.isSuspended; };

    public boolean isRunning() { return !this.isStopped && !this.isSuspended; }

    public boolean hasCompleted() { return this.retCode != Integer.MAX_VALUE; }

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setOutputConsumer(OutputConsumer outputConsumer) { this.outputConsumer = outputConsumer; }

    public CountDownLatch getWaitObject() { return this.counter; }

    @Override
    public void run() {
        try {
            isStopped = isSuspended = false;
            retCode = execute(true);
        } catch (Exception e) {
            retCode = -255;
            logger.severe(e.getMessage());
        } finally {
            if (this.counter != null) {
                counter.countDown();
            }
            isStopped = true;
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
