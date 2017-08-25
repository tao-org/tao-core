/*
 *
 *  * Copyright (C) 2017 CS ROMANIA
 *  *
 *  * This program is free software; you can redistribute it and/or modify it
 *  * under the terms of the GNU General Public License as published by the Free
 *  * Software Foundation; either version 3 of the License, or (at your option)
 *  * any later version.
 *  * This program is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 *  * more details.
 *  *
 *  * You should have received a copy of the GNU General Public License along
 *  * with this program; if not, see http://www.gnu.org/licenses/
 *  *
 *
 */

package ro.cs.tao.datasource.local;

import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.eodata.EOData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class LocalDataSource extends AbstractDataSource<EOData, LocalDataQuery> {

    protected final Logger logger;

    public LocalDataSource(String connectionString) {
        super(connectionString);
        this.logger = Logger.getLogger(LocalDataSource.class.getName());
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            this.logger.severe("PostgreSQL driver not registered");
        }
        addParameterProvider(null, new DatabaseParameterProvider());
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
    protected LocalDataQuery createQueryImpl(String code) {
        return new LocalDataQuery(this, getParameterProvider(null));
    }

    Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(this.connectionString);
        } catch (SQLException e) {
            this.logger.warning(e.getMessage());
        }
        return connection;
    }
}
