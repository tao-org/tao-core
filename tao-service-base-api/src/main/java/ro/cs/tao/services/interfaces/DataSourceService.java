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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.datasource.DataSourceCredentials;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.services.model.datasource.DataSourceDescriptor;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * Interface for the data source service
 *
 * @author Cosmin Cara
 */
public interface DataSourceService extends TAOService {

    /**
     * Returns the sorted set of sensors (or collections) detected in all the data source plugins.
     */
    SortedSet<String> getSupportedSensors();

    /**
     * Returns the list of data sources available for the given sensor (satellite or collection)
     * @param sensorName    The name of the satellite or collection
     */
    List<String> getDatasourcesForSensor(String sensorName);

    /**
     * Returns the list of descriptors for all the available data source plugins
     */
    List<DataSourceDescriptor> getDatasourceInstances();

    Map<String, String[]> getDataSourceProviders();

    /**
     * Returns the list of parameters supported by the data source identified by the given
     * sensor (or collection) and name.
     * @param sensorName        The name of the satellite (or collection)
     * @param dataSourceName    The name of the data source
     */
    List<DataSourceParameter> getSupportedParameters(String sensorName, String dataSourceName);

    /**
     * Performs a count query using the given query definition
     * @param queryObject   The query definition
     */
    long count(Query queryObject) throws SerializationException;
    /**
     * Performs a query using the given query definition and returns the (found) list of products.
     * @param queryObject   The query definition
     */
    List<EOProduct> query(Query queryObject) throws SerializationException;

    List<EOProduct> fetch(Query queryObject, List<EOProduct> products, FetchMode mode, String localPath, String pathFormat);

    DataSourceCredentials getDataSourceCredentials(String dataSource);
}
