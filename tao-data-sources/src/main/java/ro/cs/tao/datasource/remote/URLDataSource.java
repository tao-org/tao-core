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

package ro.cs.tao.datasource.remote;

import org.apache.commons.lang.NotImplementedException;
import ro.cs.tao.datasource.AbstractDataSource;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.eodata.EOData;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Cosmin Cara
 */
public abstract class URLDataSource<R extends EOData, Q extends DataQuery<R>>
        extends AbstractDataSource<R, Q> {
    protected URI remoteUrl;

    public URLDataSource(String connectionString) throws URISyntaxException {
        super(connectionString);
        this.remoteUrl = new URI(this.connectionString);
    }

    @Override
    public String getConnectionString() {
        return this.remoteUrl != null ? this.remoteUrl.toString() : null;
    }

    @Override
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
        try {
            this.remoteUrl = new URI(this.connectionString);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean ping() {
        if (this.remoteUrl == null) {
            return false;
        }
        try (Socket socket = new Socket()) {
            socket.setSoTimeout((int) this.timeout);
            socket.connect(new InetSocketAddress(this.remoteUrl.getHost(), this.remoteUrl.getPort()));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    @Override
    public void close() {
        // do nothing here
    }

    @Override
    public String defaultName() { return "NewURLDataSource"; }

    @Override
    public URLDataSource<R, Q> copy() {
        throw new NotImplementedException("This should not be called on this instance");
    }
}
