package ro.cs.tao.execution.simple;

import java.util.List;

/**
 * Structure holding information for a command to be executed
 *
 * @author Cosmin Cara
 */
public final class ExecutionUnit {
    private final ExecutorType type;
    private final String host;
    private final String user;
    private final String password;
    private final List<String> arguments;
    private final boolean asSuperUser;
    private final SSHMode sshMode;

    public ExecutionUnit(ExecutorType type, String host, String user, String password, List<String> arguments, boolean asSuperUser, SSHMode sshMode) {
        this.type = type;
        this.host = host;
        this.user = user;
        this.password = password;
        this.arguments = arguments;
        this.asSuperUser = asSuperUser;
        this.sshMode = sshMode;
    }

    public ExecutorType getType() { return type; }

    public String getHost() { return host; }

    public List<String> getArguments() { return arguments; }

    public boolean asSuperUser() { return asSuperUser; }

    public SSHMode getSshMode() { return sshMode; }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
