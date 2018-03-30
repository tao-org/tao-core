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

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.datasource.InterruptedException;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.eodata.EOProduct;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseFetchStrategy implements ProductFetchStrategy {
    private final DatabaseSource source;

    DatabaseFetchStrategy(DatabaseSource source) {
        this.source = source;
    }

    @Override
    public void setCredentials(UsernamePasswordCredentials credentials) {
        // no-op method
    }

    @Override
    public Path fetch(EOProduct product) throws InterruptedException {
        Path productPath = null;
        Connection sqlConnection = this.source.getConnection();
        if (sqlConnection != null) {
            try {
                PreparedStatement statement =
                        sqlConnection.prepareStatement("SELECT location FROM " + DatabaseSource.PRODUCTS_TABLE +
                        " WHERE name = ?");
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    String fieldValue = resultSet.getString(0);
                    if (fieldValue != null) {
                        productPath = Paths.get(fieldValue);
                    }
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
        }
        return productPath;
    }

    @Override
    public DatabaseFetchStrategy clone() {
        return new DatabaseFetchStrategy(this.source);
    }
}
