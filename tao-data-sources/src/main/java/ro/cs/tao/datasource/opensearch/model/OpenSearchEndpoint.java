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

package ro.cs.tao.datasource.opensearch.model;

import ro.cs.tao.datasource.param.DataSourceParameter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OpenSearchEndpoint {
    private String type;
    private String scope;
    private String url;
    private Map<String, DataSourceParameter> parameters;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public Map<String, DataSourceParameter> getParameters() { return parameters; }
    public void addParameter(String systemName, DataSourceParameter parameter) {
        if (this.parameters == null) {
            this.parameters = new HashMap<>();
        }
        this.parameters.put(systemName, parameter);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OpenSearchEndpoint that = (OpenSearchEndpoint) o;
        return Objects.equals(type, that.type) && Objects.equals(url, that.url) && Objects.equals(scope, that.scope);
    }

    @Override
    public int hashCode() { return Objects.hash(type, url, scope); }
}
