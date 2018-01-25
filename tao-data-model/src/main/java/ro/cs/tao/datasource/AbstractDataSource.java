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
 *
 */
package ro.cs.tao.datasource;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;

import java.util.Map;

/**
 * Abstraction for a product datasource source.
 *
 * @author Cosmin Cara
 */
public abstract class AbstractDataSource<Q extends DataQuery> extends DataSource<Q> {
    protected String connectionString;
    protected String alternateConnectionString;
    protected long timeout;
    protected UsernamePasswordCredentials credentials;
    private ParameterProvider parameterProvider;

    public AbstractDataSource() {
        super();
        this.timeout = 10000;
    }

    public AbstractDataSource(String connectionString) {
        this();
        this.connectionString = connectionString;
    }

    @Override
    public Q createQuery(String type) { return createQueryImpl(type); }

    @Override
    public String getAlternateConnectionString() { return this.alternateConnectionString; }

    @Override
    public String getConnectionString() { return connectionString; }

    @Override
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }

    @Override
    public UsernamePasswordCredentials getCredentials() { return this.credentials; }

    public ParameterProvider getParameterProvider() {
        return this.parameterProvider;
    }

    protected void setParameterProvider(ParameterProvider provider) {
        this.parameterProvider = provider;
    }

    @Override
    public ProductFetchStrategy getProductFetchStrategy(String sensorName) {
        ProductFetchStrategy productFetchStrategy = null;
        if (this.parameterProvider != null) {
            productFetchStrategy = this.parameterProvider.getRegisteredProductFetchStrategies().get(sensorName);
            if (productFetchStrategy != null) {
                productFetchStrategy.setCredentials(this.credentials);
            }
        }
        return productFetchStrategy;
    }

    @Override
    public Map<String, Map<String, ParameterDescriptor>> getSupportedParameters() {
        Map<String, Map<String, ParameterDescriptor>> descriptors = null;
        if (this.parameterProvider != null) {
            descriptors = this.parameterProvider.getSupportedParameters();
        }
        return descriptors;
    }

    @Override
    public String[] getSupportedSensors() {
        String[] sensors = null;
        if (this.parameterProvider != null) {
            sensors = this.parameterProvider.getSupportedSensors();
        }
        return sensors;
    }

    @Override
    public long getTimeout() { return this.timeout; }

    @Override
    public void setTimeout(long value) { this.timeout = value; }

    @Override
    public void setCredentials(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Invalid user");
        }
        this.credentials = new UsernamePasswordCredentials(username, password);
    }

    protected abstract Q createQueryImpl(String code);
}
