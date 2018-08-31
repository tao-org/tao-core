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

import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.serialization.BaseSerializer;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.serialization.SerializerFactory;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Abstraction for a datasource query.
 *
 * @author Cosmin Cara
 */
@XmlTransient
public abstract class DataQuery extends StringIdentifiable {
    protected static final int DEFAULT_LIMIT = 100;
    protected DataSource source;
    protected String sensorName;
    protected String queryText;
    protected Map<String, QueryParameter> parameters;
    protected int pageSize;
    protected int pageNumber;
    protected int limit;
    protected long timeout;
    protected Map<String, ParameterDescriptor> supportedParams;
    protected Set<String> mandatoryParams;
    protected Logger logger = Logger.getLogger(getClass().getName());

    protected DataQuery() { }

    public DataQuery(DataSource source, String sensorName) {
        if (source == null) {
            throw new QueryException("Empty source");
        }
        if (sensorName == null || Arrays.stream(source.getSupportedSensors()).noneMatch(sensorName::equals)) {
            throw new QueryException("Empty sensor name");
        }
        initialize(source, sensorName);
    }

    public DataSource getSource() {
        return source;
    }

    public void setSource(DataSource source) {
        this.source = source;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public QueryParameter addParameter(QueryParameter parameter) {
        if (parameter == null) {
            throw new IllegalArgumentException("Cannot accept null parameter");
        }
        checkSupported(parameter.getName(), parameter.getType());
        this.parameters.put(parameter.getName(), parameter);
        return parameter;
    }

    public <V> QueryParameter<V> addParameter(String name, Class<V> type) {
        QueryParameter<V> parameter = createParameter(name, type);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public <V> QueryParameter<V> addParameter(String name, Class<V> type, V value) {
        QueryParameter<V> parameter = createParameter(name, type, value);
        this.parameters.put(name, parameter);
        return parameter;
    }

    public <V> QueryParameter<V> addParameter(String name, V value) {
        Class clazz = this.supportedParams.get(name).getType();
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

    public List<EOProduct> execute() {
        final Set<String> mandatoryParams = getMandatoryParams();
        final Map<String, QueryParameter> actualParameters = getParameters();
        List<String> missing = mandatoryParams.stream()
                .filter(p -> !actualParameters.containsKey(p)).collect(Collectors.toList());
        if (missing.size() > 0) {
            QueryException ex = new QueryException("Mandatory parameter(s) not supplied");
            ex.addAdditionalInfo("Missing", String.join(",", missing));
            throw ex;
        }
        this.supportedParams.entrySet().stream()
                .filter(entry -> entry.getValue().getDefaultValue() != null && !actualParameters.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(p -> addParameter(p.getName(), p.getType(), p.getDefaultValue()));
        List<EOProduct> retList = executeImpl();
        retList.sort(Comparator.comparing(EOProduct::getAcquisitionDate));
        return retList;
    }

    public long getCount() {
        final Set<String> mandatoryParams = getMandatoryParams();
        final Map<String, QueryParameter> parameters = getParameters();
        List<String> missing = mandatoryParams.stream()
                .filter(p -> !parameters.containsKey(p)).collect(Collectors.toList());
        if (missing.size() > 0) {
            QueryException ex = new QueryException("Mandatory parameter(s) not supplied");
            ex.addAdditionalInfo("Missing", String.join(",", missing));
            throw ex;
        }
        this.supportedParams.entrySet().stream()
                .filter(entry -> entry.getValue().getDefaultValue() != null && !parameters.containsKey(entry.getKey()))
                .map(Map.Entry::getValue)
                .forEach(p -> addParameter(p.getName(), p.getType(), p.getDefaultValue()));
        return getCountImpl();
    }

    public Map<String, ParameterDescriptor> getSupportedParameters() { return this.supportedParams; }

    public Map<String, QueryParameter> getParameters() { return this.parameters; }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name);
    }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type, V value) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name, value);
    }

    public <V> QueryParameter<V> createParameter(String name, Class<V> type, V value, boolean optional) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name, value, optional);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name, minValue, maxValue);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue, boolean optional) {
        checkSupported(name, type);
        return new QueryParameter<>(type, name, minValue, maxValue, optional);
    }

    @Override
    public DataQuery clone() throws CloneNotSupportedException {
        DataQuery newQuery = (DataQuery) super.clone();
        newQuery.initialize(this.source, this.sensorName);
        this.parameters.forEach((key, value) -> newQuery.parameters.put(key, value));
        return newQuery;
    }

    public String exportParametersAsXML() throws SerializationException {
        final BaseSerializer<QueryParameter> serializer = SerializerFactory.create(QueryParameter.class, MediaType.XML);
        return serializer.serialize(new ArrayList<>(this.parameters.values()), "parameters");
    }

    public void importParameters(String xml) throws SerializationException {
        final BaseSerializer<QueryParameter> serializer = SerializerFactory.create(QueryParameter.class, MediaType.XML);
        List<QueryParameter> parameters = serializer.deserializeList(QueryParameter.class, new StreamSource(new StringReader(xml)));
        this.parameters.clear();
        for (QueryParameter parameter : parameters) {
            addParameter(parameter.getName(), parameter.getValue());
        }
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
        if (this.parameters.containsKey(name)) {
            throw new QueryException(
                    String.format("Parameter [%s] already exists", name));
        }
    }

    protected abstract List<EOProduct> executeImpl();

    protected long getCountImpl() { return -1; }

    private void initialize(DataSource source, String sensorName) {
        this.source = source;
        this.sensorName = sensorName;
        this.parameters = new LinkedHashMap<>();
        this.timeout = 10000;

        this.pageSize = -1;
        this.pageNumber = -1;
        this.limit = -1;
        Map<String, Map<String, ParameterDescriptor>> supportedParameters = source.getSupportedParameters();
        this.supportedParams = supportedParameters.get(sensorName);
        this.mandatoryParams = this.supportedParams.values().stream()
                .filter(p -> p.isRequired() && p.getDefaultValue() == null)
                .map(ParameterDescriptor::getName)
                .collect(Collectors.toSet());
    }
}
