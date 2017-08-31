package ro.cs.tao.utils.executors;

import com.jcraft.jsch.*;

import java.io.*;
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
    public int execute(boolean logMessages) throws IOException, InterruptedException, JSchException, SftpException {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        BufferedReader outReader;
        int ret = -1;
        Session session = null;
        Channel channel = null;
        try {
            JSch jSch = new JSch();
            //jSch.setKnownHosts("D:\\known_hosts");
            session = jSch.getSession(this.user, this.host, 22);
            session.setUserInfo(new UserInfo(this.password));
            session.setPassword(password.getBytes());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            channel = session.openChannel(this.mode.toString());
            switch(this.mode) {
                case EXEC:
                    ret = executeSshCmd(channel, logMessages);
                    break;
                case SFTP:
                    ret = executeSftpCmd(channel, logMessages);
                    break;
                default:
                    // TODO:
                    ret = executeSshCmd(channel, logMessages);
                    break;
            }
        } catch (IOException | JSchException | SftpException e) {
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

    private int executeSshCmd(Channel channel, boolean logMessages) throws JSchException, IOException {
        String cmdLine = String.join(" ", arguments);
        if (asSuperUser) {
            int idx = 0;
            String curArg;
            while (idx < arguments.size()) {
                curArg = arguments.get(idx);
                if (SHELL_COMMAND_SEPARATOR.equals(curArg) || SHELL_COMMAND_SEPARATOR_AMP.equals(curArg) ||
                        SHELL_COMMAND_SEPARATOR_BAR.equals(curArg)) {
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
        BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
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
        return channel.getExitStatus();
    }

    private int executeSftpCmd(Channel channel, boolean logMessages) throws JSchException, IOException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) channel;
        if(arguments.size() < 2) {
            throw new IllegalArgumentException("Invalid number of arguments. It should be at least file and target dir");
        }
        //channel.setInputStream(null);
        //channelSftp.setPty(asSuperUser);
        channelSftp.setOutputStream(new ByteArrayOutputStream() {
            @Override
            public synchronized void write(byte[] b, int off, int len) {
                String message = new String(b, off, len).replaceAll("\n", "");
                if (message.length() > 0) {
                    logger.info("[" + host + "] " + message);
                }
            }
        });

        channel.connect();

        String fileToTransfer = arguments.get(0);
        String workingDir = arguments.get(1);
        channelSftp.cd(workingDir);
        if(new File(fileToTransfer).isDirectory()) {
            recursiveFolderUpload(channelSftp, fileToTransfer, workingDir);
        } else {
            File f = new File(fileToTransfer);
            this.logger.info("Uploading file " + f.getName());
            channelSftp.put(new FileInputStream(f), f.getName(), ChannelSftp.OVERWRITE);
        }
        return 0;
    }

    /**
     * This method is called recursively to Upload the local folder content to
     * SFTP server
     *
     * @param sourcePath
     * @param destinationPath
     * @throws SftpException
     * @throws FileNotFoundException
     */
    private void recursiveFolderUpload(ChannelSftp channelSftp, String sourcePath, String destinationPath) throws SftpException, FileNotFoundException {

        File sourceFile = new File(sourcePath);
        if (sourceFile.isFile()) {

            // copy if it is a file
            channelSftp.cd(destinationPath);
            if (!sourceFile.getName().startsWith("."))
                this.logger.info("Uploading file " + sourceFile);
                channelSftp.put(new FileInputStream(sourceFile), sourceFile.getName(), ChannelSftp.OVERWRITE);

        } else {

            //System.out.println("inside else " + sourceFile.getName());
            File[] files = sourceFile.listFiles();

            if (files != null && !sourceFile.getName().startsWith(".")) {

                channelSftp.cd(destinationPath);
                SftpATTRS attrs = null;

                // check if the directory is already existing
                try {
                    attrs = channelSftp.stat(destinationPath + "/" + sourceFile.getName());
                } catch (Exception e) {
                    this.logger.info(destinationPath + "/" + sourceFile.getName() + " not found");
                }

                // else create a directory
                if (attrs != null) {
                    this.logger.info("Directory exists IsDir=" + attrs.isDir());
                } else {
                    this.logger.info("Creating dir " + sourceFile.getName());
                    channelSftp.mkdir(sourceFile.getName());
                }

                for (File f: files) {
                    recursiveFolderUpload(channelSftp, f.getAbsolutePath(), destinationPath + "/" + sourceFile.getName());
                }
            }
        }
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
