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

package ro.cs.tao.datasource.opensearch;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import ro.cs.tao.datasource.ProductFetchStrategy;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.opensearch.model.DescriptionParser;
import ro.cs.tao.datasource.opensearch.model.OpenSearchEndpoint;
import ro.cs.tao.datasource.opensearch.model.OpenSearchService;
import ro.cs.tao.datasource.param.DataSourceParameter;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.util.HttpMethod;
import ro.cs.tao.datasource.util.NetUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class OpenSearchParameterProvider implements ParameterProvider {

    protected String[] sensors;
    protected Map<String, Map<String, DataSourceParameter>> parameters;
    protected Map<String, ProductFetchStrategy> productFetchers;

    protected OpenSearchService searchService;
    private String url;

    public OpenSearchParameterProvider(String url) {
        this.url = url;
    }

    @Override
    public Map<String, Map<String, DataSourceParameter>> getSupportedParameters() {
        if (this.searchService == null) {
            initialize();
        }
        return this.parameters;
    }

    @Override
    public String[] getSupportedSensors() {
        if (this.searchService == null) {
            initialize();
        }
        return this.sensors;
    }

    protected abstract String sensorParameterName();

    private void initialize() {
        this.searchService = parseDescription(this.url);
        OpenSearchEndpoint endpoint = this.searchService.getEndpoint("application/atom+xml");
        if (endpoint != null) {
            Map<String, DataSourceParameter> parameters = endpoint.getParameters();
            DataSourceParameter sensorParam = parameters.get(sensorParameterName());
            this.parameters = new HashMap<>();
            Object[] values = sensorParam.getValueSet();
            Map<String, DataSourceParameter> params = new HashMap<>(parameters);
            params.remove(sensorParameterName());
            if (values != null) {
                this.sensors = new String[values.length];
                for (int i = 0; i < this.sensors.length; i++) {
                    this.sensors[i] = String.valueOf(values[i]);
                    this.parameters.put(this.sensors[i], params);
                }
            }
        }
    }

    private OpenSearchService parseDescription(String url) {
        try (CloseableHttpResponse response = NetUtils.openConnection(HttpMethod.GET, url, null)) {
            switch (response.getStatusLine().getStatusCode()) {
                case 200:
                    DescriptionParser parser = new DescriptionParser();
                    return parser.parse(EntityUtils.toString(response.getEntity()));
                case 401:
                    throw new QueryException("The supplied credentials are invalid!");
                default:
                    throw new QueryException(String.format("The request was not successful. Reason: %s",
                                                           response.getStatusLine().getReasonPhrase()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
