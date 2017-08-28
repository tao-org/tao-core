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

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import ro.cs.tao.component.Identifiable;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.converters.ConverterFactory;
import ro.cs.tao.datasource.converters.DateConverter;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.result.json.JsonResponseParser;
import ro.cs.tao.datasource.remote.scihub.json.SciHubJsonResponseHandler;
import ro.cs.tao.datasource.remote.scihub.parameters.SciHubParameterProvider;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class SciHubDataQuery extends DataQuery<EOData> {

    private static final String PATTERN_DATE = "NOW";
    private static final String PATTERN_OFFSET_DATE = PATTERN_DATE + "-%sDAY";
    private static final ConverterFactory converterFactory = ConverterFactory.getInstance();

    static {
        converterFactory.register(SciHubPolygonConverter.class, Polygon2D.class);
        converterFactory.register(DateConverter.class, Date.class);
    }

    SciHubDataQuery(SciHubDataSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        List<EOData> results = new ArrayList<>();
        String query = "";
        int idx = 0;
        for (Map.Entry<String, QueryParameter> entry : this.parameters.entrySet()) {
            QueryParameter parameter = entry.getValue();
            if (!parameter.isOptional() && !parameter.isInterval() && parameter.getValue() == null) {
                throw new QueryException(String.format("Parameter [%s] is required but no value is supplied", parameter.getName()));
            }
            if (parameter.isOptional() &
                    ((!parameter.isInterval() & parameter.getValue() == null) |
                            (parameter.isInterval() & parameter.getMinValue() == null & parameter.getMaxValue() == null))) {
                continue;
            }
            if (idx > 0) {
                query += " AND ";
            }
            if (parameter.getType().isArray()) {
                query += "(";
                Object value = parameter.getValue();
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    query += Array.get(value, i).toString();
                    if (i < length - 1) {
                        query += " OR ";
                    }
                }
                if (length > 1) {
                    query = query.substring(0, query.length() - 4);
                }
                query += ")";
            } else if (Date.class.equals(parameter.getType())) {
                query += entry.getKey() + ":[";
                try {
                    query += converterFactory.create(parameter).stringValue();
                } catch (ConversionException e) {
                    throw new QueryException(e.getMessage());
                }
                query += "]";
            /*} else if (Polygon2D.class.equals(parameter.getType())) {
                query += entry.getKey() + ":Intersects(";
                try {
                    query += converterFactory.create(parameter).stringValue();
                } catch (ConversionException e) {
                    throw new QueryException(e.getMessage());
                }
                query += ")";*/
            } else {
                query += entry.getKey() + ":";
                try {
                    query += converterFactory.create(parameter).stringValue();
                } catch (ConversionException e) {
                    throw new QueryException(e.getMessage());
                }
            }
            idx++;
        }
        if (this.limit <= 0) {
            this.limit = DEFAULT_LIMIT;
        }
        if (this.pageSize <= 0) {
            this.pageSize = Math.min(this.limit, DEFAULT_LIMIT);
        }
        int page = Math.max(this.pageNumber, 0);
        int retrieved = 0;
        do {
            List<EOProduct> tmpResults;
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("filter", query));
            params.add(new BasicNameValuePair("limit", String.valueOf(this.pageSize)));
            params.add(new BasicNameValuePair("offset", page > 0 ? String.valueOf((page - 1) * this.pageSize) : "0"));

            String queryUrl = this.source.getConnectionString() + "?"
                    + URLEncodedUtils.format(params, "UTF-8").replace("+", "%20");
            try (CloseableHttpResponse response = NetUtils.openConnection(queryUrl, this.source.getCredentials())) {
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        JsonResponseParser<EOProduct> parser = new JsonResponseParser<>();
                        tmpResults = parser.parse(EntityUtils.toString(response.getEntity()),
                                                              new SciHubJsonResponseHandler());
                        if (tmpResults != null) {
                            retrieved = tmpResults.size();
                            if ("Sentinel-2".equals(this.parameters.get("platformName").getValue()) &&
                                    this.parameters.containsKey("cloudcoverpercentage")) {
                                final Double clouds = (Double) this.parameters.get("cloudcoverpercentage").getValue();
                                tmpResults = tmpResults.stream()
                                        .filter(r -> Double.parseDouble(r.getAttributeValue("Cloud cover percentage")) <= clouds)
                                        .collect(Collectors.toList());
                            }
                            results.addAll(tmpResults);
                            page++;
                        }
                        break;
                    case 401:
                        throw new QueryException("The supplied credentials are invalid!");
                    default:
                        throw new QueryException(String.format("The request was not successful. Reason: %s",
                                                               response.getStatusLine().getReasonPhrase()));
                }
            } catch (IOException ex) {
                throw new QueryException(ex);
            }
        } while (this.pageNumber <= 0 && (retrieved > 0 && results.size() <= this.limit));
        if (results.size() > this.limit) {
            return results.subList(0, this.limit);
        } else {
            return results;
        }
    }

    @Override
    public String defaultName() { return "SciHubQuery"; }

    @Override
    public Identifiable copy() {
        SciHubDataSource src = (SciHubDataSource) this.source;
        SciHubParameterProvider parameterProvider = (SciHubParameterProvider) src.getParameterProvider(null);
        SciHubDataQuery copy = new SciHubDataQuery(src, parameterProvider);

        return copy;
    }
}
