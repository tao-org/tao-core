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

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

import static com.google.common.collect.Iterables.toArray;
import static ro.cs.tao.serialization.ObjectMapperProvider.JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE;
import static ro.cs.tao.serialization.ObjectMapperProvider.JSON_OBJECT_MAPPER;

/**
 * @author Valentin Netoiu on 10/17/2019.
 */
public abstract class AbstractParameterProvider implements ParameterProvider {

    protected final String[] supportedSensors;
    protected final Map<String, Map<String, DataSourceParameter>> supportedParameters;

    protected Logger logger = Logger.getLogger(getClass().getName());

    protected AbstractParameterProvider() {
        Map<String, Map<String, DataSourceParameter>> readParameters = null;
        URL parametersConfigUrl = getClass().getResource("parameters.json");
        if(parametersConfigUrl != null){
            try {
                readParameters = JSON_OBJECT_MAPPER.readValue(parametersConfigUrl, JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE);
            } catch (IOException e) {
                logger.severe(String.format("Cannot load data source supported parameters from %s. Cause: %s", parametersConfigUrl, e.getMessage()));
            }
        }

        if(readParameters != null){
            supportedParameters = readParameters;
            supportedSensors = toArray(readParameters.keySet(), String.class);
        } else {
            supportedParameters = Maps.newHashMap();
            supportedSensors = new String[0];
        }
    }

    /**
     * Returns the query parameters for all the sensors supported by this data source
     */
    @Override
    public Map<String, Map<String, DataSourceParameter>> getSupportedParameters() {
       return supportedParameters;
    }


    /**
     * Returns the sensors supported by this data source
     */
    @Override
    public String[] getSupportedSensors() {
        return supportedSensors;
    }
}
