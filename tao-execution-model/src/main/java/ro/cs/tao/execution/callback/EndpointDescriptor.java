package ro.cs.tao.execution.callback;

import org.apache.http.auth.Credentials;
import ro.cs.tao.utils.HttpMethod;

import java.util.Objects;

/**
 * Encapsulates connection information for external task callback
 *
 * @author  Cosmin Cara
 * @since   1.1.0
 */
public class EndpointDescriptor {
    private String protocol;
    private String hostName;
    private int port;
    private String endpoint;
    private HttpMethod method;
    private Credentials credentials;

    public EndpointDescriptor() {
    }

    /**
     * Constructs a descriptor given a host, a port and the connection credentials
     * @param protocol  The protocol used to connect. Can be one of tcp, http or https
     * @param hostName  The remote host
     * @param port      The remote port
     * @param credentials   The credentials for connecting to the endpoint (or <code>null</code> if none).
     */
    public EndpointDescriptor(String protocol, String hostName, int port, Credentials credentials) {
        this(protocol, hostName, port, null, credentials);
    }

    /**
     * Constructs a descriptor given a host, a port and the connection credentials
     * @param protocol  The protocol used to connect. Can be one of http or https
     * @param hostName  The remote listener host
     * @param port      The remote port
     * @param endpoint  The additional URL part of the remote REST endpoint
     * @param credentials   The credentials for connecting to the endpoint (or <code>null</code> if none).
     */
    public EndpointDescriptor(String protocol, String hostName, int port, String endpoint, Credentials credentials) {
        this(protocol, hostName, port, endpoint, null, credentials);
    }

    public EndpointDescriptor(String protocol, String hostName, int port, String endpoint, String method, Credentials credentials) {
        this.protocol = protocol;
        this.hostName = hostName;
        this.port = port;
        this.endpoint = endpoint;
        this.credentials = credentials;
        if (protocol != null && protocol.toLowerCase().startsWith("http")) {
            if (method == null) {
                throw new IllegalArgumentException(("[method] null"));
            } else {
                final String val = method.toUpperCase();
                if (!val.equals(HttpMethod.GET.name()) && !val.equals(HttpMethod.POST.name())) {
                    throw new IllegalArgumentException(("[method] Only GET or POST supported for HTTP/S"));
                } else {
                    this.method = HttpMethod.valueOf(val);
                }
            }
        } else {
            this.method = null;
        }
    }

    /**
     * Returns the protocol that will be used to connect to the endpoint
     */
    public String getProtocol() { return protocol; }

    /**
     * Returns the host of the endpoint
     */
    public String getHostName() { return hostName; }

    /**
     * Returns the port of the endpoint
     */
    public int getPort() { return port; }

    /**
     * Returns the additional part of the URL of the endpoint. If the endpoint is a plain socket,
     * this will return <code>null</code>.
     */
    public String getEndpoint() { return endpoint; }

    public HttpMethod getMethod() { return method; }

    /**
     * Returns the credentials used to connect to the endpoint
     */
    public Credentials getCredentials() { return credentials; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndpointDescriptor that = (EndpointDescriptor) o;
        return port == that.port &&
                protocol.equals(that.protocol) &&
                hostName.equals(that.hostName) &&
                Objects.equals(endpoint, that.endpoint) &&
                Objects.equals(credentials, that.credentials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, hostName, port, endpoint, credentials);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        if (protocol != null) {
            builder.append(protocol).append("//");
        }
        if (credentials != null && credentials.getUserPrincipal() != null && credentials.getPassword() != null) {
            builder.append(credentials.getUserPrincipal().getName())
                    .append(":").append(credentials.getPassword())
                    .append("@");
        }
        builder.append(hostName).append(":").append(port);
        if (endpoint != null) {
            if (!endpoint.startsWith("/")) {
                builder.append("/");
            }
            builder.append(endpoint);
        }
        return builder.toString();
    }

    public String toAnonString() {
        final StringBuilder builder = new StringBuilder();
        if (protocol != null) {
            builder.append(protocol).append("//");
        }
        builder.append(hostName).append(":").append(port);
        if (endpoint != null) {
            if (!endpoint.startsWith("/")) {
                builder.append("/");
            }
            builder.append(endpoint);
        }
        return builder.toString();
    }
}
