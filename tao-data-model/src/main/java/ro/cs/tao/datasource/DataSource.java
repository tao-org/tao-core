package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.datasource.param.ParameterDescriptor;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface DataSource<Q extends DataQuery> {
    /**
     * Returns the timeout for this data source connection
     */
    long getTimeout();
    /**
     * Sets the timeout for this data source connection
     * @param value     The timeout value in milliseconds
     */
    void setTimeout(long value);
    /**
     * Returns the connection string for this data source.
     * In the case of a remote data source, it is the base url of the remote endpoint.
     * In the case of a database data source, it is the connection string of the database.
     */
    String getConnectionString();
    /**
     * Sets the connection string for this data source.
     * @param connectionString      The connection string
     */
    void setConnectionString(String connectionString);
    /**
     * Sets the credentials needed to connect to this data source
     * @param username  The user name
     * @param password  The user password
     */
    void setCredentials(String username, String password);
    /**
     * Gets the credentials set for this data source.
     */
    UsernamePasswordCredentials getCredentials();
    /**
     * Tests that the datasource source is reachable.
     * Must return <code>true</code> if the source is reachable, <code>false</code> otherwise.
     */
    boolean ping();
    /**
     * Closes the data source connection.
     */
    void close();
    /**
     * Returns the sensors (product types) supported by this data source
     */
    String[] getSupportedSensors();
    /**
     * Returns a the query parameters for each sensor supported by this data source
     */
    Map<String, Map<String, ParameterDescriptor>> getSupportedParameters();
    /**
     * Creates a query object that can be used to look for products in this data source.
     * This is intended to be used on single product type data source.
     */
    default Q createQuery() { return createQuery(null); }
    /**
     * Creates a query object that can be used to look for products of the given type in this data source.
     * @param sensorName  The sensor name
     */
    Q createQuery(String sensorName);
    /**
     * Retrieves the fetch strategy for products of the given sensor name
     * @param sensorName    The sensor name
     */
    ProductFetchStrategy getProductFetchStrategy(String sensorName);
}
