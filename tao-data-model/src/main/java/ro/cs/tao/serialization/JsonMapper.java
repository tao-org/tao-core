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
package ro.cs.tao.serialization;

import ro.cs.tao.datasource.CollectionDescription;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.param.IndexedDataSourceParameter;
import tools.jackson.core.StreamReadFeature;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;

import java.util.Map;

/**
 * @author Valentin Netoiu on 10/17/2019.
 */
public class JsonMapper {

    private static final ObjectMapper instance;
    public static final TypeReference<Map<String, Map<String, DataSourceParameter>>> JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE;
    public static final TypeReference<Map<String, CollectionDescription>> JSON_SENSOR_TYPE_REFERENCE;
    public static final TypeReference<Map<String, IndexedDataSourceParameter>> JSON_INDEXED_DATA_SOURCE_REFERENCE;

    static {
        instance = new ObjectMapper().rebuild()
                //.findAndRegisterModules()
                .configure(EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .disable(StreamReadFeature.AUTO_CLOSE_SOURCE)
                .disable(StreamWriteFeature.AUTO_CLOSE_TARGET)
                .build();

        JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE = new TypeReference<Map<String, Map<String, DataSourceParameter>>>() {};
        JSON_SENSOR_TYPE_REFERENCE = new TypeReference<Map<String, CollectionDescription>>() { };
        JSON_INDEXED_DATA_SOURCE_REFERENCE = new TypeReference<Map<String, IndexedDataSourceParameter>>() { };
    }

    public static ObjectMapper instance() { return instance; }

    private JsonMapper() {

    }
}

