/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.utils.executors;

import com.jcraft.jsch.Channel;
import org.apache.commons.lang3.SystemUtils;
import ro.cs.tao.utils.executors.monitoring.ActivityListener;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Base class for process executors that also acts as an Executor factory.
 * The parameter type of subclasses represent the unit that handles the respective execution.
 *
 * @author Cosmin Cara
 */
public abstract class Executor<T> implements Runnable {
    public static final String SHELL_COMMAND_SEPARATOR = ";";
    private static final String SHELL_COMMAND_SEPARATOR_AMP = "&&";
    private static final String SHELL_COMMAND_SEPARATOR_BAR = "||";
    private static final NamedThreadPoolExecutor executorService;
    private static final Logger staticLogger;
    private static final AtomicLong requestedMemory;
    private static final Map<Executor<?>, Long> memoryRequirements;

    static {
        executorService = new NamedThreadPoolExecutor("process-exec", Runtime.getRuntime().availableProcessors());
        staticLogger = Logger.getLogger(Executor.class.getName());
        requestedMemory = new AtomicLong(0L);
        memoryRequirements = Collections.synchronizedMap(new HashMap<>());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                executorService.shutdownNow();
            }
        });
    }

    T channel;
    String host;
    String user;
    String password;
    volatile boolean isStopped;
    private volatile boolean isSuspended;
    List<String> arguments;
    Logger logger;
    boolean asSuperUser;
    OutputConsumer outputConsumer;
    File workingDirectory;
    ActivityListener monitor;

    private volatile int retCode = Integer.MAX_VALUE;
    private final CountDownLatch counter;

    /**
     * Submits for execution the given command and returns the executor object.
     * The caller is responsible of waiting for the completion of the execution by waiting on
     * Executor.getWaitObject() latch.
     *
     * @param outputConsumer    The counsumer of the process output
     * @param job               The command (job) to be executed
     * @return                  The Executor object that is responsible of execution
     */
    public static Executor<?> execute(OutputConsumer outputConsumer, ExecutionUnit job) {
        return execute(outputConsumer, job, null);
    }

    public static Executor<?> execute(OutputConsumer outputConsumer, ExecutionUnit job, ActivityListener monitor) {
        Executor<?> executor = prepare(outputConsumer, job);
        awaitForConditions(job);
        if (monitor != null) {
            executor.setMonitor(monitor);
        }
        executorService.submit(executor);
        printQueue();
        return executor;
    }
    /**
     * Creates an executor for the given command and returns the executor object.
     * The caller is responsible of actually invoking the execution on the returned executor.
     *
     * @param outputConsumer    The counsumer of the process output
     * @param job               The command (job) to be executed
     * @return                  The Executor object that is responsible of execution
     */
    public static Executor<?> prepare(OutputConsumer outputConsumer, ExecutionUnit job) {
        if (job == null) {
            throw new IllegalArgumentException("ExecutionUnit must not be null");
        }
        Executor<?> executor = create(job.getType(),
                                   job.getHost(),
                                   job.getArguments(),
                                   job.asSuperUser(),
                                   job.getSshMode());
        executor.setUser(job.getUser());
        executor.setPassword(job.getPassword());
        executor.setOutputConsumer(outputConsumer);
        memoryRequirements.put(executor, job.getMinMemory() != null ? job.getMinMemory() : 0L);
        return executor;
    }

    /**
     * Submits for execution the given command and returns the executor object.
     * The caller is responsible of waiting for the completion of the execution by waiting on
     * Executor.getWaitObject() latch.
     *
     * @param outputConsumer    The counsumer of the process output
     * @param workingDirectory  The context directory
     * @param job               The command (job) to be executed
     * @return                  The Executor object that is responsible of execution
     */
    public static Executor<?> execute(OutputConsumer outputConsumer, File workingDirectory, ExecutionUnit job) {
        Executor<?> executor = prepare(outputConsumer, workingDirectory, job);
        awaitForConditions(job);
        executorService.submit(executor);
        printQueue();
        return executor;
    }

    public static Executor<?> prepare(OutputConsumer outputConsumer, File workingDirectory, ExecutionUnit job) {
        if (job == null) {
            throw new IllegalArgumentException("ExecutionUnit must not be null");
        }
        Executor<?> executor = create(job.getType(),
                                   job.getHost(),
                                   job.getArguments(),
                                   job.asSuperUser(),
                                   job.getSshMode(),
                                   workingDirectory);
        executor.setUser(job.getUser());
        executor.setPassword(job.getPassword());
        executor.setOutputConsumer(outputConsumer);
        memoryRequirements.put(executor, job.getMinMemory() != null ? job.getMinMemory() : 0L);
        return executor;
    }

    /**
     * Executes the given command setting a fixed timeout.
     * When the timeout expires, the execution will be terminated.
     *
     * @param outputConsumer    The counsumer of the process output
     * @param timeout           The timeout in seconds
     * @param job               The command (job) to be executed
     * @return                  The return code of the execution
     */
    public static int execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit job) {
        return execute(outputConsumer, timeout, job, null);
    }

    public static int execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit job, ActivityListener monitor) {
        Executor<?> executor = execute(outputConsumer, job, monitor);
        try {
            executor.getWaitObject().await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            staticLogger.severe(String.format("The job %s failed to complete within the allocated time [%s s] and is being terminated",
                                              job, timeout));
        } finally {
            executor.stop();
            printQueue();
        }
        return executor.getReturnCode();
    }

    public static Executor<?>[] prepare(OutputConsumer outputConsumer, ExecutionUnit... jobs) {
        if (jobs == null || jobs.length == 0) {
            throw new IllegalArgumentException("At least one ExecutionUnit expected");
        }
        Executor<?>[] executors = new Executor[jobs.length];
        for (int i = 0; i < jobs.length; i++) {
            ExecutionUnit job = jobs[i];
            Executor<?> executor = create(job.getType(),
                                       job.getHost(),
                                       job.getArguments(),
                                       job.asSuperUser(),
                                       job.getSshMode());
            executor.setUser(job.getUser());
            executor.setPassword(job.getPassword());
            executor.setOutputConsumer(outputConsumer);
            executors[i] = executor;
            memoryRequirements.put(executor, job.getMinMemory() != null ? job.getMinMemory() : 0L);
        }
        return executors;
    }

    /**
     * Submits for execution the given commands and returns the executor objects.
     * The caller is responsible of waiting for the completion of the execution by waiting on each
     * Executor.getWaitObject() latch.
     *
     * @param outputConsumer    The consumer of the process output
     * @param jobs              One or more command descriptors
     */
    public static Executor<?>[] execute(OutputConsumer outputConsumer, ExecutionUnit... jobs) {
        Executor<?>[] executors = prepare(outputConsumer, jobs);
        for (int i = 0; i < jobs.length; i++) {
            //awaitForConditions(jobs[i]);
            executorService.submit(executors[i]);
            printQueue();
        }
        return executors;
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
        return execute(outputConsumer, timeout, null, jobs);
    }
    public static int[] execute(OutputConsumer outputConsumer, long timeout, ActivityListener monitor, ExecutionUnit... jobs) {
        Executor<?>[] executors = prepare(outputConsumer, jobs);
        int[] retCodes = new int[executors.length];
        final CountDownLatch aggregateLatch = new CountDownLatch(jobs.length);
        final Thread[] waitThreads = new Thread[jobs.length];
        for (int i = 0; i < executors.length; i++) {
            final int finalI = i;
            waitThreads[i] = new Thread(() -> {
                try {
                    if (monitor != null) {
                        executors[finalI].setMonitor(monitor);
                    }
                    executorService.submit(executors[finalI]);
                    printQueue();
                    executors[finalI].getWaitObject().await(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException iex) {
                    staticLogger.severe(String.format("The job %s failed to complete within the allocated time [%s s] and is being terminated",
                                                      jobs[finalI], timeout));
                } finally {
                    executors[finalI].stop();
                    printQueue();
                    aggregateLatch.countDown();
                }
            });
            awaitForConditions(jobs[i]);
            waitThreads[i].start();
        }
        try {
            aggregateLatch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) { }
        for (int i = 0; i < executors.length; i++) {
            executors[i].stop();
            printQueue();
            waitThreads[i] = null;
            retCodes[i] = executors[i].getReturnCode();
        }
        return retCodes;
    }

    public static Executor<?> create(ExecutorType type, String host, List<String> arguments) {
        return create(type, host, arguments, false, SSHMode.EXEC, null);
    }

    public static Executor<?> create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser) {
        return create(type, host, arguments, asSuperUser, SSHMode.EXEC, null);
    }

    public static Executor<?> create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, SSHMode mode) {
        return create(type, host, arguments, asSuperUser, mode, null);
    }

    public static Executor<?> create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, SSHMode mode, File workingDir) {
        Executor<?> executor = null;
        switch (type) {
            case PROCESS:
                executor = new ProcessExecutor(host, arguments, asSuperUser, workingDir);
                break;
            case SSH2:
                executor = new SSHExecutor(host, arguments, asSuperUser, mode);
                break;
        }
        return executor;
    }

    public static Executor<?> create(ExecutorType type, String host, int port, List<String> arguments, boolean asSuperUser, SSHMode mode) {
        if (type != ExecutorType.SSH2) {
            throw new IllegalArgumentException("Wrong executor type if port specified");
        }
        return new SSHExecutor(host, port, arguments, asSuperUser, mode);
    }

    private static void awaitForConditions(ExecutionUnit job) {
        boolean canExecute = canExecute(job);
        while (!canExecute) {
            try {
                Thread.sleep(10000);
                canExecute = canExecute(job);
            } catch (Exception e) {
                Logger.getLogger(Executor.class.getName()).warning(e.getMessage());
                canExecute = true;
            }
        }
    }

    private static synchronized boolean canExecute(ExecutionUnit executionUnit) {
        boolean retVal;
        final Long minMemory = executionUnit.getMinMemory();
        if (minMemory != null) {
            try {
                RuntimeInfo runtimeInfo = RuntimeInfo.createInspector(executionUnit.getHost(),
                                                                      executionUnit.getUser(), executionUnit.getPassword());
                final long availMem = runtimeInfo.getAvailableMemoryMB();
                final long requested = requestedMemory.get();
                retVal = (availMem - requested) >= minMemory;
                Logger.getLogger(Executor.class.getName())
                        .fine(String.format("Job %s requires %dMB of RAM, system has %dMB available (of which %dMB requested by other jobs)",
                                            executionUnit.hashCode(), minMemory, availMem, requested));
            } catch (Exception e) {
                Logger.getLogger(Executor.class.getName()).warning(e.getMessage());
                retVal = true;
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Constructs an executor with given arguments and a shared latch counter.
     *
     * @param host      The execution node name
     * @param args          The arguments
     */
    Executor(String host, List<String> args, boolean asSU) {
        this(host, args, asSU, false);
    }

    /**
     * Constructs an executor with given arguments and a shared latch counter.
     *
     * @param host      The execution node name
     * @param args          The arguments
     */
    Executor(String host, List<String> args, boolean asSU, boolean ensureTokenizedArgs) {
        this.isStopped = false;
        this.isSuspended = false;
        this.host = host;
        this.arguments = ensureTokenizedArgs ? ensureTokenized(args) : args;
        this.counter = new CountDownLatch(1);
        this.asSuperUser = asSU;
        logger = Logger.getLogger(Executor.class.getName());
    }

    public void setMonitor(ActivityListener monitor) { this.monitor = monitor; }

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

    public boolean isSuspended() { return this.isSuspended; }

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
            requestedMemory.getAndAdd(memoryRequirements.get(this));
            retCode = execute(true);
        } catch (Exception e) {
            retCode = -255;
            logger.severe(e.getMessage());
        } finally {
            if (this.counter != null) {
                counter.countDown();
            }
            requestedMemory.getAndAdd(-memoryRequirements.remove(this));
            isStopped = true;
        }
    }

    /**
     * Performs the actual execution, optionally logging the execution output and returning the output as a list
     * of messages.
     *
     * @param logMessages   If <code>true</code>, the output will be logged
     *
     */
    public abstract int execute(boolean logMessages) throws Exception;

    public abstract boolean canConnect();

    private List<String> ensureTokenized(List<String> arguments) {
        List<String> args = new ArrayList<>();
        if (arguments != null) {
            arguments.forEach(arg -> {
                if (arg.startsWith("\"")) {
                    arg = arg.substring(1);
                }
                if (arg.endsWith("\"")) {
                    arg = arg.substring(0, arg.length() - 1);
                }
                args.addAll(Arrays.asList(arg.split(" ")));
            });
        }
        return args;
    }

    protected List<String> formatArguments() {
        if (SystemUtils.IS_OS_LINUX &&
                (arguments.contains(";") || arguments.contains("&") || arguments.contains("&&") || arguments.contains("|") || arguments.contains("||"))) {
            List<String> newArgs = new ArrayList<>();
            newArgs.add("/bin/sh");
            newArgs.add("-c");
            final String quoteChar = arguments.contains(" sed ") ? "'" : "";
            newArgs.add(quoteChar + String.join(" ", arguments) + quoteChar);
            return newArgs;
        } else {
            return arguments;
        }
    }

    protected void insertSudoParams() {
        int idx = 0;
        String curArg;
        List<String> sudoArgs = new ArrayList<String>() {{
               add("sudo");
               add("-S");
               add("-p");
               add("''");
        }};
        while (idx < arguments.size()) {
            curArg = arguments.get(idx);
            if (SHELL_COMMAND_SEPARATOR.equals(curArg) || SHELL_COMMAND_SEPARATOR_AMP.equals(curArg) ||
                    SHELL_COMMAND_SEPARATOR_BAR.equals(curArg)) {
                arguments.addAll(idx + 1, sudoArgs);
            }
            idx++;
        }
        arguments.addAll(0, sudoArgs);
    }

    protected void writeSudoPassword() throws IOException {
        OutputStream outputStream;
        if (this.channel instanceof Channel) {
            outputStream = ((Channel) this.channel).getOutputStream();
        } else if (this.channel instanceof Process) {
            outputStream = ((Process) this.channel).getOutputStream();
        } else {
            throw new IllegalArgumentException("This type of channel is not supported");
        }
        outputStream.write((this.password + "\n").getBytes());
        outputStream.flush();
    }

    private static void printQueue() {
        staticLogger.finest(String.format("%s running tasks, %s queued tasks",
                                          executorService.getActiveCount(),
                                          executorService.getQueue().size()));
    }
}
