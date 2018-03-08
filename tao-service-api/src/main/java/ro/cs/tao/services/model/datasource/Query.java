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
package ro.cs.tao.services.model.datasource;

import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceManager;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.serialization.SerializationException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class Query {
    private String dataSource;
    private String sensor;
    private String user;
    private String password;
    private int pageSize;
    private int pageNumber;
    private int limit;
    private Map<String, Object> values;

    public Query() { }

    public Map<String, Object> getValues() { return values; }

    public void setValues(Map<String, Object> values) { this.values = values; }

    public String getDataSource() { return dataSource; }

    public void setDataSource(String dataSource) { this.dataSource = dataSource; }

    public String getSensor() { return sensor; }

    public void setSensor(String sensor) { this.sensor = sensor; }

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

    public static DataQuery toDataQuery(Query webQuery) throws SerializationException {
        DataQuery query = null;
        if (webQuery != null) {
            try {
                DataSourceComponent dsComponent = new DataSourceComponent(webQuery.getSensor(), webQuery.getDataSource());
                dsComponent.setUserCredentials(webQuery.getUser(), webQuery.getPassword());
                final Map<String, ParameterDescriptor> parameterDescriptorMap =
                        DataSourceManager.getInstance().getSupportedParameters(webQuery.getSensor(), webQuery.getDataSource());
                query = dsComponent.createQuery();
                query.setMaxResults(webQuery.getLimit());
                query.setPageNumber(webQuery.getPageNumber());
                query.setPageSize(webQuery.getPageSize());
                Map<String, Object> paramValues = webQuery.getValues();
                for (Map.Entry<String, Object> entry : paramValues.entrySet()) {
                    final ParameterDescriptor descriptor = parameterDescriptorMap.get(entry.getKey());
                    final Class type = descriptor.getType();
                    if (Date.class.isAssignableFrom(type)) {
                        query.addParameter(entry.getKey(), type,
                                           new SimpleDateFormat("yyyy-MM-dd").parse(String.valueOf(entry.getValue())));
                    } else if (Polygon2D.class.isAssignableFrom(type)) {
                        query.addParameter(entry.getKey(), type, Polygon2D.fromWKT(String.valueOf(entry.getValue())));
                    } else {
                        query.addParameter(entry.getKey(), entry.getValue());
                    }
                }
            } catch (ParseException pex) {
                throw new SerializationException(pex);
            }
        }
        return query;
    }
}
