package ro.cs.tao.utils.executors;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Executes a process with given arguments.
 *
 * @author Cosmin Cara
 */
public class ProcessExecutor extends Executor {
    private Process process;

    public ProcessExecutor(String nodeName, List<String> args, boolean asSU) {
        super(nodeName, args, asSU);
    }

    @Override
    public int execute(boolean logMessages) throws IOException, InterruptedException {
        BufferedReader outReader = null;
        int ret = 0x80000000;
        try {
            this.logger.info("[" + this.host + "] " + String.join(" ", arguments));
            resetProcess();
            ProcessBuilder pb = new ProcessBuilder(arguments);
            //redirect the error of the tool to the standard output
            pb.redirectErrorStream(true);
            pb.environment().putAll(System.getenv());
            //start the process
            this.process = pb.start();
            //get the process output
            InputStream inputStream = this.process.getInputStream();
            outReader = new BufferedReader(new InputStreamReader(inputStream));
            while (!isStopped()) {
                while (!this.isStopped && outReader.ready()) {
                    //read the process output line by line
                    String line = outReader.readLine();
                    //consume the line if possible
                    if (line != null && !"".equals(line.trim())) {
                        if (this.outputConsumer != null) {
                            this.outputConsumer.consume(line);
                        }
                        if (logMessages) {
                            this.logger.info(line);
                        }
                    }
                }
                // check if the project finished execution
                if (!this.process.isAlive()) {
                    //isStopped the loop
                    super.stop();
                } else {
                    //yield the control to other threads
                    Thread.yield();
                }
            }
            try {
                //wait for the project to end.
                this.process.waitFor();
            } catch (InterruptedException ignored) {
            }
            ret = this.process.exitValue();
        } catch (Exception e) {
            this.logger.severe(String.format("[%s] failed: %s", host, e.getMessage()));
            this.isStopped = true;
            throw e;
        } finally {
            closeStream(outReader);
            resetProcess();
        }
        return ret;
    }

    @Override
    public void suspend() {
        super.suspend();
        ProcessHelper.suspend(this.process);
    }

    @Override
    public void resume() {
        super.resume();
        ProcessHelper.resume(this.process);
    }

    @Override
    public void stop() {
        super.stop();
        ProcessHelper.terminate(this.process);
    }

    private void resetProcess() {
        if (this.process != null) {
            // if the process is still running, force it to isStopped
            if (this.process.isAlive()) {
                //destroy the process
                this.process.destroyForcibly();
            }
            try {
                //wait for the project to end.
                this.process.waitFor();
            } catch (InterruptedException ignored) {
            }
            //close all streams
            closeStream(this.process.getErrorStream());
            closeStream(this.process.getInputStream());
            closeStream(this.process.getOutputStream());
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
