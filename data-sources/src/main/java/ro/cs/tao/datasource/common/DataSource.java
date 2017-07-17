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
 *
 */
package ro.cs.tao.datasource.common;

import org.apache.http.auth.UsernamePasswordCredentials;
import ro.cs.tao.eodata.EOData;

/**
 * Abstraction for a product datasource source.
 *
 * @author Cosmin Cara
 */
public abstract class DataSource<R extends EOData, Q extends DataQuery<R>> {
    protected String connectionString;
    protected long timeout;
    protected UsernamePasswordCredentials credentials;

    public DataSource() { this.timeout = 10000; }

    public DataSource(String connectionString) {
        this();
        this.connectionString = connectionString;
    }

    public long getTimeout() { return this.timeout; }

    public void setTimeout(long value) { this.timeout = value; }

    public String getConnectionString() { return connectionString; }

    public void setConnectionString(String connectionString) { this.connectionString = connectionString; }

    public void setCredentials(String username, String password) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Invalid user");
        }
        this.credentials = new UsernamePasswordCredentials(username, password);
    }

    public UsernamePasswordCredentials getCredentials() { return this.credentials; }

    /**
     * Tests that the datasource source is reachable.
     * Must return <code>true</code> if the source is reachable, <code>false</code> otherwise.
     *
     */
    public abstract boolean ping();

    /**
     * Closes the datasource source connection.
     */
    public abstract void close();

    /**
     * Creates a query object that will be executed against the datasource source to retrieve results.
     */
    public abstract Q createQuery();
}
