package ro.cs.tao.execution.model;

import org.apache.http.auth.Credentials;

import java.util.Objects;

/**
 * Encapsulates connection information for external task callback
 *
 * @author  Cosmin Cara
 * @since   1.1.0
 */
public class ExternalTaskListenerDescriptor {
    private final String hostName;
    private final int port;
    private final String endpoint;
    private final Credentials credentials;

    /**
     * Constructs a descriptor given a host, a port and the connection credentials
     * @param hostName  The remote listener host
     * @param port      The remote port
     * @param credentials   The credentials for connecting to the remote listener (or <code>null</code> if none).
     */
    public ExternalTaskListenerDescriptor(String hostName, int port, Credentials credentials) {
        this(hostName, port, null, credentials);
    }

    /**
     * Constructs a descriptor given a host, a port and the connection credentials
     * @param hostName  The remote listener host
     * @param port      The remote port
     * @param endpoint  The additional URL part of the remote REST endpoint
     * @param credentials   The credentials for connecting to the remote listener (or <code>null</code> if none).
     */
    public ExternalTaskListenerDescriptor(String hostName, int port, String endpoint, Credentials credentials) {
        this.hostName = hostName;
        this.port = port;
        this.endpoint = endpoint;
        this.credentials = credentials;
    }

    /**
     * Returns the host of the remote listener
     */
    public String getHostName() { return hostName; }

    /**
     * Returns the port of the remote listener
     */
    public int getPort() { return port; }

    /**
     * Returns the additional part of the URL of the remote listener. If the remote listener is a plain socket,
     * this will return <code>null</code>.
     */
    public String getEndpoint() { return endpoint; }

    /**
     * Returns the credentials used to connect to the remote listener.
     */
    public Credentials getCredentials() { return credentials; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExternalTaskListenerDescriptor that = (ExternalTaskListenerDescriptor) o;
        return port == that.port &&
                hostName.equals(that.hostName) &&
                Objects.equals(endpoint, that.endpoint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hostName, port, endpoint);
    }
}
