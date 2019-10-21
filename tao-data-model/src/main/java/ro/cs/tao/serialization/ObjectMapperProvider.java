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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import ro.cs.tao.datasource.param.DataSourceParameter;

import java.util.Map;

/**
 * @author Valentin Netoiu on 10/17/2019.
 */
public class ObjectMapperProvider {

    public static final ObjectMapper JSON_OBJECT_MAPPER;
    public static final ObjectWriter JSON_DATA_SOURCE_PARAMETERS_WRITER;
    public static final ObjectReader JSON_DATA_SOURCE_PARAMETERS_READER;

    static {
        JSON_OBJECT_MAPPER = new ObjectMapper()
                .findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.ALWAYS)
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
                .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        JSON_DATA_SOURCE_PARAMETERS_READER = JSON_OBJECT_MAPPER.readerFor(new TypeReference<Map<String, Map<String, DataSourceParameter>>>() {
        });
        JSON_DATA_SOURCE_PARAMETERS_WRITER = JSON_OBJECT_MAPPER.writerFor(new TypeReference<Map<String, Map<String, DataSourceParameter>>>() {
        });

    }

    private ObjectMapperProvider() {

    }
}

