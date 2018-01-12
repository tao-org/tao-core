package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.component.Identifiable;
import ro.cs.tao.datasource.param.ParameterDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Map;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataSource")
public abstract class DataSource<Q extends DataQuery> extends Identifiable {
    protected Properties properties;

    public DataSource() {
        this.properties = new Properties();
    }

    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }

    /**
     * Returns the timeout for this data source connection
     */
    @XmlTransient
    public abstract long getTimeout();
    /**
     * Sets the timeout for this data source connection
     * @param value     The timeout value in milliseconds
     */
    public abstract void setTimeout(long value);
    /**
     * Returns the connection string for this data source.
     * In the case of a remote data source, it is the base url of the remote endpoint.
     * In the case of a database data source, it is the connection string of the database.
     */
    @XmlTransient
    public abstract String getConnectionString();
    /**
     * Sets the connection string for this data source.
     * @param connectionString      The connection string
     */
    public abstract void setConnectionString(String connectionString);
    @XmlTransient
    public abstract String getAlternateConnectionString();
    /**
     * Sets the credentials needed to connect to this data source
     * @param username  The user id
     * @param password  The user password
     */
    public abstract void setCredentials(String username, String password);
    /**
     * Gets the credentials set for this data source.
     */
    public abstract UsernamePasswordCredentials getCredentials();
    /**
     * Tests that the datasource source is reachable.
     * Must return <code>true</code> if the source is reachable, <code>false</code> otherwise.
     */
    public abstract boolean ping();
    /**
     * Closes the data source connection.
     */
    public abstract void close();
    /**
     * Returns the sensors (product types) supported by this data source
     */
    public abstract String[] getSupportedSensors();
    /**
     * Returns a the query parameters for each sensor supported by this data source
     */
    public abstract Map<String, Map<String, ParameterDescriptor>> getSupportedParameters();
    /**
     * Creates a query object that can be used to look for products in this data source.
     * This is intended to be used on single product type data source.
     */
    public Q createQuery() { return createQuery(null); }
    /**
     * Creates a query object that can be used to look for products of the given type in this data source.
     * @param sensorName  The sensor id
     */
    public abstract Q createQuery(String sensorName);
    /**
     * Retrieves the fetch strategy for products of the given sensor id
     * @param sensorName    The sensor id
     */
    public abstract ProductFetchStrategy getProductFetchStrategy(String sensorName);
}
