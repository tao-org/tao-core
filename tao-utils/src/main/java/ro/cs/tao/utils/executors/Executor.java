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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Base class for process executors
 *
 * @author Cosmin Cara
 */
public abstract class Executor<T> implements Runnable {
    public static final String SHELL_COMMAND_SEPARATOR = ";";
    public static final String SHELL_COMMAND_SEPARATOR_AMP = "&&";
    public static final String SHELL_COMMAND_SEPARATOR_BAR = "||";
    private static final NamedThreadPoolExecutor executorService;
    private static final Logger staticLogger;

    static {
        executorService = new NamedThreadPoolExecutor("process-exec", Runtime.getRuntime().availableProcessors());
        staticLogger = Logger.getLogger(Executor.class.getName());
    }

    T channel;
    String host;
    String user;
    String password;
    volatile boolean isStopped;
    volatile boolean isSuspended;
    List<String> arguments;
    Logger logger;
    boolean asSuperUser;
    OutputConsumer outputConsumer;
    File workingDirectory;

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
    public static Executor execute(OutputConsumer outputConsumer, ExecutionUnit job) {
        Executor executor = prepare(outputConsumer, job);
        awaitForConditions(job);
        executorService.submit(executor);
        staticLogger.finest(String.format("%s running tasks, %s queued tasks",
                                          executorService.getActiveCount(),
                                          executorService.getQueue().size()));
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
    public static Executor prepare(OutputConsumer outputConsumer, ExecutionUnit job) {
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
    public static Executor execute(OutputConsumer outputConsumer, File workingDirectory, ExecutionUnit job) {
        Executor executor = prepare(outputConsumer, workingDirectory, job);
        awaitForConditions(job);
        executorService.submit(executor);
        staticLogger.finest(String.format("%s running tasks, %s queued tasks",
                                          executorService.getActiveCount(),
                                          executorService.getQueue().size()));
        return executor;
    }

    public static Executor prepare(OutputConsumer outputConsumer, File workingDirectory, ExecutionUnit job) {
        if (job == null) {
            throw new IllegalArgumentException("ExecutionUnit must not be null");
        }
        Executor executor = create(job.getType(),
                                   job.getHost(),
                                   job.getArguments(),
                                   job.asSuperUser(),
                                   job.getSshMode(),
                                   workingDirectory);
        executor.setUser(job.getUser());
        executor.setPassword(job.getPassword());
        executor.setOutputConsumer(outputConsumer);
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
        Executor executor = execute(outputConsumer, job);
        try {
            executor.getWaitObject().await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            executor.stop();
        }
        return executor.getReturnCode();
    }

    public static Executor[] prepare(OutputConsumer outputConsumer, ExecutionUnit... jobs) {
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
            executors[i] = executor;
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
    public static Executor[] execute(OutputConsumer outputConsumer, ExecutionUnit... jobs) {
        Executor[] executors = prepare(outputConsumer, jobs);
        for (int i = 0; i < jobs.length; i++) {
            //awaitForConditions(jobs[i]);
            executorService.submit(executors[i]);
            staticLogger.finest(String.format("%s running tasks, %s queued tasks",
                                              executorService.getActiveCount(),
                                              executorService.getQueue().size()));
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
        Executor[] executors = prepare(outputConsumer, jobs);
        int[] retCodes = new int[executors.length];
        final CountDownLatch aggregateLatch = new CountDownLatch(jobs.length);
        final Thread[] waitThreads = new Thread[jobs.length];
        for (int i = 0; i < executors.length; i++) {
            final int finalI = i;
            waitThreads[i] = new Thread(() -> {
                try {
                    executorService.submit(executors[finalI]);
                    staticLogger.finest(String.format("%s running tasks, %s queued tasks",
                                                      executorService.getActiveCount(),
                                                      executorService.getQueue().size()));
                    executors[finalI].getWaitObject().await(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException iex) {
                    executors[finalI].stop();
                } finally {
                    aggregateLatch.countDown();
                }
            });
            /*boolean canExecute = canExecute(jobs[i]);
            while (!canExecute) {
                try {
                    Thread.sleep(10000);
                    canExecute = canExecute(jobs[i]);
                } catch (Exception e) {
                    Logger.getLogger(Executor.class.getName()).warning(e.getMessage());
                    canExecute = true;
                }
            }*/
            awaitForConditions(jobs[i]);
            waitThreads[i].start();
        }
        try {
            aggregateLatch.await(timeout, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) { }
        for (int i = 0; i < executors.length; i++) {
            executors[i].stop();
            waitThreads[i] = null;
            retCodes[i] = executors[i].getReturnCode();
        }
        return retCodes;
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments) {
        return create(type, host, arguments, false, SSHMode.EXEC, null);
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser) {
        return create(type, host, arguments, asSuperUser, SSHMode.EXEC, null);
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, SSHMode mode) {
        return create(type, host, arguments, asSuperUser, mode, null);
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, SSHMode mode, File workingDir) {
        Executor executor = null;
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

    public static Executor create(ExecutorType type, String host, int port, List<String> arguments, boolean asSuperUser, SSHMode mode) {
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

    private static boolean canExecute(ExecutionUnit executionUnit) {
        boolean retVal;
        if (executionUnit.getMinMemory() != null) {
            try {
                RuntimeInfo runtimeInfo = RuntimeInfo.createInspector(executionUnit.getHost(),
                                                                      executionUnit.getUser(), executionUnit.getPassword());
                long availMem = runtimeInfo.getAvailableMemoryMB();
                retVal = availMem >= executionUnit.getMinMemory();
                Logger.getLogger(Executor.class.getName())
                        .fine(String.format("Job %s requires %sMB of RAM, system has %sMB available",
                                            executionUnit.hashCode(), executionUnit.getMinMemory(), availMem));
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

    /**
     * Returns the process exit code.
     */
    public int getReturnCode() { return this.retCode; }

    /**
     * Signals the stop of the execution.
     */
    public void stop() {
        this.isStopped = true;
        staticLogger.finest(String.format("%s running tasks, %s queued tasks",
                                          executorService.getActiveCount(),
                                          executorService.getQueue().size()));
    }

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
}
