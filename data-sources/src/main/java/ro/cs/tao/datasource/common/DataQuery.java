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

import ro.cs.tao.datasource.common.parameter.ParameterDescriptor;
import ro.cs.tao.datasource.common.parameter.ParameterProvider;
import ro.cs.tao.datasource.common.parameter.QueryParameter;
import ro.cs.tao.eodata.EOData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstraction for a datasource query.
 *
 * @author Cosmin Cara
 */
public abstract class DataQuery<R extends EOData> {
    protected DataSource source;
    protected String queryText;
    protected Map<String, QueryParameter> parameters;
    protected int pageSize;
    protected int pageNumber;
    protected int limit;
    protected long timeout;
    protected final Map<String, ParameterDescriptor> supportedParams;
    protected final Set<String> mandatoryParams;

    public DataQuery(DataSource source, ParameterProvider parameterProvider) {
        this.source = source;
        this.parameters = new LinkedHashMap<>();
        this.timeout = 10000;
        this.pageSize = -1;
        this.pageNumber = -1;
        this.limit = -1;
        if (parameterProvider == null) {
            throw new IllegalArgumentException("ParameterProvider should be set");
        }
        this.supportedParams = parameterProvider.getSupportedParameters();
        this.mandatoryParams = this.supportedParams.values().stream()
                .filter(ParameterDescriptor::isRequired)
                .map(ParameterDescriptor::getName)
                .collect(Collectors.toSet());
    }

    public QueryParameter addParameter(QueryParameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Cannot accept null parameter");
        }
        checkSupported(parameter.getName(), parameter.getType());
        this.parameters.put(parameter.getName(), parameter);
        return parameter;
    }

    public <V> QueryParameter addParameter(String name, Class<V> type) {
        QueryParameter parameter = createParameter(name, type);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public <V> QueryParameter addParameter(String name, Class<V> type, V value) {
        QueryParameter parameter = createParameter(name, type, value);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public QueryParameter addParameter(String name, Object value) {
        Class clazz = value != null ? value.getClass() : String.class;
        QueryParameter parameter = createParameter(name, clazz, value);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public QueryParameter getParameter(String name) { return this.parameters.get(name); }

    public Set<String> getMandatoryParams() { return this.mandatoryParams; }

    public int getParameterCount() { return this.parameters.size(); }

    public String getText() { return this.queryText; }

    public void setText(String value) { this.queryText = value; }

    public void setPageSize(int value) { this.pageSize = value; }

    public void setPageNumber(int value) { this.pageNumber = value; }

    public void setMaxResults(int value) { this.limit = value; }

    public List<R> execute() {
        final Set<String> mandatoryParams = getMandatoryParams();
        final Map<String, QueryParameter> parameters = getParameters();
        List<String> missing = mandatoryParams.stream()
                .filter(p -> !parameters.containsKey(p)).collect(Collectors.toList());
        if (missing.size() > 0) {
            QueryException ex = new QueryException("Mandatory parameter(s) not supplied");
            ex.addAdditionalInfo("Missing", String.join(",", missing));
            throw ex;
        }
        return executeImpl();
    }

    public Map<String, ParameterDescriptor> getSupportedParameters() { return this.supportedParams; }

    public Map<String, QueryParameter> getParameters() { return this.parameters; }

    public QueryParameter createParameter(String name, Class<?> type) {
        checkSupported(name, type);
        return new QueryParameter(type, name);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V value) {
        checkSupported(name, type);
        return new QueryParameter(type, name, value);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V value, boolean optional) {
        checkSupported(name, type);
        return new QueryParameter(type, name, value, optional);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue) {
        checkSupported(name, type);
        return new QueryParameter(type, name, minValue, maxValue);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue, boolean optional) {
        checkSupported(name, type);
        return new QueryParameter(type, name, minValue, maxValue, optional);
    }

    protected void checkSupported(String name, Class type) {
        ParameterDescriptor descriptor = this.supportedParams.get(name);
        if (descriptor == null) {
            throw new QueryException(String.format("Parameter [%s] not supported on this data source", name));
        }
        if (!descriptor.getType().isAssignableFrom(type)) {
            throw new QueryException(
                    String.format("Wrong type for parameter [%s]: expected %s, found %s",
                                  name, descriptor.getType().getSimpleName(), type.getSimpleName()));
        }
    }

    protected abstract List<R> executeImpl();
}
