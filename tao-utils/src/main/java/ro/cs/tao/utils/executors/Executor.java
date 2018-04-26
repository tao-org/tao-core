/*
 * Copyright (C) 2017 CS ROMANIA
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
import ro.cs.tao.utils.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
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
    private static final ExecutorService executorService;

    static {
        executorService = new NamedThreadPoolExecutor("process-exec", Runtime.getRuntime().availableProcessors());
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

    /**
     * Submits for execution the given commands and returns the executor objects.
     * The caller is responsible of waiting for the completion of the execution by waiting on each
     * Executor.getWaitObject() latch.
     *
     * @param outputConsumer    The consumer of the process output
     * @param jobs              One or more command descriptors
     */
    public static Executor[] execute(OutputConsumer outputConsumer, ExecutionUnit... jobs) {
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

    /**
     * Executes the given commands setting a fixed timeout.
     * When the timeout expires, the executions will be terminated.
     *
     * @param outputConsumer    The consumer of the process output
     * @param timeout   The timeout in seconds
     * @param jobs      One or more command descriptors
     */
    public static int[] execute(OutputConsumer outputConsumer, long timeout, ExecutionUnit... jobs) {
        Executor[] executors = execute(outputConsumer, jobs);
        int[] retCodes = new int[executors.length];
        long duration, remaining;
        remaining = timeout * 1000;
        for (Executor executor : executors) {
            if (remaining > 0) {
                duration = System.nanoTime();
                try {
                    executor.getWaitObject().await(timeout, TimeUnit.SECONDS);
                } catch (InterruptedException ignored) {
                    executor.stop();
                }
                duration = System.nanoTime() - duration;
                remaining -= duration;
            }
        }
        Arrays.stream(executors).filter(Executor::isRunning).forEach(Executor::stop);
        for (int i = 0; i < executors.length; i++) {
            retCodes[i] = executors[i].getReturnCode();
        }
        return retCodes;
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments) {
        return create(type, host, arguments, false, SSHMode.EXEC);
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser) {
        return create(type, host, arguments, asSuperUser, SSHMode.EXEC);
    }

    public static Executor create(ExecutorType type, String host, List<String> arguments, boolean asSuperUser, SSHMode mode) {
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
            //noinspection ConstantConditions
            if (Platform.ID.win != Platform.getCurrentPlatform().getId()) {
               /*add("runas");
               add("/user:");
               add(Executor.this.user);
           } else {*/
               add("sudo");
               add("-S");
               add("-p");
               add("''");
           }
        }};
        while (idx < arguments.size()) {
            curArg = arguments.get(idx);
            if (SHELL_COMMAND_SEPARATOR.equals(curArg) || SHELL_COMMAND_SEPARATOR_AMP.equals(curArg) ||
                    SHELL_COMMAND_SEPARATOR_BAR.equals(curArg)) {
                arguments.addAll(idx + 1, sudoArgs);;
            }
            idx++;
        }
        arguments.addAll(0, sudoArgs);
    }

    protected void writeSudoPassword() throws IOException {
        if (Platform.ID.win != Platform.getCurrentPlatform().getId()) {
            OutputStream outputStream = null;
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
}
