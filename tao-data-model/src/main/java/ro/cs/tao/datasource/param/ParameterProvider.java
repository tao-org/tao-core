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
package ro.cs.tao.datasource.param;

import ro.cs.tao.datasource.ProductFetchStrategy;

import java.util.Map;

/**
 * Interface that provides capabilities of a data source (what sensors and
 * what parameters for each sensor)
 *
 * @author Cosmin Cara
 */
public interface ParameterProvider {
    /**
     * Returns the query parameters for all the sensors supported by this data source
     */
    Map<String, Map<String, ParameterDescriptor>> getSupportedParameters();

    /**
     * Returns the sensors supported by this data source
     */
    String[] getSupportedSensors();

    /**
     * Returns the fetchers associated with the supported sensors.
     */
    Map<String, ProductFetchStrategy> getRegisteredProductFetchStrategies();
}
