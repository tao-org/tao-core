package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.eodata.EOData;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface DataSource<R extends EOData, Q extends DataQuery<R>> {
    long getTimeout();

    void setTimeout(long value);

    String getConnectionString();

    void setConnectionString(String connectionString);

    void setCredentials(String username, String password);

    UsernamePasswordCredentials getCredentials();

    /**
     * Tests that the datasource source is reachable.
     * Must return <code>true</code> if the source is reachable, <code>false</code> otherwise.
     *
     */
    boolean ping();

    /**
     * Closes the datasource source connection.
     */
    void close();

    Map<String, Map<String, ParameterDescriptor>> getSupportedParameters();

    Q createQuery();

    Q createQuery(String type);
}
