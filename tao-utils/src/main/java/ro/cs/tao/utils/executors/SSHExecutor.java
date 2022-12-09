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
import ro.cs.tao.utils.ExceptionUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Executor class based on JSch. It uses SSH2 for remote host connection and command invocation.
 *
 * @author Cosmin Cara
 */
public class SSHExecutor extends Executor<Channel> {

    private final SSHMode mode;
    private final int port;
    private Session singleSession;
    private boolean keepAlive;
    private Timer keepAliveTimer;
    
    /** Perform asynchronous execution. Default: false. */
    private final boolean asyncExecution;

    /** Screen identifier for asynchronous execution. */
    private volatile String asyncScreenID;
    
    /** The name of the file where the output is saved in case of asynchronous execution. */
    private final String asyncFileName;

    /** The result of the Asynchronous call. Null when the result was not obtained.*/
    private Integer asyncResult = null;
    
    /** The asynchronous call status. Null when the call was not yet initialized.  */
    private Boolean asyncCallFinished = null;
    
    /** True if the asynchronous command received is a docker command. */
    private boolean dockerAsyncCommand = false;
    
    /** The docker identifier if the docker command is executed asynchronously. */
    private String dockerAsyncID;
    
    /** Regex pattern to extract the screen ID after the screen session is disconnected. */
    private static final Pattern SCREEN_ID_PATTERN = Pattern.compile("^\\[.+\\s(\\d+\\.\\S+\\.\\S+)\\]$");

    /** Regex pattern to extract the execution result code after execution within a screen session. */
    private static final Pattern EXECUTION_RESULT_PATTERN = Pattern.compile("^\\*{3}\\={3}\\*{3}\\s*(\\-?[0-9]+)\\s*\\*{3}\\={3}\\*{3}$");

    /** Regex pattern to extract the docker pattern. */
    private static final Pattern DOCKER_ID = Pattern.compile("^([a-z0-9]+)\\:.*$");
    
    /** The format for the file generated during asynchronous call. */
    private static final String ASYNC_FILE_NAME_FORMAT = "~/%s.out";
    
    public SSHExecutor(String host, List<String> args, boolean asSU) {
    	this(host, 22, args, asSU, SSHMode.EXEC, false, Long.toString(System.currentTimeMillis()));
    }

    public SSHExecutor(String host, int port, List<String> args, boolean asSU) {
    	this(host, port, args, asSU, SSHMode.EXEC, false, Long.toString(System.currentTimeMillis()));
    }

    public SSHExecutor(String host, List<String> args, boolean asSU, SSHMode mode) {
    	this(host, 22, args, asSU, mode, false, Long.toString(System.currentTimeMillis()));
    }

    public SSHExecutor(String host, List<String> args, boolean asSU, SSHMode mode, boolean asyncExecution, String taskName) {
    	this(host, 22, args, asSU, mode, asyncExecution, taskName);
    }

    public SSHExecutor(String host, int port, List<String> args, boolean asSU, SSHMode mode) {
    	this(host, port, args, asSU, mode, false, Long.toString(System.currentTimeMillis()));
    }

    public SSHExecutor(String host, int port, List<String> args, boolean asSU, SSHMode mode, boolean asyncExecution) {
    	this(host, port, args, asSU, mode, asyncExecution, Long.toString(System.currentTimeMillis()));
    }

    public SSHExecutor(String host, int port, List<String> args, boolean asSU, SSHMode mode, boolean asyncExecution, String taskName) {
        super(host, args, asSU);
        this.mode = mode;
        this.port = port;
        this.asyncExecution = asyncExecution;
        this.asyncFileName = String.format(ASYNC_FILE_NAME_FORMAT, taskName);
    }
    
    public void setKeepAlive(boolean value) {
        this.keepAlive = value;
        if (this.keepAlive) {
            this.keepAliveTimer = new Timer();
            this.keepAliveTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        getSession();
                    } catch (Exception e) {
                        logger.severe(ExceptionUtils.getStackTrace(logger, e));
                    }
                }
            }, 60000, 60000);
        } else if (this.keepAliveTimer != null) {
            this.keepAliveTimer.cancel();
            this.keepAliveTimer.purge();
            this.keepAliveTimer = null;
        }
    }

    private Session getSession() throws Exception {
        if (keepAlive) {
            try {
                if (singleSession == null) {
                    singleSession = createSession();
                } else if (!singleSession.isConnected()) {
                    singleSession.connect();
                }
                ChannelExec testChannel = (ChannelExec) singleSession.openChannel(SSHMode.EXEC.toString());
                testChannel.setCommand("true");
                testChannel.connect();
                testChannel.disconnect();
            } catch (Throwable t) {
                logger.fine("Session terminated. Create a new one.");
                singleSession = createSession();
            }
            return singleSession;
        } else {
            return createSession();
        }
    }

    private Session createSession() throws JSchException {
        Session session = openSession(3000);
        //this.logger.finest(String.format("Session %d opened", session.hashCode()));
        return session;
    }

    private Session openSession(int connectTimeoutMilliseconds) throws JSchException {
        JSch jSch = new JSch();
        Session session = jSch.getSession(this.user, this.host, this.port);
        session.setUserInfo(new UserInfo(this.password));
        if (this.certificate != null) {
            jSch.addIdentity(this.certificate);
        } else {
            session.setPassword(password.getBytes());
        }
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(connectTimeoutMilliseconds);
        return session;
    }

    @Override
    public boolean canConnect() {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        boolean ret = false;
        Session session = null;
        try {
            session = getSession();
            final Channel channel = session.openChannel(this.mode.toString());
            channel.connect();
            channel.disconnect();
            ret = true;
        } catch (Exception e) {
            logger.severe(String.format("[%s] failed: %s", host, e.getMessage()));
        } finally {
            //resetChannel();
            if (session != null && !keepAlive) {
                session.disconnect();
                //this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }
        return ret;
    }

    public boolean canConnect(int connectTimeoutMilliseconds) throws JSchException {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        boolean result;
        Session session = openSession(connectTimeoutMilliseconds);
        try {
            Channel channel = session.openChannel(this.mode.toString());
            channel.connect(connectTimeoutMilliseconds);
            channel.disconnect();
            result = true;
        } catch (Exception e) {
            result = false;
        } finally {
            session.disconnect();
        }
        return result;
    }

    @Override
    public int execute(boolean logMessages) throws IOException, JSchException, SftpException {
        if (!SSHMode.EXEC.equals(this.mode) && asSuperUser) {
            throw new UnsupportedOperationException("Mode not permitted");
        }
        int ret;
        //Session session = null;
        try {
            //session = getSession();
            //this.channel = session.openChannel(this.mode.toString());
            switch(this.mode) {
                case EXEC:
                	if(this.asyncExecution) {
                		ret = executeAsyncSSH(logMessages);
                		if (dockerAsyncCommand) {
                			updateDockerId();
                		}
                	} else {
                        ret = executeSshCmd(logMessages);
                	}
                    break;
                case SFTP:
                    ret = executeSftpCmd(logMessages);
                    break;
                default:
                    // TODO:
                    ret = executeSshCmd(logMessages);
                    break;
            }
        } catch (Exception e) {
            logger.severe(ExceptionUtils.getStackTrace(logger, e));
            isStopped = !keepAlive;
            throw new IOException(e);
        }/* finally {
            resetChannel();
            if (session != null && !keepAlive) {
                session.disconnect();
            }
        }*/
        return ret;
    }

    @Override
    public void suspend() {
        if (!this.asyncExecution) {
            if (this.channel != null && !this.channel.isClosed() && this.channel.isConnected()) {
                try {
                    this.channel.sendSignal("19");
                } catch (Exception e) {
                    this.logger.warning(e.getMessage());
                }
            }
        }
        super.suspend();
    }

    @Override
    public void resume() {
        if (!this.asyncExecution) {
            if (this.channel != null && !this.channel.isClosed() && this.channel.isConnected()) {
                try {
                    this.channel.sendSignal("18");
                } catch (Exception e) {
                    this.logger.warning(e.getMessage());
                }
            }
        }
        super.resume();
    }

    @Override
    public void stop() {
        if (!asyncExecution) {
            if (this.channel != null && !this.channel.isClosed() && this.channel.isConnected()) {
                try {
                    this.channel.sendSignal("2");
                } catch (Exception e) {
                    this.logger.warning(e.getMessage());
                }
            }
        } else {
    		stopAsync(asyncScreenID);
        	try {
        		// Update the asyncSSH result, for future calls
    			getAsyncSSHResult();
    		} catch (Exception e) {
    			this.logger.warning(e.getMessage());
    			this.asyncResult = Integer.MIN_VALUE;
    		}
    	}
        super.stop();
    }

    @Override
	public int getReturnCode() {
    	if (!asyncExecution) {
    		return super.getReturnCode();
    	}

    	try {
			return getAsyncSSHResult();
		} catch (Exception e) {
			this.logger.warning(e.getMessage());
			return Integer.MIN_VALUE;
		}
	}

	@Override
	public boolean isRunning() {
    	if (!asyncExecution) {
    		return super.isRunning();
    	}
    	
    	try {
			return checkSession();
		} catch (Exception e) {
			this.logger.warning(e.getMessage());
			// assume still running
			return true;
		}
	}
	
	@Override
	public boolean hasCompleted() {
		// for synchronous execution pass the control to the parent
    	if (!asyncExecution) {
    		return super.hasCompleted();
    	}
    	
    	// check if the asynchronous call was even started and return its status. 
    	if (this.asyncCallFinished != null) {
    		return this.asyncCallFinished;
    	}
    	
    	// the asynchronous call was not yet started, thus not completed
    	return false;
	}

	public Channel open(SSHMode mode) throws IOException {
        Session session = null;
        Channel channel = null;
        try {
            session = getSession();
            channel = session.openChannel(mode.toString());
            channel.setOutputStream(new ByteArrayOutputStream() {
                @Override
                public synchronized void write(byte[] b, int off, int len) {
                    String message = new String(b, off, len).replaceAll("\n", "");
                    if (message.length() > 0) {
                        SSHExecutor.this.logger.fine("[" + SSHExecutor.this.host + "] " + message);
                    }
                }
            });
            channel.connect();
            //this.logger.finest(String.format("Channel %d opened", channel.getId()));
            return channel;
        } catch (Exception e) {
            stop(channel);
            if (session != null && !keepAlive) {
                session.disconnect();
                //this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
            throw new IOException(e);
        }
    }

    public void close() {
        if (singleSession != null) {
            singleSession.disconnect();
            //this.logger.finest(String.format("Session %d disconnected", singleSession.hashCode()));
            singleSession = null;
        }
    }

    private void stop(Channel channel) {
        if (channel != null && !channel.isClosed() && channel.isConnected()) {
            try {
                //channel.sendSignal("2");
                channel.disconnect();
                //this.logger.finest(String.format("Channel %d disconnected", channel.getId()));
            } catch (Exception e) {
                this.logger.warning(e.getMessage());
            }
        }
        super.stop();
        if (!asyncExecution) {
            isStopped = !keepAlive;
        }
    }

    /**
     * Start an asynchronous call.
     * 
     * @param logMessages use the logger for messages
     * @return the execution result of the screen command
     * @throws Exception
     */
    public int executeAsyncSSH(final boolean logMessages) throws Exception {

    	// Initialise the sync result and status variable
    	this.asyncResult = null;
    	this.asyncCallFinished = null;
    	
    	// set the name of the output file
        if (asSuperUser) {
            insertSudoParams();
        }
        
        // check if the command is a docker command
        final String execCommand = String.join(" ", arguments);
        if (execCommand.contains("docker ") && execCommand.contains(" run ")) {
        	this.dockerAsyncCommand = true;
        }
        // build the command line that will be executed within the screen session
        // the pattern of the line containing the return code has the format :
        // ***===***RESULT_CODE***===***
        String cmdLine = "( " + execCommand + "; echo '***===***'$?'***===***'; ) > " +this.asyncFileName+ " 2>&1\n";
        if (logMessages) {
            logger.fine("[" + host + "] " + cmdLine);
        }
        final Session session = getSession();
        ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
        try {
        	// Configure channel
        	final String command = "screen";// -L -Logfile " + this.asyncFileName;
        	final InputStream inputStream = configureAndConnectChannel(channel, command, logMessages, true);

        	// send a new line character to exit the screen's message
            writeCommand(channel, "\n".getBytes());

            // execute the command
            writeCommand(channel, cmdLine.getBytes());

            // write the password if elevation is required
            if (asSuperUser) {
                if (this.certificate != null) {
                    this.password = "";
                }
                writeSudoPassword(channel, this.password);
            }
            // Send the screen exit
            writeCommand(channel, "exit\n".getBytes());
            
            // exit screen session with CTRL+a, d
            writeCommand(channel, "\01d".getBytes());
            
            BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean ended = false;
            while (!isStopped() && !ended) {
                while (!isStopped && (line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                        /*if (this.outputConsumer != null && (this.password == null || !this.password.equals(line))) {
                            this.outputConsumer.consume(line);
                        }
                        if (logMessages && !this.password.equals(line)) {
                            this.logger.fine(line);
                        }*/

                        // try to extract the screen ID
                        Matcher matcher = SCREEN_ID_PATTERN.matcher(line.trim());
                        if (matcher.matches()) {
                            this.asyncScreenID = matcher.group(1);
                        }
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue;
                    }
                    stop(channel);
                    
                    ended = true;
                } else {
                    Thread.yield();
                }
            }
        } finally {
            if (!keepAlive) {
                session.disconnect();
                //this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }

        // Execution still running
        this.isStopped = false;
        this.asyncCallFinished = false;
        return channel.getExitStatus();
    }
    
    /**
     * Check if a screen session is still running
     * 
     * @return true if the screen session is still running
     * @throws Exception
     */
    public boolean checkSession() throws Exception {
        boolean sessionRunning = false;
        // Check if the asynchronous session was started.
        if (this.asyncCallFinished == null) {
        	return true;
        } else if (this.asyncCallFinished) {
        	return false;
        }
        final Session session = getSession();
        ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
        try {
        	// Configure the channel
        	final String command = "screen -ls " + this.asyncScreenID;
        	final InputStream inputStream = configureAndConnectChannel(channel, command, false, true);

            BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean ended = false;
            while (!ended) {
                while ((line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                    	if (line.contains(this.asyncScreenID)) {
                    		sessionRunning = true;
                    	}
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue;
                    }
                    stop(channel);
                    ended = true;
                } else {
                    Thread.yield();
                }
            }
        } finally {
            if (!keepAlive) {
                session.disconnect();
                logger.finest("Completed check for screen session " + this.asyncScreenID + " on host [" + this.host + "]");
                //this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }
        
        this.isStopped = !sessionRunning;
        this.asyncCallFinished = !sessionRunning;
        return sessionRunning;
    	
    }
    
    /**
     * Extract the result of an asynchronous call.
     * 
     * <p>
     *  This method will return the result of the last executed process in the asynchronous call. 
     * 
     * @return the return code of the process execution
     * @throws Exception
     */
    public int getAsyncSSHResult() throws Exception {
    	
    	// If the async result was already obtained, return the value.
    	if (asyncResult != null) {
    		return asyncResult;
    	}
    	
        int result = Integer.MIN_VALUE;
        final Session session = getSession();
        ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
        try {
        	// Configure the channel
        	final String command = "cat " + this.asyncFileName + " && rm " + this.asyncFileName;
        	final InputStream inputStream = configureAndConnectChannel(channel, command, false, true);

            BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean ended = false;
            
            while (!ended) {
                while ((line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                        final Matcher matcher = EXECUTION_RESULT_PATTERN.matcher(line.trim());
                        if (matcher.matches()) {
                        	// the line contains the result code
                        	result = Integer.parseInt(matcher.group(1));
                        	asyncResult = result;
                        } else {
                        	// the line contains the output of the process
	                        if (this.outputConsumer != null && (this.password == null || !this.password.equals(line))) {
	                            this.outputConsumer.consume(line);
	                        }
                        }
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue;
                    }
                    stop(channel);
                    ended = true;
                } else {
                    Thread.yield();
                }
            }
        } finally {
            if (!keepAlive) {
                session.disconnect();
                this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }

        return result;
    }
    
    /**
     * Update the docker id field
     * 
     * @throws Exception if the operation failed
     */
    public void updateDockerId() throws Exception {
        final Session session = getSession();
        ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
        try {
        	// Configure the channel
        	final String command = "docker ps --format=\"{{.ID}}:{{.Command}}\" --no-trunc | grep -G \\\".*" + this.arguments.get(this.arguments.size() - 1) + "\\\"";
        	final InputStream inputStream = configureAndConnectChannel(channel, command, false, true);

            BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean ended = false;
            
            while (!ended) {
                while ((line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                        final Matcher matcher = DOCKER_ID.matcher(line.trim());
                        if (matcher.matches()) {
                        	// the line contains the result code
                        	this.dockerAsyncID = matcher.group(1);
                        }
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue;
                    }
                    stop(channel);
                    ended = true;
                } else {
                    Thread.yield();
                }
            }
        } finally {
            if (!keepAlive) {
                session.disconnect();
                this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }
    }
    
    /**
     * Stop the asynchronous process
     * 
     * @param screenID the id of the screen where the process is running
     */
    public void stopAsync(String screenID) {
    	try {
        	if (!checkSession()) {
        		return;
        	}
        	
            final Session session = getSession();
            ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
            try {
            	// stop all docker images
            	String command;
            	if (dockerAsyncCommand) {
            		command = "docker kill " + this.dockerAsyncID + " ; " + "screen -X -S \"" + screenID + "\" quit"; 
            	} else {
            		command = "screen -X -S \"" + screenID + "\" quit";
            	}
            	
            	final InputStream inputStream = configureAndConnectChannel(channel, command, false, true);

                // kill the screen session and delete the temporary file that was created 
                //writeCommand(channel, ("screen -X -S \"" + screenID + "\" quit && rm " + this.asyncFileName + "\n").getBytes());

                BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                boolean ended = false;
                
                while (!ended) {
                    while ((line = outReader.readLine()) != null) {
                        if (!"".equals(line.trim())) {
                        	// the line contains the output of the process
	                        if (this.outputConsumer != null && (this.password == null || !this.password.equals(line))) {
	                            this.outputConsumer.consume(line);
	                        }
                        }
                    }
                    if (channel.isClosed()) {
                        if (inputStream.available() > 0) {
                            continue;
                        }
                        stop(channel);
                        ended = true;
                    } else {
                        Thread.yield();
                    }
                }
            } finally {
                if (!keepAlive) {
                    session.disconnect();
                    this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
                }
            }

    	} catch(Exception e) {
    		this.logger.warning(e.getMessage());
    	}
    }
    
    /** Reconnect to an existing session and wait for it to end.
     * @param screenID The ID of the screen session
     * @param logMessages send messages to the log
     * @return the return code of the screen call
     * @throws Exception
     */
    public int reconnectAsyncSSH(String screenID, boolean logMessages) throws Exception {
        
        final Session session = getSession();
        ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
        try {
        	final String command = "screen -r " + screenID; 
        	final InputStream inputStream = configureAndConnectChannel(channel, command, logMessages, true);

            BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean ended = false;
            while (!isStopped() && !ended) {
                while (!isStopped && (line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                        if (this.outputConsumer != null && (this.password == null || !this.password.equals(line))) {
                            this.outputConsumer.consume(line);
                        }
                        if (logMessages && !this.password.equals(line)) {
                            this.logger.fine(line);
                        }
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue;
                    }
                    stop(channel);
                    ended = true;
                } else {
                    Thread.yield();
                }
            }
        } finally {
            if (!keepAlive) {
                session.disconnect();
                this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }

        return channel.getExitStatus();
    }

    /**
     * Write a command to the SSH channel.
     * 
     * @param channel the channel
     * @param command the command.
     * @throws IOException
     */
    private void writeCommand(final Channel channel, final byte[] command) throws IOException{
        if (channel == null) {
            throw new NullPointerException("The channel is null.");
        }

        OutputStream outputStream = channel.getOutputStream();
        outputStream.write(command);
        outputStream.flush();
    }

    /**
     * Configure and connect a channel to a SSH server
     * 
     * @param channel the channel
     * @param command the command to execute after channel connection
     * @param logMessages add messages to log
     * @param usePseudoTerminal configure the channel to emulate a terminal
     * @return the input stream that can be used to send data through the channel
     * @throws Exception
     */
    private InputStream configureAndConnectChannel(final ChannelExec channel, final String command, final boolean logMessages, final boolean usePseudoTerminal) throws Exception {
        channel.setCommand(command);
        channel.setInputStream(null);
        channel.setPty(usePseudoTerminal);
        channel.setErrStream(new ByteArrayOutputStream() {
            @Override
            public synchronized void write(byte[] b, int off, int len) {
                String message = new String(b, off, len).replaceAll("\n", "");
                if (message.length() > 0) {
                    if (outputConsumer != null) {
                        outputConsumer.consume(message);
                    }
                    if (logMessages) {
                        logger.fine("[" + host + "] " + message);
                    }
                }
            }
        });
        InputStream inputStream = channel.getInputStream();
        channel.connect();

        return inputStream;
    }
    
    private int executeSshCmd(boolean logMessages) throws Exception {
        String cmdLine = String.join(" ", arguments);
        if (asSuperUser) {
            insertSudoParams();
            //cmdLine = "sudo -S -p '' " + String.join(" ", arguments);
            cmdLine = String.join(" ", arguments);
        }
        if (logMessages) {
            logger.finest("[" + host + "] " + cmdLine);
        }
        final Session session = getSession();
        ChannelExec channel = (ChannelExec) session.openChannel(SSHMode.EXEC.toString());
        try {
            channel.setCommand(cmdLine);
            channel.setInputStream(null);
            channel.setPty(asSuperUser);
            channel.setErrStream(new ByteArrayOutputStream() {
                @Override
                public synchronized void write(byte[] b, int off, int len) {
                    String message = new String(b, off, len).replaceAll("\n", "");
                    if (message.length() > 0) {
                        if (outputConsumer != null) {
                            outputConsumer.consume(message);
                        }
                        if (logMessages) {
                            logger.fine("[" + host + "] " + message);
                        }
                    }
                }
            });
            InputStream inputStream = channel.getInputStream();
            channel.connect();
            if (asSuperUser) {
                if (this.certificate != null) {
                    this.password = "";
                }
                writeSudoPassword(channel, this.password);
            }
            BufferedReader outReader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            boolean ended = false;
            while (!isStopped() && !ended) {
                while (!isStopped && (line = outReader.readLine()) != null) {
                    if (!"".equals(line.trim())) {
                        if (this.outputConsumer != null && (this.password == null || !this.password.equals(line))) {
                            this.outputConsumer.consume(line);
                        }
                        if (logMessages && !this.password.equals(line)) {
                            this.logger.fine(line);
                        }
                    }
                }
                if (channel.isClosed()) {
                    if (inputStream.available() > 0) {
                        continue;
                    }
                    stop(channel);
                    ended = true;
                } else {
                    Thread.yield();
                }
            }
        } finally {
            if (!keepAlive) {
                session.disconnect();
                this.logger.finest(String.format("Session %d disconnected", session.hashCode()));
            }
        }
        return channel.getExitStatus();
    }

    private static void writeSudoPassword(Channel channel, String password) throws IOException {
        if (channel == null) {
            throw new NullPointerException("The channel is null.");
        }
        OutputStream outputStream = channel.getOutputStream();
        outputStream.write((password + "\n").getBytes());
        outputStream.flush();
    }

    private int executeSftpCmd(boolean logMessages) throws Exception {
        if(this.arguments.size() < 2) {
            throw new IllegalArgumentException("Invalid number of arguments. It should be at least file and target dir");
        }
        ChannelSftp channelSftp = (ChannelSftp) getSession().openChannel(SSHMode.SFTP.toString());
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

        channelSftp.connect();

        String fileToTransfer = this.arguments.get(0);
        String workingDir = this.arguments.get(1);
        channelSftp.cd(workingDir);
        Path file = Paths.get(fileToTransfer);
        if(Files.isDirectory(file)) {
            recursiveFolderUpload(channelSftp, fileToTransfer, workingDir);
        } else {
            this.logger.fine("Uploading file " + file.getFileName());
            try (InputStream stream = Files.newInputStream(file)) {
                channelSftp.put(stream, file.getFileName().toString(), ChannelSftp.OVERWRITE);
            }
        }
        channelSftp.disconnect();
        return channelSftp.getExitStatus();
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
    private void recursiveFolderUpload(ChannelSftp channelSftp, String sourcePath, String destinationPath) throws SftpException, IOException {
        Path file = Paths.get(sourcePath);
        String fileName = file.getFileName().toString();
        if (Files.isRegularFile(file)) {
            // copy if it is a file
            channelSftp.cd(destinationPath);
            if (!file.getFileName().startsWith(".")) {
                this.logger.finest("Uploading file " + file);
            }
            try (InputStream stream = Files.newInputStream(file)) {
                channelSftp.put(stream, fileName, ChannelSftp.OVERWRITE);
            }
        } else {
            try (Stream<Path> files = Files.list(file)) {
                if (files != null && !file.getFileName().startsWith(".")) {
                    channelSftp.cd(destinationPath);
                    SftpATTRS attrs = null;

                    // check if the directory is already existing
                    try {
                        attrs = channelSftp.stat(destinationPath + "/" + fileName);
                    } catch (Exception e) {
                        this.logger.fine(destinationPath + "/" + fileName + " not found");
                    }

                    // else create a directory
                    if (attrs != null) {
                        this.logger.finest("Directory exists IsDir=" + attrs.isDir());
                    } else {
                        this.logger.fine("Creating dir " +fileName);
                        channelSftp.mkdir(fileName);
                    }
                    files.forEach(f -> {
                        try {
                            recursiveFolderUpload(channelSftp, f.toAbsolutePath().toString(), destinationPath + "/" + fileName);
                        } catch (SftpException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            }
        }
    }

    private static class UserInfo implements com.jcraft.jsch.UserInfo {

        private final String pwd;

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
