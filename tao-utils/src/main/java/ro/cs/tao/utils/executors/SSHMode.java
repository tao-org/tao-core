package ro.cs.tao.utils.executors;

/**
 * Possible SSH connection modes
 *
 * @author Cosmin Cara
 */
public enum SSHMode {
    SESSION("session"),
    SHELL("shell"),
    EXEC("exec"),
    X11("x11"),
    AGENT_FORWARDING("auth-agent@openssh.com"),
    DIRECT_TCPIP("direct-tcpip"),
    FORWARDED_TCPIP("forwarded-tcpip"),
    SFTP("sftp"),
    SUBSYSTEM("subsystem");

    private final String text;

    private SSHMode(final String text) { this.text = text; }

    @Override
    public String toString() { return this.text; }

    public boolean equals(SSHMode other) {
        return other != null && other.toString().equals(this.text);
    }
}
