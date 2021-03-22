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
import ro.cs.tao.utils.HttpMethod;
import ro.cs.tao.utils.NetUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class OpenSearchParameterProvider implements ParameterProvider {

    protected String[] sensors;
    protected Map<String, Map<String, DataSourceParameter>> parameters;
    protected Map<String, ProductFetchStrategy> productFetchers;

    private OpenSearchService searchService;
    private String url;
    protected String endpointType;

    public OpenSearchParameterProvider(String url) {
        this.url = url;
        this.endpointType = "application/atom+xml";
    }

    public OpenSearchParameterProvider(String url, String endpointType) {
        this.url = url;
        this.endpointType = endpointType;
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
        List<OpenSearchEndpoint> endpoints = this.searchService.getEndpoints(this.endpointType);
        if (endpoints != null) {
            for (OpenSearchEndpoint endpoint : endpoints) {
                Map<String, DataSourceParameter> parameters = endpoint.getParameters();
                DataSourceParameter parameter = parameters.values().stream()
                        .filter(v -> v.getRemoteName().equals(sensorParameterName())).findFirst().orElse(null);
                String sensorParamName = parameter != null ? parameter.getName() : null;
                DataSourceParameter sensorParam = parameters.get(sensorParamName);
                this.parameters = new HashMap<>();
                if (sensorParamName != null) {
                    Object[] values = sensorParam.getValueSet();
                    parameters.remove(sensorParamName);
                    if (values != null) {
                        this.sensors = new String[values.length];
                        for (int i = 0; i < this.sensors.length; i++) {
                            this.sensors[i] = String.valueOf(values[i]);
                            this.parameters.put(this.sensors[i], new LinkedHashMap<>(parameters));
                        }
                    }
                    break;
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
            throw new QueryException(e);
        }
    }
}
