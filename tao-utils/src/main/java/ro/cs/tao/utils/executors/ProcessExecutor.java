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

    public ProcessExecutor(String nodeName, List<String> args, boolean asSU) {
        super(nodeName, args, asSU);
    }

    @Override
    public int execute(boolean logMessages) throws IOException, InterruptedException {
        Process process = null;
        BufferedReader outReader = null;
        int ret = -1;
        try {
            logger.info("[[" + host + "]] " + String.join(" ", arguments));
            ProcessBuilder pb = new ProcessBuilder(arguments);
            //redirect the error of the tool to the standard output
            pb.redirectErrorStream(true);
            pb.environment().putAll(System.getenv());
            //start the process
            process = pb.start();
            //get the process output
            InputStream inputStream = process.getInputStream();
            outReader = new BufferedReader(new InputStreamReader(inputStream));
            while (!isStopped()) {
                while (!isStopped && outReader.ready()) {
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
                if (!process.isAlive()) {
                    //isStopped the loop
                    stop();
                } else {
                    //yield the control to other threads
                    Thread.yield();
                }
            }
            ret = process.exitValue();
        } catch (IOException e) {
            logger.severe(String.format("[[%s]] failed: %s", host, e.getMessage()));
            wasCancelled = true;
            throw e;
        } finally {
            if (process != null) {
                // if the process is still running, force it to isStopped
                if (process.isAlive()) {
                    //destroy the process
                    process.destroyForcibly();
                }
                try {
                    //wait for the project to end.
                    ret = process.waitFor();
                } catch (InterruptedException ignored) {
                }

                //close the reader
                closeStream(outReader);
                //close all streams
                closeStream(process.getErrorStream());
                closeStream(process.getInputStream());
                closeStream(process.getOutputStream());
            }
        }

        return ret;
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
