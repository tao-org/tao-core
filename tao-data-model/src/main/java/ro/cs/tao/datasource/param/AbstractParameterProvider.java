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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static ro.cs.tao.serialization.ObjectMapperProvider.JSON_DATA_SOURCE_PARAMETERS_READER;
import static ro.cs.tao.serialization.ObjectMapperProvider.JSON_OBJECT_MAPPER;

/**
 * Created by vnetoiu on 10/17/2019.
 */
public abstract class AbstractParameterProvider implements ParameterProvider {

    protected Logger logger = Logger.getLogger(getClass().getName());
    protected final URL parametersConfigUrl;
    protected final URL sensorsConfigUrl;

    protected AbstractParameterProvider(URL parametersConfigUrl, URL sensorsConfigUrl) {
        this.parametersConfigUrl = parametersConfigUrl;
        this.sensorsConfigUrl = sensorsConfigUrl;
    }

    /**
     * Returns the query parameters for all the sensors supported by this data source
     */
    @Override
    public Map<String, Map<String, DataSourceParameter>> getSupportedParameters() {
       if(parametersConfigUrl != null){
           try {
               Map<String, Map<String, DataSourceParameter>> supportedParameters = JSON_DATA_SOURCE_PARAMETERS_READER.readValue(parametersConfigUrl);
               return supportedParameters;
           } catch (IOException e) {
               logger.severe(String.format("Cannot load data source supported parameters from %s. Cause: %s", parametersConfigUrl, e.getMessage()));
           }
       }

        return new HashMap<>();
    }


    /**
     * Returns the sensors supported by this data source
     */
    @Override
    public String[] getSupportedSensors() {
        if(sensorsConfigUrl != null){
            try {
                String[] supportedSensors = JSON_OBJECT_MAPPER.readerFor(String[].class).readValue(sensorsConfigUrl);
                return supportedSensors;
            } catch (IOException e) {
                logger.severe(String.format("Cannot load data source supported sensors from %s. Cause: %s", sensorsConfigUrl, e.getMessage()));
            }
        }
        return new String[0];
    }
}
