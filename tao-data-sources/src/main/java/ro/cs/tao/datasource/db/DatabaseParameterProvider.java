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
package ro.cs.tao.datasource.db;

import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.eodata.Polygon2D;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.eodata.enums.SensorType;
import ro.cs.tao.spi.ServiceRegistry;
import ro.cs.tao.spi.ServiceRegistryManager;

import java.util.*;

/**
 * @author Cosmin Cara
 */
public class DatabaseParameterProvider implements ParameterProvider {

    private final DatabaseSource source;

    DatabaseParameterProvider(DatabaseSource source) {
        this.source = source;
    }

    @Override
    public Map<String, Map<String, ParameterDescriptor>> getSupportedParameters() {
        String[] sensors = getSupportedSensors();
        return Collections.unmodifiableMap(
                new HashMap<String, Map<String, ParameterDescriptor>>() {{
                    for (String sensor : sensors) {
                        put(sensor, new HashMap<String, ParameterDescriptor>() {{
                            put("name", new ParameterDescriptor("name", String.class, false));
                            put("type_id", new ParameterDescriptor("type_id", DataFormat.class, false));
                            put("geometry", new ParameterDescriptor("geometry", Polygon2D.class, false));
                            put("coordinate_reference_system", new ParameterDescriptor("coordinate_reference_system", String.class, false));
                            put("sensor_type_id", new ParameterDescriptor("sensor_type_id", SensorType.class, false));
                            put("acquisition_date", new ParameterDescriptor("acquisition_date", Date.class, false));
                            put("product_type", new ParameterDescriptor("product_type", String.class, sensor));
                        }});
                    }
                }});
    }

    @Override
    public String[] getSupportedSensors() {
        Set<String> sensors = new HashSet<>();
        ServiceRegistry<DataSource> serviceRegistry = ServiceRegistryManager.getInstance().getServiceRegistry(DataSource.class);
        Set<DataSource> services = serviceRegistry.getServices();
        for (DataSource service : services) {
            if (!service.getClass().equals(this.source.getClass())) {
                Collections.addAll(sensors, service.getSupportedSensors());
            }
        }
        /*List<String> sensors = new ArrayList<>();
        Connection sqlConnection = this.source.getConnection();
        if (sqlConnection != null) {
            try {
                PreparedStatement statement = sqlConnection.prepareStatement("SELECT DISTINCT product_type FROM " + DatabaseSource.PRODUCTS_TABLE);
                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    sensors.add(resultSet.getString(0));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    sqlConnection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }*/
        return sensors.toArray(new String[sensors.size()]);
    }

    @Override
    public Map<String, ProductFetchStrategy> getRegisteredProductFetchStrategies() {
        String[] sensors = getSupportedSensors();
        return Collections.unmodifiableMap(
                new HashMap<String, ProductFetchStrategy>() {{
                    for (String sensor : sensors) {
                        put(sensor, new DatabaseFetchStrategy(DatabaseParameterProvider.this.source));
                    }
                }});
    }
}
