/*
 * Copyright (C) 2017 CS ROMANIA
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
package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.param.ParameterDescriptor;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.Map;
import java.util.Properties;

/**
 * Base class for data sources.
 * The implementation is an abstract class instead of an interface due to limitations in serialization libraries.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataSource")
public abstract class DataSource<Q extends DataQuery> extends StringIdentifiable {
    protected Properties properties;

    public DataSource() {
        this.properties = new Properties();
    }

    @Override
    public String defaultId() { return null; }

    /**
     * Returns the given property value.
     * @param name  The property name
     */
    public String getProperty(String name) {
        return this.properties.getProperty(name);
    }

    @XmlTransient
    /**
     * Returns the capabilities flag of this data source.
     * See {@link DataSourceCapability} enumeration for more details
     */
    public int getCapabilities() { return DataSourceCapability.QUERY | DataSourceCapability.DOWNLOAD; }

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
