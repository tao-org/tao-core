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

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class DatabaseSource extends AbstractDataSource<DatabaseQuery> {
    static final String PRODUCTS_TABLE = "tao.raster_data_product";
    static final String PRODUCT_PARAMS_TABLE = "tao.data_product_attributes";
    private final String dbUser;
    private final String dbPass;
    private final Logger logger;

    public DatabaseSource() {
        super();
        this.connectionString = ConfigurationManager.getInstance().getValue("spring.datasource.url");
        this.dbUser = ConfigurationManager.getInstance().getValue("spring.datasource.username");
        this.dbPass = ConfigurationManager.getInstance().getValue("spring.datasource.password");
        this.logger = Logger.getLogger(DatabaseSource.class.getName());
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            this.logger.severe("PostgreSQL driver not registered");
        }
        setParameterProvider(new DatabaseParameterProvider(this));
    }

    @Override
    public boolean ping() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(this.connectionString);
        } catch (SQLException e) {
            this.logger.warning(e.getMessage());
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    this.logger.warning(e.getMessage());
                }
            }
        }
        return true;
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public String defaultName() { return "Local Database"; }

    @Override
    public DatabaseSource clone() throws CloneNotSupportedException {
        return (DatabaseSource) super.clone();
    }

    @Override
    protected DatabaseQuery createQueryImpl(String sensorName) {
        final ParameterProvider parameterProvider = getParameterProvider();
        if (parameterProvider == null) {
            throw new QueryException("No parameter provider found for this data source");
        }
        final Map<String, Map<String, ParameterDescriptor>> supportedParameters =
                parameterProvider.getSupportedParameters();
        if (supportedParameters == null) {
            throw new QueryException("Parameters not defined for this data source");
        }
        return new DatabaseQuery(this, sensorName);
    }

    Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(this.connectionString, this.dbUser, this.dbPass);
        } catch (SQLException e) {
            this.logger.warning(e.getMessage());
        }
        return connection;
    }
}
