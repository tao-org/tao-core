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
package ro.cs.tao.datasource.common;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.datasource.common.parameter.ParameterDescriptor;
import ro.cs.tao.datasource.common.parameter.ParameterProvider;
import ro.cs.tao.eodata.EOData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstraction for a product datasource source.
 *
 * @author Cosmin Cara
 */
public abstract class AbstractDataSource<R extends EOData, Q extends DataQuery<R>>
        implements DataSource<R, Q> {
    protected String connectionString;
    protected long timeout;
    protected UsernamePasswordCredentials credentials;
    private Map<String, ParameterProvider> parameterProviders;

    public AbstractDataSource() { this.timeout = 10000; }

    public AbstractDataSource(String connectionString) {
        this();
        this.connectionString = connectionString;
    }

    @Override
    public long getTimeout() { return this.timeout; }

    @Override
    public void setTimeout(long value) { this.timeout = value; }

    @Override
    public String getConnectionString() { return connectionString; }

    @Override
    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }

    @Override
    public void setCredentials(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Invalid user");
        }
        this.credentials = new UsernamePasswordCredentials(username, password);
    }

    @Override
    public UsernamePasswordCredentials getCredentials() { return this.credentials; }

    @Override
    public Map<String, Map<String, ParameterDescriptor>> getSupportedParameters() {
        Map<String, Map<String, ParameterDescriptor>> descriptors = null;
        if (this.parameterProviders != null) {
            descriptors = this.parameterProviders.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getSupportedParameters()));
        }
        return descriptors;
    }

    /**
     * Creates a query object that will be executed against the datasource source to retrieve results.
     */
    @Override
    public Q createQuery() { return createQueryImpl(null); }

    @Override
    public Q createQuery(String type) { return createQueryImpl(type); }

    protected abstract Q createQueryImpl(String code);

    protected void addParameterProvider(String code, ParameterProvider provider) {
        if (this.parameterProviders == null) {
            this.parameterProviders = new HashMap<>();
        }
        code = code == null ? "" : code;
        this.parameterProviders.put(code, provider);
    }

    protected ParameterProvider getParameterProvider(String code) {
        code = code == null ? "" : code;
        return this.parameterProviders.get(code);
    }
}
