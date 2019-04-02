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
package ro.cs.tao.services.model.datasource;

import ro.cs.tao.datasource.param.DataSourceParameter;

import java.util.Iterator;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class DataSourceDescriptor {
    private String sensor;
    private String dataSourceName;
    private Map<String, DataSourceParameter> parameters;

    public DataSourceDescriptor() {
    }

    public DataSourceDescriptor(String sensor, String dataSourceName, Map<String, DataSourceParameter> parameters) {
        this.sensor = sensor;
        this.dataSourceName = dataSourceName;
        this.parameters = parameters;
        if (this.parameters != null) {
            Iterator<String> iterator = parameters.keySet().iterator();
            int order = 1;
            while (iterator.hasNext()) {
                parameters.get(iterator.next()).setOrder(order++);
            }
        }
    }

    public String getSensor() {
        return sensor;
    }
    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Map<String, DataSourceParameter> getParameters() {
        return parameters;
    }
    public void setParameters(Map<String, DataSourceParameter> parameters) {
        this.parameters = parameters;
    }
}
