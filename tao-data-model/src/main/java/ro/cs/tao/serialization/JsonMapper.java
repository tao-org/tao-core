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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ro.cs.tao.datasource.CollectionDescription;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.param.IndexedDataSourceParameter;

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
        instance = new ObjectMapper()
                //.findAndRegisterModules()
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_ARRAY_AS_NULL_OBJECT, true)
                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                .registerModule(new JavaTimeModule());

        JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE = new TypeReference<Map<String, Map<String, DataSourceParameter>>>() {};
        JSON_SENSOR_TYPE_REFERENCE = new TypeReference<Map<String, CollectionDescription>>() { };
        JSON_INDEXED_DATA_SOURCE_REFERENCE = new TypeReference<Map<String, IndexedDataSourceParameter>>() { };
    }

    public static ObjectMapper instance() { return instance; }

    private JsonMapper() {

    }
}

