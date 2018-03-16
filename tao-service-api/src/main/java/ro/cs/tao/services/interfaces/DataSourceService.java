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
package ro.cs.tao.services.interfaces;

import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.execution.model.Query;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.services.model.datasource.DataSourceDescriptor;

import java.util.List;
import java.util.SortedSet;

/**
 * @author Cosmin Cara
 */
public interface DataSourceService {

    SortedSet<String> getSupportedSensors();

    List<String> getDatasourcesForSensor(String sensorName);

    List<DataSourceDescriptor> getDatasourceInstances();

    List<ParameterDescriptor> getSupportedParameters(String sensorName, String dataSourceName);

    long count(Query queryObject) throws SerializationException;

    List<EOProduct> query(Query queryObject) throws SerializationException;

    List<EOProduct> fetch(Query queryObject, List<EOProduct> products);

}
