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

import ro.cs.tao.eodata.EOData;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    public DataQuery(DataSource source) {
        this.source = source;
        this.parameters = new LinkedHashMap<>();
        this.timeout = 10000;
        this.pageSize = -1;
        this.pageNumber = -1;
        this.limit = -1;
    }

    public QueryParameter addParameter(QueryParameter parameter) {
        if (parameter != null) {
            this.parameters.put(parameter.getName(), parameter);
        }
        return parameter;
    }

    public QueryParameter getParameter(String name) { return this.parameters.get(name); }

    public int getParameterCount() { return this.parameters.size(); }

    public String getText() { return this.queryText; }

    public void setText(String value) { this.queryText = value; }

    public void setPageSize(int value) { this.pageSize = value; }

    public void setPageNumber(int value) { this.pageNumber = value; }

    public void setMaxResults(int value) { this.limit = value; }

    public abstract List<R> execute() throws QueryException;

    public QueryParameter createParameter(String name, Class<?> type) {
        return new QueryParameter(type, name);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V value) {
        return new QueryParameter(type, name, value);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V value, boolean optional) {
        return new QueryParameter(type, name, value, optional);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue) {
        return new QueryParameter(type, name, minValue, maxValue);
    }

    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue, boolean optional) {
        return new QueryParameter(type, name, minValue, maxValue, optional);
    }

}
