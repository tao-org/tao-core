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

import com.jcraft.jsch.*;

import java.io.*;
import java.util.List;

/**
 * Executor class based on JSch. It uses SSH2 for remote host connection and command invocation.
 *
 * @author Cosmin Cara
 */
public class SSHExecutor extends Executor<Channel> {

    private SSHMode mode;
    private int port;

    public SSHExecutor(String host, List<String> args, boolean asSU) {
        super(host, args, asSU);
        this.mode = SSHMode.EXEC;
        this.port = 22;
    }

    public SSHExecutor(String host, int port, List<String> args, boolean asSU) {
        super(host, args, asSU);
        this.mode = SSHMode.EXEC;
        this.port = port;
    }

    public SSHExecutor(String host, List<String> args, boolean asSU, SSHMode mode) {
        super(host, args, asSU);
        this.mode = mode;
        this.port = 22;
    }

    public SSHExecutor(String host, int port, List<String> args, boolean asSU, SSHMode mode) {
        super(host, args, asSU);
        this.mode = mode;
        this.port = port;
    }

    @Override
    public boolean canConnect() {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        boolean ret = false;
        Session session = null;
        try {
            JSch jSch = new JSch();
            //jSch.setKnownHosts("D:\\known_hosts");
            session = jSch.getSession(this.user, this.host, this.port);
            session.setUserInfo(new UserInfo(this.password));
            session.setPassword(password.getBytes());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(1000);
            this.channel = session.openChannel(this.mode.toString());
            ret = true;
        } catch (Exception e) {
            logger.severe(String.format("[[%s]] failed: %s", host, e.getMessage()));
        } finally {
            resetChannel();
            if (session != null) {
                session.disconnect();
            }
        }
        return ret;
    }

    @Override
    public int execute(boolean logMessages) throws IOException, JSchException, SftpException {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        int ret = -1;
        Session session = null;
        try {
            JSch jSch = new JSch();
            //jSch.setKnownHosts("D:\\known_hosts");
            session = jSch.getSession(this.user, this.host, this.port);
            session.setUserInfo(new UserInfo(this.password));
            session.setPassword(password.getBytes());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            this.channel = session.openChannel(this.mode.toString());
            switch(this.mode) {
                case EXEC:
                    ret = executeSshCmd(logMessages);
                    break;
                case SFTP:
                    ret = executeSftpCmd(logMessages);
                    break;
                default:
                    // TODO:
                    ret = executeSshCmd(logMessages);
                    break;
            }
        } catch (IOException | JSchException | SftpException e) {
            logger.severe(String.format("[[%s]] failed: %s", host, e.getMessage()));
            isStopped = true;
            throw e;
        } finally {
            resetChannel();
            if (session != null) {
                session.disconnect();
            }
        }
        return ret;
    }

    @Override
    public void suspend() {
        if (!this.channel.isClosed() && this.channel.isConnected()) {
            try {
                this.channel.sendSignal("19");
            } catch (Exception e) {
                this.logger.warning(e.getMessage());
            }
        }
        super.suspend();
    }

    @Override
    public void resume() {
        if (!this.channel.isClosed() && this.channel.isConnected()) {
            try {
                this.channel.sendSignal("18");
            } catch (Exception e) {
                this.logger.warning(e.getMessage());
            }
        }
        super.resume();
    }

    @Override
    public void stop() {
        if (!this.channel.isClosed() && this.channel.isConnected()) {
            try {
                this.channel.sendSignal("2");
            } catch (Exception e) {
                this.logger.warning(e.getMessage());
            }
        }
        super.stop();
    }

    private int executeSshCmd(boolean logMessages) throws JSchException, IOException {
        String cmdLine = String.join(" ", arguments);
        if (asSuperUser) {
            insertSudoParams();
            cmdLine = "sudo -S -p '' " + String.join(" ", arguments);
        }
        logger.fine("[[" + host + "]] " + cmdLine);
        ((ChannelExec) this.channel).setCommand(cmdLine);
        this.channel.setInputStream(null);
        ((ChannelExec) this.channel).setPty(asSuperUser);
        ((ChannelExec) this.channel).setErrStream(new ByteArrayOutputStream() {
            @Override
            public synchronized void write(byte[] b, int off, int len) {
                String message = new String(b, off, len).replaceAll("\n", "");
                if (message.length() > 0) {
                    logger.fine("[" + host + "] " + message);
                }
            }
        });
        InputStream inputStream = this.channel.getInputStream();
        this.channel.connect();
        if (asSuperUser) {
            writeSudoPassword();
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
                        this.logger.fine(line);
                    }
                }
            }
            if (this.channel.isClosed()) {
                if (inputStream.available() > 0)
                    continue;
                stop();
            } else {
                Thread.yield();
            }
        }
        return this.channel.getExitStatus();
    }

    private int executeSftpCmd(boolean logMessages) throws JSchException, IOException, SftpException {
        ChannelSftp channelSftp = (ChannelSftp) this.channel;
        if(this.arguments.size() < 2) {
            throw new IllegalArgumentException("Invalid number of arguments. It should be at least file and target dir");
        }
        //channel.setInputStream(null);
        //channelSftp.setPty(asSuperUser);
        channelSftp.setOutputStream(new ByteArrayOutputStream() {
            @Override
            public synchronized void write(byte[] b, int off, int len) {
                String message = new String(b, off, len).replaceAll("\n", "");
                if (message.length() > 0) {
                    SSHExecutor.this.logger.fine("[" + SSHExecutor.this.host + "] " + message);
                }
            }
        });

        channel.connect();

        String fileToTransfer = this.arguments.get(0);
        String workingDir = this.arguments.get(1);
        channelSftp.cd(workingDir);
        if(new File(fileToTransfer).isDirectory()) {
            recursiveFolderUpload(channelSftp, fileToTransfer, workingDir);
        } else {
            File f = new File(fileToTransfer);
            this.logger.fine("Uploading file " + f.getName());
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
                this.logger.finest("Uploading file " + sourceFile);
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
                    this.logger.fine(destinationPath + "/" + sourceFile.getName() + " not found");
                }

                // else create a directory
                if (attrs != null) {
                    this.logger.finest("Directory exists IsDir=" + attrs.isDir());
                } else {
                    this.logger.fine("Creating dir " + sourceFile.getName());
                    channelSftp.mkdir(sourceFile.getName());
                }

                for (File f: files) {
                    recursiveFolderUpload(channelSftp, f.getAbsolutePath(), destinationPath + "/" + sourceFile.getName());
                }
            }
        }
    }

    private void resetChannel() {
        if (this.channel != null) {
            if (this.channel.isConnected()) {
                this.channel.disconnect();
            }
            this.channel = null;
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
