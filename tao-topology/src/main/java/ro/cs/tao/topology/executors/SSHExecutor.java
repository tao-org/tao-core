package ro.cs.tao.topology.executors;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Executor class based on JSch. It uses SSH2 for remote host connection and command invocation.
 *
 * @author Cosmin Cara
 */
public class SSHExecutor extends Executor {

    private SSHMode mode;

    public SSHExecutor(String host, List<String> args, boolean asSU, CountDownLatch sharedCounter) {
        super(host, args, asSU, sharedCounter);
        this.mode = SSHMode.EXEC;
    }

    public SSHExecutor(String host, List<String> args, boolean asSU, CountDownLatch sharedCounter, SSHMode mode) {
        super(host, args, asSU, sharedCounter);
        this.mode = mode;
    }

    @Override
    public int execute(boolean logMessages) throws IOException, InterruptedException, JSchException {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        BufferedReader outReader;
        int ret = -1;
        Session session = null;
        Channel channel = null;
        try {
            String cmdLine = String.join(" ", arguments);
            JSch jSch = new JSch();
            //jSch.setKnownHosts("D:\\known_hosts");
            session = jSch.getSession(this.user, this.host, 22);
            session.setUserInfo(new UserInfo(this.password));
            session.setPassword(password.getBytes());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = session.openChannel(this.mode.toString());
            if (asSuperUser) {
                int idx = 0;
                while (idx < arguments.size()) {
                    if (SHELL_COMMAND_SEPARATOR.equals(arguments.get(idx))) {
                        arguments.add(idx + 1, "sudo");
                        arguments.add(idx + 2, "-S");
                        arguments.add(idx + 3, "-p");
                        arguments.add(idx + 4, "''");
                    }
                    idx++;
                }
                cmdLine = "sudo -S -p '' " + String.join(" ", arguments);
            }
            logger.info("[[" + host + "]] " + cmdLine);
            ((ChannelExec) channel).setCommand(cmdLine);
            channel.setInputStream(null);
            ((ChannelExec) channel).setPty(asSuperUser);
            ((ChannelExec) channel).setErrStream(new ByteArrayOutputStream() {
                @Override
                public synchronized void write(byte[] b, int off, int len) {
                    String message = new String(b, off, len).replaceAll("\n", "");
                    if (message.length() > 0) {
                        logger.info("[" + host + "] " + message);
                    }
                }
            });
            InputStream inputStream = channel.getInputStream();
            channel.connect();
            if (asSuperUser) {
                OutputStream outputStream = channel.getOutputStream();
                outputStream.write((this.password + "\n").getBytes());
                outputStream.flush();
            }
            outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while (!isStopped()) {
                while (!isStopped && (line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                        if (this.outputConsumer != null && !this.password.equals(line)) {
                            this.outputConsumer.consume(line);
                        }
                        if (logMessages && !this.password.equals(line)) {
                            this.logger.info(line);
                        }
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0)
                        continue;
                    stop();
                } else {
                    Thread.yield();
                }
            }
            ret = channel.getExitStatus();
        } catch (IOException | JSchException e) {
            logger.severe(String.format("[[%s]] failed: %s", host, e.getMessage()));
            wasCancelled = true;
            throw e;
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
        return ret;
    }

    private class UserInfo implements com.jcraft.jsch.UserInfo {

        private String pwd;

        UserInfo(String pass) {
            this.pwd = pass;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return pwd;
        }

        @Override
        public boolean promptPassword(String s) {
            return false;
        }

        @Override
        public boolean promptPassphrase(String s) {
            return false;
        }

        @Override
        public boolean promptYesNo(String s) {
            return false;
        }

        @Override
        public void showMessage(String s) {

        }
    }
}
