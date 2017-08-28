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

package ro.cs.tao.datasource.remote.scihub;

import ro.cs.tao.datasource.remote.URLDataSource;
import ro.cs.tao.datasource.remote.scihub.parameters.SciHubParameterProvider;
import ro.cs.tao.datasource.util.NetUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Base64;
import java.util.Properties;

/**
 * @author Cosmin Cara
 */
public class SciHubDataSource extends URLDataSource<SciHubDataQuery> {
    private static String URL;

    static {
        Properties props = new Properties();
        try {
            props.load(SciHubDataSource.class.getResourceAsStream("scihub.properties"));
            URL = props.getProperty("scihub.search.url");
        } catch (IOException ignored) {
        }
    }

    public SciHubDataSource() throws URISyntaxException {
        super(URL);
        setParameterProvider(new SciHubParameterProvider());
    }

    @Override
    public String defaultName() { return "Scientific Data Hub"; }

    @Override
    public void setCredentials(String username, String password) {
        super.setCredentials(username, password);
        String authToken = "Basic " + new String(Base64.getEncoder().encode((username + ":" + password).getBytes()));
        NetUtils.setAuthToken(authToken);
    }

    @Override
    protected SciHubDataQuery createQueryImpl(String sensorName) {
        return new SciHubDataQuery(this, sensorName);
    }
}
