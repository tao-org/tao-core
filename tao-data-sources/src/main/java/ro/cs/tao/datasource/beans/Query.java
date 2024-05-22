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
package ro.cs.tao.datasource.beans;

import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceManager;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.GenericAdapter;
import ro.cs.tao.serialization.JsonMapper;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.utils.CompositeKey;
import ro.cs.tao.utils.DateUtils;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class Query extends LongIdentifiable {
    private static final DateTimeFormatter dateFormat = DateUtils.getResilientFormatterAtUTC();//DateUtils.getFormatterAtUTC("yyyy-MM-dd");
    private static final Map<CompositeKey, DataSourceComponent> componentPool = Collections.synchronizedMap(new HashMap<>());
    private String label;
    private String userId;
    private String siteId;
    private Long workflowNodeId;
    private String sensor;
    private String dataSource;
    private String user;
    private String password;
    private String secret;
    private int pageSize;
    private int pageNumber;
    private int limit;
    private Double coverage;
    private Map<String, String> values;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String componentId;
    private String groupId;
    private String groupLabel;

    public Query() { super(); }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getSiteId() { return siteId; }
    public void setSiteId(String siteId) { this.siteId = siteId; }

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

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

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

    public String getComponentId() { return componentId; }
    public void setComponentId(String componentId) { this.componentId = componentId; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupLabel() { return groupLabel; }
    public void setGroupLabel(String groupLabel) { this.groupLabel = groupLabel; }

    public double getCoverage() { return coverage != null ? coverage : 0.0; }
    public void setCoverage(Double coverage) { this.coverage = coverage; }

    public static DataQuery toDataQuery(Query webQuery) throws SerializationException {
        DataQuery query = null;
        if (webQuery != null) {
            try {
                final CompositeKey key = new CompositeKey(webQuery.getSensor(),
                                                          webQuery.getDataSource(),
                                                          webQuery.getUser());
                if (!componentPool.containsKey(key)) {
                    componentPool.put(key, new DataSourceComponent(webQuery.getSensor(), webQuery.getDataSource()));
                }
                DataSourceComponent dsComponent = componentPool.get(key);
                if (webQuery.getUser() != null && webQuery.getPassword() != null) {
                    dsComponent.setUserCredentials(webQuery.getUser(), webQuery.getPassword(), webQuery.getSecret());
                }
                final Map<String, DataSourceParameter> parameterDescriptorMap =
                        DataSourceManager.getInstance().getSupportedParameters(webQuery.getSensor(), webQuery.getDataSource());
                query = dsComponent.createQuery();
                query.setMaxResults(webQuery.getLimit() > 0 ? webQuery.getLimit() : 10);
                query.setPageNumber(webQuery.getPageNumber() > 0 ? webQuery.getPageNumber() : 1);
                query.setPageSize(webQuery.getPageSize() > 0 ? webQuery.getPageSize() : 10);
                query.setCoverage(webQuery.getCoverage());
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
                    if ("".equals(value)) {
                        value = null;
                    }
                    if (value != null && value.startsWith("[") & value.endsWith("]")) {
                        if (!type.isArray()) {
                            throw new IllegalArgumentException(String.format("[%s] parameter type is not an array", paramName));
                        }
                        String[] elements = value.substring(1, value.length() - 1).split(",");
                        final Class arrayType = type.isArray() ? type.getComponentType() : type;
                        if (LocalDateTime.class.isAssignableFrom(arrayType)) {
                            // date[] values are not an actual array, but represent [min,max] pairs
                            query.addParameter(query.createParameter(paramName,
                                                                     type,
                                                                     elements[0].length() > 10 ?
                                                                        LocalDateTime.parse(elements[0], dateFormat) :
                                                                        LocalDate.parse(elements[0], dateFormat).atStartOfDay(),
                                                                     elements[1].length() > 10 ?
                                                                        LocalDateTime.parse(elements[1], dateFormat) :
                                                                        LocalDate.parse(elements[1], dateFormat).plusDays(1).atStartOfDay().minusSeconds(1)));
                        } else {
                            final Object array = Array.newInstance(arrayType, elements.length);
                            final GenericAdapter adapter = new GenericAdapter(arrayType.getName());
                            for (int i = 0; i < elements.length; i++) {
                                Array.set(array, i, adapter.marshal(elements[i]));
                            }
                            query.addParameter(query.createParameter(paramName, type, array));
                        }
                    } else {
                        if (LocalDateTime.class.isAssignableFrom(type)) {
                            final String stringValue = String.valueOf(value);
                            query.addParameter(paramName, type,
                                               stringValue.length() > 10 ?
                                                       LocalDateTime.parse(stringValue, dateFormat) :
                                                       LocalDate.parse(stringValue, dateFormat).atStartOfDay());
                        } else if (Polygon2D.class.isAssignableFrom(type)) {
                            query.addParameter(paramName, type, Polygon2D.fromWKT(String.valueOf(value)));
                        } else {
                            query.addParameter(paramName, value);
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
                    subQuery.siteId = this.siteId;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Query query = (Query) o;
        return id.equals(query.id) &&
                sensor.equals(query.sensor) &&
                dataSource.equals(query.dataSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, sensor, dataSource);
    }

    @Override
    public String toString() {
        try {
            return JsonMapper.instance().writeValueAsString(this);
        } catch (Exception ex) {
            Logger.getLogger(Query.class.getName()).severe(ex.getMessage());
            return super.toString();
        }
    }

    public static Query fromString(String jsonValue) {
        try {
            return JsonMapper.instance().readValue(jsonValue, Query.class);
        } catch (Exception ex) {
            Logger.getLogger(Query.class.getName()).severe(ex.getMessage());
            return null;
        }
    }
}
