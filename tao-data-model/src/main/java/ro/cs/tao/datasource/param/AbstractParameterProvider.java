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
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static com.google.common.collect.Iterables.toArray;
import static ro.cs.tao.serialization.ObjectMapperProvider.JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE;
import static ro.cs.tao.serialization.ObjectMapperProvider.JSON_OBJECT_MAPPER;

/**
 * @author Valentin Netoiu on 10/17/2019.
 */
public abstract class AbstractParameterProvider implements ParameterProvider {

    private static final String PARAMETER_DESCRIPTOR_RESOURCE = "ro/cs/tao/datasource/parameters/parameters.json";
    private static final List<String> parameterResources;

    protected final String[] supportedSensors;
    protected final Map<String, Map<String, DataSourceParameter>> supportedParameters;
    protected Logger logger = Logger.getLogger(getClass().getName());

    static {
        parameterResources = new ArrayList<>();
        try {
            Enumeration<URL> resources = AbstractParameterProvider.class.getClassLoader().getResources(PARAMETER_DESCRIPTOR_RESOURCE);
            while (resources.hasMoreElements()) {
                String resourcePath = resources.nextElement().getPath().replaceFirst("^/(.:/)", "$1").replace("file:/", "");
                if (SystemUtils.IS_OS_LINUX) {
                    resourcePath = "/" + resourcePath;
                }
                parameterResources.add(resourcePath);
            }
        } catch (IOException e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    protected AbstractParameterProvider() {
        Map<String, Map<String, DataSourceParameter>> readParameters = null;
        try {
            readParameters = JSON_OBJECT_MAPPER.readValue(readDescriptor(), JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE);
        } catch (IOException e) {
            logger.severe(String.format("Cannot load data source supported parameters. Cause: %s", e.getMessage()));
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

    private String readDescriptor() throws IOException {
        final String classLocation = getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replaceFirst("^/(.:/)", "$1");
        String currentResource = null;
        for (String current : parameterResources) {
            if (current.startsWith(classLocation)) {
                currentResource = current.replaceFirst("^/(.:/)", "$1");
                break;
            }
        }
        if (currentResource == null) {
            throw new IOException("No parameter descriptor found");
        }
        Path rPath;
        if (classLocation.endsWith(".jar")) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "false");
            final String strPath = "jar:file:" + (SystemUtils.IS_OS_LINUX ? "" : "/") + currentResource;
            rPath = FileSystems.newFileSystem(URI.create(strPath), env).getPath("/" + PARAMETER_DESCRIPTOR_RESOURCE);
        } else {
            rPath = Paths.get(currentResource);
        }
        return new String(Files.readAllBytes(rPath));
    }
}
