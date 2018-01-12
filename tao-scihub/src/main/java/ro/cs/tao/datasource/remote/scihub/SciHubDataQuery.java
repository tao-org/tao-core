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
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.converters.ConverterFactory;
import ro.cs.tao.datasource.converters.DateConverter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.result.ResponseParser;
import ro.cs.tao.datasource.remote.result.json.JsonResponseParser;
import ro.cs.tao.datasource.remote.result.xml.XmlResponseParser;
import ro.cs.tao.datasource.remote.scihub.json.SciHubJsonResponseHandler;
import ro.cs.tao.datasource.remote.scihub.xml.SciHubXmlResponseHandler;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class SciHubDataQuery extends DataQuery {

    private static final String PATTERN_DATE = "NOW";
    private static final String PATTERN_OFFSET_DATE = PATTERN_DATE + "-%sDAY";
    private static final ConverterFactory converterFactory = ConverterFactory.getInstance();

    private final String sensorName;
    private final Logger logger = Logger.getLogger(SciHubDataQuery.class.getSimpleName());

    static {
        converterFactory.register(SciHubPolygonConverter.class, Polygon2D.class);
        converterFactory.register(DateConverter.class, Date.class);
    }

    SciHubDataQuery(SciHubDataSource source, String sensorName) {
        super(source, sensorName);
        this.sensorName = sensorName;
    }

    @Override
    protected List<EOProduct> executeImpl() throws QueryException {
        List<EOProduct> results = new ArrayList<>();
        String query = buildQueryParameters();
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
            params.add(new BasicNameValuePair("q", query));
            params.add(new BasicNameValuePair("rows", String.valueOf(this.pageSize)));
            params.add(new BasicNameValuePair("start", page > 0 ? String.valueOf((page - 1) * this.pageSize) : "0"));

            String queryUrl = this.source.getConnectionString() + "?"
              + URLEncodedUtils.format(params, "UTF-8").replace("+", "%20");
            try (CloseableHttpResponse response = NetUtils.openConnection(queryUrl, this.source.getCredentials())) {
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        String rawResponse = EntityUtils.toString(response.getEntity());
                        ResponseParser<EOProduct> parser;
                        boolean isXml = rawResponse.startsWith("<?xml");
                        if (isXml) {
                            parser = new XmlResponseParser<>();
                            ((XmlResponseParser) parser).setHandler(new SciHubXmlResponseHandler("entry"));
                        } else {
                            parser = new JsonResponseParser<>(new SciHubJsonResponseHandler());
                        }
                        tmpResults = parser.parse(rawResponse);
                        if (tmpResults != null) {
                            retrieved = tmpResults.size();
                            if ("Sentinel2".equals(this.sensorName) &&
                              this.parameters.containsKey("cloudcoverpercentage")) {
                                final Double clouds = (Double) this.parameters.get("cloudcoverpercentage").getValue();
                                tmpResults = tmpResults.stream()
                                  .filter(r -> Double.parseDouble(r.getAttributeValue(isXml ? "cloudcoverpercentage" : "Cloud Cover Percentage")) <= clouds)
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
        } while (this.pageNumber <= 0 && (retrieved > 0 && results.size() == this.limit));
        logger.info(String.format("Query returned %s products", results.size()));
        if (results.size() > this.limit) {
            return results.subList(0, this.limit);
        } else {
            return results;
        }
    }

    @Override
    public long getCount() {
        long count = -1;
        String query = buildQueryParameters();
        final String countUrl = this.source.getProperty("scihub.search.count.url");
        if (countUrl != null) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("filter", query));
            String queryUrl = countUrl + "?" + URLEncodedUtils.format(params, "UTF-8").replace("+", "%20");
            try (CloseableHttpResponse response = NetUtils.openConnection(queryUrl, this.source.getCredentials())) {
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        String rawResponse = EntityUtils.toString(response.getEntity());
                        count = Long.parseLong(rawResponse);
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
        }
        return count;
    }

    @Override
    public String defaultName() { return "SciHubQuery"; }

    private String buildQueryParameters() {
        StringBuilder query = new StringBuilder();
        int idx = 0;
        if (!this.parameters.containsKey("platformName")) {
            addParameter("platformName", this.sensorName);
        }
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
                query.append(" AND ");
            }
            if (parameter.getType().isArray()) {
                query.append("(");
                Object value = parameter.getValue();
                int length = Array.getLength(value);
                for (int i = 0; i < length; i++) {
                    query.append(Array.get(value, i).toString());
                    if (i < length - 1) {
                        query.append(" OR ");
                    }
                }
                if (length > 1) {
                    query = new StringBuilder(query.substring(0, query.length() - 4));
                }
                query.append(")");
            } else if (Date.class.equals(parameter.getType())) {
                query.append(entry.getKey()).append(":[");
                try {
                    query.append(converterFactory.create(parameter).stringValue());
                } catch (ConversionException e) {
                    throw new QueryException(e.getMessage());
                }
                query.append("]");
            } else {
                query.append(entry.getKey()).append(":");
                try {
                    query.append(converterFactory.create(parameter).stringValue());
                } catch (ConversionException e) {
                    throw new QueryException(e.getMessage());
                }
            }
            idx++;
        }
        return query.toString();
    }
}
