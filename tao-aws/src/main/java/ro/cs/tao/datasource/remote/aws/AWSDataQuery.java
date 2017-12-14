package ro.cs.tao.datasource.remote.aws;

import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSource;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOProduct;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class AWSDataQuery extends DataQuery {
    private DataQuery innerQuery;

    AWSDataQuery(DataSource source, String sensorName) {
        super(source, sensorName);
        switch(sensorName) {
            case "Sentinel2":
                this.innerQuery = new Sentinel2Query(source);
                break;
            case "Landsat8":
                this.innerQuery = new Landsat8Query(source);
                break;
            default:
                this.innerQuery = null;
                break;
        }
    }

    @Override
    public QueryParameter addParameter(QueryParameter parameter) {
        return innerQuery.addParameter(parameter);
    }

    @Override
    public <V> QueryParameter<V> addParameter(String name, Class<V> type) {
        return innerQuery.addParameter(name, type);
    }

    @Override
    public <V> QueryParameter<V> addParameter(String name, Class<V> type, V value) {
        return innerQuery.addParameter(name, type, value);
    }

    @Override
    public <V> QueryParameter<V> addParameter(String name, V value) {
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
    public <V> QueryParameter<V> createParameter(String name, Class<V> type) {
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
    public List<EOProduct> execute() throws QueryException {
        return innerQuery.execute();
    }

    @Override
    public Map<String, ParameterDescriptor> getSupportedParameters() {
        return innerQuery.getSupportedParameters();
    }

    @Override
    protected List<EOProduct> executeImpl() throws QueryException {
        return innerQuery != null ? innerQuery.execute() : new ArrayList<>();
    }

    @Override
    public String defaultName() {
        return "AWSQuery";
    }

    @Override
    public AWSDataQuery clone() throws CloneNotSupportedException {
        AWSDataQuery query = (AWSDataQuery) super.clone();
        query.innerQuery = this.innerQuery.clone();
        return query;
    }
}
