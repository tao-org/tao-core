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

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Executes a process with given arguments.
 *
 * @author Cosmin Cara
 */
public class ProcessExecutor extends Executor<Process> {

    ProcessExecutor(String nodeName, List<String> args, boolean asSU) {
        super(nodeName, args, asSU);
    }

    ProcessExecutor(String nodeName, List<String> args, boolean asSU, File workingDir) {
        super(nodeName, args, asSU);
        this.workingDirectory = workingDir;
    }

    @Override
    public boolean canConnect() {
        return true;
    }

    @Override
    public int execute(boolean logMessages) throws IOException {
        BufferedReader outReader = null;
        int ret = 0x80000000;
        try {
            this.logger.finest("[" + this.host + "] " + String.join(" ", arguments));
            resetProcess();
            ProcessBuilder pb = new ProcessBuilder(arguments);
            if (this.workingDirectory != null) {
                pb.directory(this.workingDirectory);
            }
            //redirect the error of the tool to the standard output
            pb.redirectErrorStream(true);
            pb.environment().putAll(System.getenv());
            if (asSuperUser) {
                insertSudoParams();
            }
            //start the process
            this.channel = pb.start();
            if (asSuperUser) {
                writeSudoPassword();
            }
            if (this.monitor != null) {
                this.monitor.attachTo(this.channel);
            }
            //get the process output
            InputStream inputStream = this.channel.getInputStream();
            outReader = new BufferedReader(new InputStreamReader(inputStream));
            while (!isStopped()) {
                String line;
                while (!this.isStopped && (line = outReader.readLine()) != null) {
                    //consume the line if possible
                    if (!"".equals(line.trim())) {
                        if (this.outputConsumer != null) {
                            this.outputConsumer.consume(line);
                        }
                        if (logMessages) {
                            this.logger.finest(line);
                        }
                    }
                }
                // check if the process finished execution
                if (!this.channel.isAlive()) {
                    this.isStopped = true;
                } else {
                    //yield the control to other threads
                    Thread.yield();
                }
            }
            try {
                //wait for the process to end.
                this.channel.waitFor(2000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException ignored) {
                logger.warning(String.format("Timeout occurred while waiting for the process [pid=%d] to exit.",
                                             ProcessHelper.getPID(this.channel)));
            }
            try {
                ret = this.channel.exitValue();
            } catch (IllegalThreadStateException ignored) {
            }
        } catch (Exception e) {
            this.isStopped = true;
            String message = ExceptionUtils.getStackTrace(e);
            message = message.substring(0, message.indexOf("\tat "));
            throw new IOException(String.format("[%s] failed: %s", host, message));
        } finally {
            closeStream(outReader);
            if (this.monitor != null) {
                this.monitor.detachFrom(this.channel);
            }
            resetProcess();
        }
        return ret;
    }

    @Override
    public void suspend() {
        super.suspend();
        ProcessHelper.suspend(this.channel);
    }

    @Override
    public void resume() {
        super.resume();
        ProcessHelper.resume(this.channel);
    }

    @Override
    public void stop() {
        super.stop();
        ProcessHelper.terminate(this.channel);
    }

    @Override
    protected void insertSudoParams() {
        if (!SystemUtils.IS_OS_WINDOWS) {
            super.insertSudoParams();
        }
    }

    @Override
    protected void writeSudoPassword() throws IOException {
        if (!SystemUtils.IS_OS_WINDOWS) {
            super.writeSudoPassword();
        }
    }

    private void resetProcess() {
        if (this.channel != null) {
            // if the process is still running, force it to isStopped
            if (this.channel.isAlive()) {
                //destroy the process
                this.channel.destroyForcibly();
            }
            try {
                //wait for the project to end.
                this.channel.waitFor();
            } catch (InterruptedException ignored) {
            }
            //close all streams
            closeStream(this.channel.getErrorStream());
            closeStream(this.channel.getInputStream());
            closeStream(this.channel.getOutputStream());
        }
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                //nothing to do.
            }
        }
    }
}
