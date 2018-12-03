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
package ro.cs.tao.execution.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceManager;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.GenericAdapter;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.utils.CompositeKey;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class Query {
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final Map<CompositeKey, DataSourceComponent> componentPool = Collections.synchronizedMap(new HashMap<>());
    private Long id;
    private String label;
    private String userId;
    private Long workflowNodeId;
    private String sensor;
    private String dataSource;
    private String user;
    private String password;
    private int pageSize;
    private int pageNumber;
    private int limit;
    private Map<String, String> values;
    private LocalDateTime created;
    private LocalDateTime modified;

    public Query() { }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getWorkflowNodeId() { return workflowNodeId; }
    public void setWorkflowNodeId(Long workflowNodeId) { this.workflowNodeId = workflowNodeId; }

    public Map<String, String> getValues() { return values; }
    public void setValues(Map<String, String> values) { this.values = values; }

    public String getSensor() { return sensor; }
    public void setSensor(String sensor) { this.sensor = sensor; }

    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getPageSize() {
        return pageSize;
    }
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber;
    }
    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getLimit() {
        return limit;
    }
    public void setLimit(int limit) {
        this.limit = limit;
    }

    public LocalDateTime getCreated() { return created; }
    public void setCreated(LocalDateTime created) { this.created = created; }

    public LocalDateTime getModified() { return modified; }
    public void setModified(LocalDateTime modified) { this.modified = modified; }

    public static DataQuery toDataQuery(Query webQuery) throws SerializationException {
        DataQuery query = null;
        if (webQuery != null) {
            try {
                CompositeKey key = new CompositeKey(webQuery.getSensor(),
                                                    webQuery.getDataSource(),
                                                    webQuery.getUser());
                if (!componentPool.containsKey(key)) {
                    componentPool.put(key, new DataSourceComponent(webQuery.getSensor(), webQuery.getDataSource()));
                }
                DataSourceComponent dsComponent = componentPool.get(key);
                if (webQuery.getUser() != null && webQuery.getPassword() != null) {
                    dsComponent.setUserCredentials(webQuery.getUser(), webQuery.getPassword());
                }
                final Map<String, DataSourceParameter> parameterDescriptorMap =
                        DataSourceManager.getInstance().getSupportedParameters(webQuery.getSensor(), webQuery.getDataSource());
                query = dsComponent.createQuery();
                query.setMaxResults(webQuery.getLimit() > 0 ? webQuery.getLimit() : 10);
                query.setPageNumber(webQuery.getPageNumber() > 0 ? webQuery.getPageNumber() : 1);
                query.setPageSize(webQuery.getPageSize() > 0 ? webQuery.getPageSize() : 10);
                Map<String, String> paramValues = webQuery.getValues();
                for (Map.Entry<String, String> entry : paramValues.entrySet()) {
                    String paramName = entry.getKey();
                    final DataSourceParameter descriptor = parameterDescriptorMap.get(paramName);
                    if (descriptor == null) {
                        throw new SerializationException(String.format("Parameter %s does not exist for the datasource %s",
                                                                       paramName, webQuery.getDataSource()));
                    }
                    final Class type = descriptor.getType();
                    String value = entry.getValue();
                    if (value != null && value.startsWith("[") & value.endsWith("]")) {
                        String[] elements = value.substring(1, value.length() - 1).split(",");
                        if (Date.class.isAssignableFrom(type)) {
                            query.addParameter(query.createParameter(paramName,
                                                                     type,
                                                                     SIMPLE_DATE_FORMAT.parse(elements[0]),
                                                                     SIMPLE_DATE_FORMAT.parse(elements[1])));
                        } else {
                            Object array = Array.newInstance(type, elements.length);
                            GenericAdapter adapter = new GenericAdapter(type.getName());
                            for (int i = 0; i < elements.length; i++) {
                                Array.set(array, i, adapter.marshal(elements[i]));
                            }
                            query.addParameter(query.createParameter(paramName, type, array));
                        }
                    } else {
                        if (Date.class.isAssignableFrom(type)) {
                            query.addParameter(paramName, type,
                                               SIMPLE_DATE_FORMAT.parse(String.valueOf(value)));
                        } else if (Polygon2D.class.isAssignableFrom(type)) {
                            query.addParameter(paramName, type, Polygon2D.fromWKT(String.valueOf(value)));
                        } else {
                            query.addParameter(paramName, entry.getValue());
                        }
                    }
                }
            } catch (Exception pex) {
                throw new SerializationException(pex);
            }
        }
        return query;
    }

    public List<Query> splitByParameter(String parameterName) {
        List<Query> subQueries = new ArrayList<>();
        if (this.values != null && this.values.containsKey(parameterName)) {
            String value = this.values.get(parameterName);
            if (value.startsWith("[") && value.endsWith("]")) {
                String[] vals = value.substring(1, value.length() - 1).split(",");
                for (String val : vals) {
                    Query subQuery = new Query();
                    subQuery.userId = this.userId;
                    subQuery.dataSource = this.dataSource;
                    subQuery.sensor = this.sensor;
                    subQuery.user = this.user;
                    subQuery.password = this.password;
                    subQuery.pageSize = this.pageSize;
                    subQuery.pageNumber = this.pageNumber;
                    subQuery.limit = this.limit;
                    subQuery.values = new HashMap<>();
                    for (Map.Entry<String, String> entry : this.values.entrySet()) {
                        if (!parameterName.equals(entry.getKey())) {
                            subQuery.values.put(entry.getKey(), entry.getValue());
                        }
                    }
                    subQuery.values.put(parameterName, val);
                    subQueries.add(subQuery);
                }
            } else {
                subQueries.add(this);
            }
        } else {
            subQueries.add(this);
        }
        return subQueries;
    }

    @Override
    public String toString() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(Query.class.getName()).severe(ex.getMessage());
            return super.toString();
        }
    }

    public static Query fromString(String jsonValue) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.readValue(jsonValue, Query.class);
        } catch (Exception ex) {
            Logger.getLogger(Query.class.getName()).severe(ex.getMessage());
            return null;
        }
    }
}
