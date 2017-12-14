package ro.cs.tao.datasource.remote.peps;

import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.converters.ConversionException;
import ro.cs.tao.datasource.converters.ConverterFactory;
import ro.cs.tao.datasource.converters.RangeConverter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.peps.parameters.BooleanConverter;
import ro.cs.tao.datasource.remote.peps.parameters.DateConverter;
import ro.cs.tao.datasource.remote.peps.parameters.PolygonConverter;
import ro.cs.tao.datasource.remote.result.json.JsonResponseParser;
import ro.cs.tao.datasource.util.NetUtils;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.Polygon2D;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Cosmin Cara
 */
public class PepsDataQuery extends DataQuery {
    private static final ConverterFactory converterFactory = ConverterFactory.getInstance();

    static {
        converterFactory.register(PolygonConverter.class, Polygon2D.class);
        converterFactory.register(DateConverter.class, Date.class);
        converterFactory.register(RangeConverter.class, Double.class);
        converterFactory.register(BooleanConverter.class, Boolean.class);
    }

    private PepsDataQuery() { super(); }

    PepsDataQuery(DataSource source, String sensorName) {
        super(source, sensorName);
    }

    @Override
    protected List<EOProduct> executeImpl() {
        List<EOProduct> results = new ArrayList<>();
        List<NameValuePair> params = new ArrayList<>();
        Collection collection = Collection.S2ST;
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
            try {
                if (!"collection".equals(parameter.getName())) {
                    params.add(new BasicNameValuePair(parameter.getName(),
                                                      converterFactory.create(parameter).stringValue()));
                } else {
                    collection = Enum.valueOf(Collection.class, parameter.getValueAsString());
                }
            } catch (ConversionException e) {
                throw new QueryException(e.getMessage());
            }
        }
        if (this.limit <= 0) {
            this.limit = DEFAULT_LIMIT;
        }
        if (this.pageSize <= 0) {
            this.pageSize = Math.min(this.limit, DEFAULT_LIMIT);
        }
        int page = Math.max(this.pageNumber, 1);
        int retrieved = 0;
        do {
            List<EOProduct> tmpResults;
            List<NameValuePair> queryParams = new ArrayList<>(params);
            queryParams.add(new BasicNameValuePair("maxRecords", String.valueOf(this.pageSize)));
            queryParams.add(new BasicNameValuePair("page", String.valueOf(page)));

            String queryUrl = this.source.getConnectionString() + collection.toString() + "/search.json?"
                    + URLEncodedUtils.format(queryParams, "UTF-8").replace("+", "%20");
            try (CloseableHttpResponse response = NetUtils.openConnection(queryUrl, this.source.getCredentials())) {
                switch (response.getStatusLine().getStatusCode()) {
                    case 200:
                        JsonResponseParser<EOProduct> parser = new JsonResponseParser<EOProduct>(new PepsQueryResponseHandler()) {
                            @Override
                            public String[] getExcludedAttributes() {
                                return new String[] { "keywords", "links", "services" };
                            }
                        };
                        tmpResults = parser.parse(EntityUtils.toString(response.getEntity()));
                        if (tmpResults != null) {
                            retrieved = tmpResults.size();
                            if ("Sentinel2".equals(this.parameters.get("platform").getValue()) &&
                                    this.parameters.containsKey("cloudCover")) {
                                final Double clouds = (Double) this.parameters.get("cloudCover").getValue();
                                tmpResults = tmpResults.stream()
                                        .filter(r -> Double.parseDouble(r.getAttributeValue("cloudCover")) <= clouds)
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
        } while (this.pageNumber <= 1 && (retrieved > 0 && results.size() <= this.limit));
        if (results.size() > this.limit) {
            return results.subList(0, this.limit);
        } else {
            return results;
        }
    }

    @Override
    public String defaultName() {
        return "PepsQuery";
    }

}
