package ro.cs.tao.topology;

import ro.cs.tao.utils.executors.ExecutionMode;
import ro.cs.tao.utils.executors.ExecutorType;
import ro.cs.tao.utils.executors.SSHMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cosmin on 8/8/2017.
 */
public class ToolInstallStep {
    private int orderId;
    private String name = "";
    private ExecutorType invocationType = ExecutorType.PROCESS;
    private SSHMode sshMode = SSHMode.EXEC;
    private ExecutionMode executionModeMode = ExecutionMode.USER;
    private String invocationCommand;
    private List<String> executionMessages = new ArrayList<>();
    private String hostName;
    private String user;
    private String pass;
    private boolean ignoreErr = false;
    private int executionTimeout = 60000;

    public void setName(String stepName) {
        this.name = stepName;
    }

    public String getName() {
        return name;
    }

    public void setInvocationCommand(String invocationCommand) {
        this.invocationCommand = invocationCommand;
    }

    public void setInvocationType(ExecutorType invocationType) {
        this.invocationType = invocationType;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setSshMode(SSHMode sshMode) {
        this.sshMode = sshMode;
    }

    public SSHMode getSshMode() {
        return sshMode;
    }

    public String getInvocationCommand() {
        return invocationCommand;
    }

    public ExecutorType getInvocationType() {
        return invocationType;
    }

    public void setExecutionModeMode(ExecutionMode executionModeMode) {
        this.executionModeMode = executionModeMode;
    }

    public ExecutionMode getExecutionModeMode() {
        return executionModeMode;
    }

    public void addExecutionMessage(String message) {
        this.executionMessages.add(message);
    }

    public void setExecutionMessages(List<String> executionMessages) {
        if(executionMessages == null) {
            this.executionMessages = new ArrayList<>();
        } else {
            this.executionMessages = executionMessages;
        }
    }

    public List<String> getExecutionMessages() {
        return executionMessages;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getPass() {
        return pass;
    }

    public void setIgnoreErr(boolean ignoreErr) {
        this.ignoreErr = ignoreErr;
    }

    boolean getIgnoreErr() {
        return this.ignoreErr;
    }

    public void setExecutionTimeout(int executionTimeout) {
        this.executionTimeout = executionTimeout;
    }

    public int getExecutionTimeout() {
        return executionTimeout;
    }
}
