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
package ro.cs.tao.datasource;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;
import ro.cs.tao.utils.NetUtils;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager class for DataSource plugins.
 *
 * @author Cosmin Cara
 */
public class DataSourceManager {

    private static final DataSourceManager instance;
    private final ServiceRegistry<DataSource> registry;
    private final Map<Map.Entry<String, String>, Map<String, DataSourceParameter>> registeredSources;
    private Map<String, Map<String, Map<String, Map<String, String>>>> filteredParameters;
    private final Map<String, DataSource<?, ?>> additionalDatasources;

    static {
        instance = new DataSourceManager();
    }

    /**
     * Returns the only instance of this manager.
     */
    public static DataSourceManager getInstance() { return instance; }

    private DataSourceManager() {
        Map<String, String> proxySettings = ConfigurationManager.getInstance().getValues("proxy");
        if (proxySettings != null && proxySettings.size() > 0) {
            String port = proxySettings.get("proxy.port");
            String host = proxySettings.get("proxy.host");
            if (host != null && !host.isEmpty()) {
                NetUtils.setProxy(proxySettings.get("proxy.type"),
                                  proxySettings.get("proxy.host"),
                                  port == null || port.isEmpty() ? 0 : Integer.parseInt(port),
                                  proxySettings.get("proxy.user"),
                                  proxySettings.get("proxy.password"));
            }
        }
        this.registeredSources = new HashMap<>();
        this.registry = ServiceRegistryManager.getInstance().getServiceRegistry(DataSource.class);
        this.additionalDatasources = new HashMap<>();
        initializeSources();
    }

    private void initializeSources() {
        this.registeredSources.clear();
        final Set<DataSource> services = this.registry.getServices();
        services.forEach(ds -> {
            final String[] sensors = ds.getSupportedSensors();
            final String dsName = ds.getId();
            for (String sensor : sensors) {
                Map.Entry<String, String> key = new AbstractMap.SimpleEntry<>(sensor, dsName);
                if (!this.registeredSources.containsKey(key)) {
                    if (this.filteredParameters != null && this.filteredParameters.containsKey(key.getValue())) {
                        ds.setFilteredParameters(this.filteredParameters.get(key.getValue()));
                    }
                    final Map<String, Map<String, DataSourceParameter>> parameters = ds.getSupportedParameters();
                    this.registeredSources.put(key, parameters.get(sensor));
                }
            }
        });
    }

    public void registerDataSource(DataSource<?, ?> ds) {
        unregisterDataSource(ds.getId());
        final String[] sensors = ds.getSupportedSensors();
        final String dsName = ds.getId();
        for (String sensor : sensors) {
            Map.Entry<String, String> key = new AbstractMap.SimpleEntry<>(sensor, dsName);
            //if (!this.registeredSources.containsKey(key)) {
                if (this.filteredParameters != null && this.filteredParameters.containsKey(key.getValue())) {
                    ds.setFilteredParameters(this.filteredParameters.get(key.getValue()));
                }
                final Map<String, Map<String, DataSourceParameter>> parameters = ds.getSupportedParameters();
                this.registeredSources.put(key, parameters.get(sensor));
                if (this.registry.getService(dsName) == null) {
                    this.additionalDatasources.put(dsName, ds);
                }
            //}
        }
    }

    public void unregisterDataSource(String dsName) {
        this.registeredSources.entrySet().removeIf(entry -> entry.getKey().getValue().equals(dsName));
        this.additionalDatasources.remove(dsName);
    }

    public void setFilteredParameters(Map<Map.Entry<String, String>, Map<String, Map<String, String>>> filter) {
        // Map<Datasource, Map<Sensor, Map<Parameter, Map<ValueSetEntry, ValueSetFriendlyValue>>>>
        this.filteredParameters = new HashMap<>();
        for (Map.Entry<String, String> entry : filter.keySet()) {
            if (this.registeredSources.containsKey(entry)) {
                filteredParameters.computeIfAbsent(entry.getKey(), k -> new HashMap<>());
                filteredParameters.get(entry.getKey()).put(entry.getValue(), filter.get(entry));
            }
        }
        initializeSources();
    }

    /**
     * Returns all the registered (detected) data sources
     */
    public Set<DataSource<?, ?>> getRegisteredDataSources() {
        final Set<DataSource<?, ?>> services = this.registry.getServices().stream().map(d -> (DataSource<?, ?>) d).collect(Collectors.toSet());
        for (DataSource<?, ?> dataSource : services) {
            if (this.filteredParameters != null && this.filteredParameters.containsKey(dataSource.getId())) {
                dataSource.setFilteredParameters(this.filteredParameters.get(dataSource.getId()));
            }
        }
        Set<DataSource<?, ?>> sources = new HashSet<>(services);
        sources.addAll(this.additionalDatasources.values());
        return sources;
    }

    /**
     * Returns the list of supported (detected) sensors (satellites)
     */
    public SortedSet<String> getSupportedSensors() {
        return this.registeredSources.keySet().stream().map(Map.Entry::getKey).collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Returns the name of the first data source associated with the given sensor,
     * or <code>null</code> if no data source is associated.
     *
     * @param sensorName    The sensor name
     */
    public String getFirst(String sensorName) {
        return this.registeredSources.keySet().stream()
                .filter(e -> e.getKey().equals(sensorName)).findFirst()
                .map(Map.Entry::getValue).orElse(null);
    }

    /**
     * Returns the names of the data sources associated with the given sensor,
     * or an empty list if no data source is associated.
     *
     * @param sensorName    The sensor name
     */
    public List<String> getNames(String sensorName) {
        return this.registeredSources.keySet().stream()
                .filter(e -> e.getKey().equals(sensorName))
                .map(Map.Entry::getValue).collect(Collectors.toList());
    }

    /**
     * Returns an instance of the first data source associated with the given sensor,
     * or null if no data source is associated.
     *
     * @param sensorName    The name of the sensor
     */
    public DataSource<?, ?> get(String sensorName) {
        DataSource<?, ?> dataSource = null;
        String firstName = getFirst(sensorName);
        if (firstName != null) {
            dataSource = getInternal(firstName);
            if (this.filteredParameters != null && this.filteredParameters.containsKey(firstName)) {
                dataSource.setFilteredParameters(this.filteredParameters.get(firstName));
            }
        }
        return dataSource;
    }

    /**
     * Returns the (same) instance of the given data source for the given sensor, if the data source
     * is registered.
     *
     * @param sensorName    The sensor name
     * @param dataSourceName     The data source class name
     */
    public DataSource<?, ?> get(String sensorName, String dataSourceName) {
        DataSource<?, ?> dataSource = null;
        if (this.registeredSources.containsKey(new AbstractMap.SimpleEntry<>(sensorName, dataSourceName))) {
            dataSource = getInternal(dataSourceName);
            if (dataSource == null) {
                dataSource = this.additionalDatasources.get(dataSourceName);
            }
            if (this.filteredParameters != null && this.filteredParameters.containsKey(dataSourceName)) {
                dataSource.setFilteredParameters(this.filteredParameters.get(dataSourceName));
            }
        }
        return dataSource;
    }

    /**
     * Returns a new instance of the first data source associated with the given sensor,
     * or null if no data source is associated.
     *
     * @param sensorName    The name of the sensor
     */
    public DataSource<?, ?> createInstance(String sensorName) {
        DataSource<?, ?> dataSource = null;
        String firstName = getFirst(sensorName);
        if (firstName != null) {
            try {
                dataSource = getInternal(firstName).getClass().newInstance();
                if (this.filteredParameters != null && this.filteredParameters.containsKey(firstName)) {
                    dataSource.setFilteredParameters(this.filteredParameters.get(firstName));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dataSource;
    }

    /**
     * Returns a new instance of the given data source for the given sensor, if the data source
     * is registered.
     *
     * @param sensorName    The sensor name
     * @param dataSourceName     The data source class name
     */
    public DataSource<?, ?> createInstance(String sensorName, String dataSourceName) {
        DataSource<?, ?> dataSource = null;
        if (this.registeredSources.containsKey(new AbstractMap.SimpleEntry<>(sensorName, dataSourceName))) {
            try {
                dataSource = getInternal(dataSourceName);
                final Class<? extends DataSource> dataSourceClass = dataSource.getClass();
                Constructor<? extends DataSource> constructor;
                try {
                    constructor = dataSourceClass.getConstructor();
                    dataSource = constructor.newInstance();
                } catch (Exception e) {
                    constructor = dataSourceClass.getConstructor(String.class);
                    dataSource = constructor.newInstance(dataSource.getId());
                }
                if (this.filteredParameters != null && this.filteredParameters.containsKey(dataSourceName)) {
                    dataSource.setFilteredParameters(this.filteredParameters.get(dataSourceName));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return dataSource;
    }

    /**
     * Returns the specific parameters of the given data source, for the given sensor.
     * @param sensorName        The sensor name
     * @param dataSourceName    The data source name
     */
    public Map<String, DataSourceParameter> getSupportedParameters(String sensorName, String dataSourceName) {
        return this.registeredSources.get(new AbstractMap.SimpleEntry<>(sensorName, dataSourceName));
    }

    /**
     * Returns a new instance of the first data source that matches the given remote URL (either the given URL starts
     * with the data source remote root URL or the data source root URL starts with the given URL fragment).
     *
     * @param url   The URL or the URL fragment to be matched.
     * @return      A data source instance if matched or <code>null</code> if not found.
     */
    public DataSource<?, ?> getMatchingDataSource(String url) {
        DataSource<?, ?> match = url != null ? this.registry.getServices().stream().filter(d -> {
            String connectionStringDomain = Utilities.getDomainURL(d.getConnectionString());
            if (url.equals(connectionStringDomain)) {
                return true;
            }
            connectionStringDomain = Utilities.getDomainURL(d.getAlternateConnectionString());
            return url.equals(connectionStringDomain);
        }).findFirst().orElse(null) : null;
        if (match != null) {
            try {
                match = match.getClass().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return match;
    }

    private DataSource<?, ?> getInternal(String name) {
        DataSource dataSource = this.registry.getService(name);
        if (dataSource == null) {
            dataSource = this.additionalDatasources.get(name);
        }
        return dataSource;
    }
}
