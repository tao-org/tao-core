/*
 * Copyright (C) 2018 CS ROMANIA
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
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.param.ParameterProvider;

import java.util.Map;

/**
 * Abstraction for a product data source.
 *
 * @author Cosmin Cara
 */
public abstract class AbstractDataSource<Q extends DataQuery, T> extends DataSource<Q, T> {
    protected String connectionString;
    protected String alternateConnectionString;
    protected long timeout;
    protected UsernamePasswordCredentials credentials;
    private ParameterProvider parameterProvider;
    private boolean useAlternateConnectionString;

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
    public String getAlternateConnectionString() {
        return this.alternateConnectionString != null ? this.alternateConnectionString : this.connectionString;
    }

    @Override
    public String getConnectionString() { return connectionString; }

    @Override
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }

    @Override
    public boolean useAlternateConnectionString() { return this.useAlternateConnectionString; }

    @Override
    public void useAlternateConnectionString(boolean value) { this.useAlternateConnectionString = value; }

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
            if(productFetchStrategy == null){
                for(String expectedSensorName:this.parameterProvider.getRegisteredProductFetchStrategies().keySet()){
                    if(sensorName.toLowerCase().startsWith(expectedSensorName.toLowerCase()) || expectedSensorName.toLowerCase().startsWith(sensorName.toLowerCase())){
                        productFetchStrategy = this.parameterProvider.getRegisteredProductFetchStrategies().get(expectedSensorName);
                    }
                }
            }
            if (productFetchStrategy != null) {
                productFetchStrategy.setCredentials(this.credentials);
            }
        }
        return productFetchStrategy;
    }

    @Override
    public Map<String, Map<String, DataSourceParameter>> getSupportedParameters() {
        Map<String, Map<String, DataSourceParameter>> descriptors = null;
        if (this.parameterProvider != null) {
            descriptors = this.parameterProvider.getSupportedParameters();
            if (this.filteredParameters != null) {
                for (Map.Entry<String, Map<String, Map<String, String>>> entry : this.filteredParameters.entrySet()) {
                    final Map<String, DataSourceParameter> sensorParameters = descriptors.get(entry.getKey());
                    if (sensorParameters != null) {
                        final Map<String, Map<String, String>> parameterMap = entry.getValue();
                        for (Map.Entry<String, Map<String, String>> parameterValues : parameterMap.entrySet()) {
                            final DataSourceParameter parameter = sensorParameters.get(parameterValues.getKey());
                            if (parameter != null) {
                                final Map<String, String> values = parameterValues.getValue();
                                parameter.setValueSet(values.values().toArray());
                            }
                        }
                    }
                }
            }
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
    public Map<String, CollectionDescription> getSensorTypes() {
        Map<String, CollectionDescription> types = null;
        if (this.parameterProvider != null) {
            types = this.parameterProvider.getSensorTypes();
        }
        return types;
    }

    @Override
    public long getTimeout() { return this.timeout; }

    @Override
    public void setTimeout(long value) { this.timeout = value; }

    @Override
    public void setCredentials(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException(String.format("Datasource %s requires an account", this.id));
        }
        this.credentials = new UsernamePasswordCredentials(username, password);
    }

    protected abstract Q createQueryImpl(String code);
}
