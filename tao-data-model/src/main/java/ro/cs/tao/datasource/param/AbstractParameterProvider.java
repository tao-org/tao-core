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

import org.apache.commons.lang3.exception.ExceptionUtils;
import ro.cs.tao.datasource.CollectionDescription;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.serialization.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

import static ro.cs.tao.serialization.JsonMapper.JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE;
import static ro.cs.tao.serialization.JsonMapper.JSON_SENSOR_TYPE_REFERENCE;

/**
 * Base class for data source parameter providers.
 *
 * @author Valentin Netoiu on 10/17/2019.
 */
public abstract class AbstractParameterProvider implements ParameterProvider {

    private static final String PARAMETER_DESCRIPTOR_RESOURCE = "ro/cs/tao/datasource/parameters/parameters.json";
    private static final String SENSOR_TYPE_RESOURCE = "ro/cs/tao/datasource/parameters/sensors.json";
    private static final List<String> resources;
    private static final Map<Class<?>, Map<String, Map<String, DataSourceParameter>>> readParamDescriptors;
    private static final Map<Class<?>, Map<String, CollectionDescription>> readTypeDescriptors;

    protected final String[] supportedSensors;
    protected final Map<String, Map<String, DataSourceParameter>> supportedParameters;
    protected final Map<String, CollectionDescription> sensorTypes;
    protected Map<String, ProductFetchStrategy> productFetchers;
    protected Logger logger = Logger.getLogger(getClass().getName());

    static {
        resources = new ArrayList<>();
        readParamDescriptors = Collections.synchronizedMap(new HashMap<>());
        readTypeDescriptors = Collections.synchronizedMap(new HashMap<>());
        try {
            Enumeration<URL> iterator = AbstractParameterProvider.class.getClassLoader().getResources(PARAMETER_DESCRIPTOR_RESOURCE);
            while (iterator.hasMoreElements()) {
                String resourcePath = iterator.nextElement().toURI().toString().replace("jar:", "");
                resources.add(resourcePath);
            }
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
        try {
            Enumeration<URL> iterator = AbstractParameterProvider.class.getClassLoader().getResources(SENSOR_TYPE_RESOURCE);
            while (iterator.hasMoreElements()) {
                String resourcePath = iterator.nextElement().toURI().toString().replace("jar:", "");
                resources.add(resourcePath);
            }
        } catch (Exception e) {
            System.err.println(ExceptionUtils.getStackTrace(e));
        }
    }

    protected AbstractParameterProvider() {
        Map<String, Map<String, DataSourceParameter>> readParameters = null;
        Map<String, CollectionDescription> readSensorTypes = null;
        try {
            final Class<? extends AbstractParameterProvider> clazz = getClass();
            if (!readParamDescriptors.containsKey(clazz)) {
                readParamDescriptors.put(clazz,
                                         JsonMapper.instance().readValue(readDescriptor(PARAMETER_DESCRIPTOR_RESOURCE),
                                                               JSON_DATA_SOURCE_PARAMETERS_TYPE_REFERENCE));
                final String sensorDescriptor = readDescriptor(SENSOR_TYPE_RESOURCE);
                if (sensorDescriptor != null) {
                    readTypeDescriptors.put(clazz,
                                            JsonMapper.instance().readValue(sensorDescriptor, JSON_SENSOR_TYPE_REFERENCE));
                }
            }
            readParameters = readParamDescriptors.get(clazz);
            readSensorTypes = readTypeDescriptors.get(clazz);

        } catch (Exception e) {
            logger.severe(String.format("Cannot load data source supported parameters. Cause: %s", e.getMessage()));
        }

        if (readParameters != null){
            supportedParameters = readParameters;
            supportedSensors = readParameters.keySet().toArray(new String[0]);
        } else {
            supportedParameters = new HashMap<>();
            supportedSensors = new String[0];
        }
        if (readSensorTypes != null) {
            sensorTypes = readSensorTypes;
        } else {
            sensorTypes = new HashMap<>();
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

    @Override
    public Map<String, CollectionDescription> getSensorTypes() {
        return sensorTypes;
    }

    @Override
    public Map<String, ProductFetchStrategy> getRegisteredProductFetchStrategies() { return productFetchers; }

    private String readDescriptor(String fileName) throws IOException, URISyntaxException {
        final String classLocation = getClass().getProtectionDomain().getCodeSource().getLocation().toURI().toString().replace("jar:", "");
        String currentResource = null;
        for (String current : resources) {
            if (current.startsWith(classLocation) && current.endsWith(fileName)) {
                currentResource = current;
                break;
            }
        }
        if (currentResource == null) {
            if (fileName.endsWith("parameters.json")) {
                throw new IOException("No parameter descriptor found");
            } else {
                return null;
            }
        }
        Path rPath;
        FileSystem fileSystem = null;
        try {
            if (classLocation.endsWith(".jar") || classLocation.contains(".jar")) {
                Map<String, String> env = new HashMap<>();
                env.put("create", "false");
                final String strPath = "jar:" + currentResource;
                final URI uri = URI.create(strPath);
                try {
                    fileSystem = FileSystems.getFileSystem(uri);
                } catch (FileSystemNotFoundException ignored) {
                    fileSystem = FileSystems.newFileSystem(uri, env);
                }
                rPath = fileSystem.getPath("/" + fileName);
            } else {
                rPath = Paths.get(URI.create(currentResource));
            }
            return new String(Files.readAllBytes(rPath));
        } finally {
            if (fileSystem != null) {
                fileSystem.close();
            }
        }
    }
}
