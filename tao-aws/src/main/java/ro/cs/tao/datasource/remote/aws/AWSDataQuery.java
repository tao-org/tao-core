package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.ParameterProvider;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.aws.parameters.LandsatParameterProvider;
import ro.cs.tao.datasource.remote.aws.parameters.Sentinel2ParameterProvider;
import ro.cs.tao.eodata.EOData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class AWSDataQuery extends DataQuery<EOData> {
    private DataQuery<EOData> innerQuery;

    AWSDataQuery(AWSDataSource source, ParameterProvider parameterProvider) {
        super(source, parameterProvider);
        if (parameterProvider != null) {
            if (parameterProvider instanceof Sentinel2ParameterProvider) {
                innerQuery = new Sentinel2Query(source, parameterProvider);
            } else if (parameterProvider instanceof LandsatParameterProvider) {
                innerQuery = new Landsat8Query(source, parameterProvider);
            }
        }
    }

    @Override
    public QueryParameter addParameter(QueryParameter parameter) {
        return innerQuery.addParameter(parameter);
    }

    @Override
    public <V> QueryParameter addParameter(String name, Class<V> type) {
        return innerQuery.addParameter(name, type);
    }

    @Override
    public <V> QueryParameter addParameter(String name, Class<V> type, V value) {
        return innerQuery.addParameter(name, type, value);
    }

    @Override
    public QueryParameter addParameter(String name, Object value) {
        return innerQuery.addParameter(name, value);
    }

    @Override
    public void setText(String value) {
        innerQuery.setText(value);
    }

    @Override
    public void setPageSize(int value) {
        innerQuery.setPageSize(value);
    }

    @Override
    public void setPageNumber(int value) {
        innerQuery.setPageNumber(value);
    }

    @Override
    public void setMaxResults(int value) {
        innerQuery.setMaxResults(value);
    }

    @Override
    public QueryParameter createParameter(String name, Class<?> type) {
        return innerQuery.createParameter(name, type);
    }

    @Override
    public <V> QueryParameter createParameter(String name, Class<V> type, V value) {
        return innerQuery.createParameter(name, type, value);
    }

    @Override
    public <V> QueryParameter createParameter(String name, Class<V> type, V value, boolean optional) {
        return innerQuery.createParameter(name, type, value, optional);
    }

    @Override
    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue) {
        return innerQuery.createParameter(name, type, minValue, maxValue);
    }

    @Override
    public <V> QueryParameter createParameter(String name, Class<V> type, V minValue, V maxValue, boolean optional) {
        return innerQuery.createParameter(name, type, minValue, maxValue, optional);
    }

    @Override
    public int getParameterCount() {
        return innerQuery.getParameterCount();
    }

    @Override
    public String getText() {
        return innerQuery.getText();
    }

    @Override
    public List<EOData> execute() throws QueryException {
        return innerQuery.execute();
    }

    @Override
    public Map<String, ParameterDescriptor> getSupportedParameters() {
        return innerQuery.getSupportedParameters();
    }

    @Override
    protected List<EOData> executeImpl() throws QueryException {
        return innerQuery != null ? innerQuery.execute() : new ArrayList<>();
    }
}
