package ro.cs.tao.datasource;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOProduct;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
public class DataSourceComponent extends TaoComponent {

    private String sensorName;
    private String sourceClassName;

    public DataSourceComponent(String sensorName, String dataSourceClassName) {
        if (sensorName == null) {
            throw new IllegalArgumentException("Parameter [sensorName] must not be null");
        }
        this.sensorName = sensorName;
        this.sourceClassName = dataSourceClassName;
    }

    private DataSourceComponent() { }

    @Override
    public String defaultName() { return "NewDatasource"; }

    public List<EOProduct> doQuery(List<QueryParameter> parameters) throws QueryException {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.sourceClassName != null ?
                dsManager.get(this.sensorName, this.sourceClassName) : dsManager.get(this.sensorName);
        final DataQuery query = dataSource.createQuery(this.sensorName);
        if (parameters != null) {
            parameters.forEach(query::addParameter);
        }
        return query.execute();
    }

    public void doFetch(List<EOProduct> products) {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.sourceClassName != null ?
                dsManager.get(this.sensorName, this.sourceClassName) : dsManager.get(this.sensorName);
        for (EOProduct product : products) {
            try {
                ProductFetchStrategy fetchStrategy = dataSource.getProductFetchStrategy(product.getProductType());
                fetchStrategy.fetch(product);
            } catch (IOException ex) {
                Logger.getLogger(DataSourceComponent.class.getSimpleName()).warning(
                        String.format("Fetching product '%s' failed: %s", product.getName(), ex.getMessage()));
            }
        }
    }

    @Override
    public DataSourceComponent clone() throws CloneNotSupportedException {
        DataSourceComponent newComponent = (DataSourceComponent) super.clone();
        newComponent.sensorName = this.sensorName;
        newComponent.sourceClassName = this.sourceClassName;
        return newComponent;
    }
}
