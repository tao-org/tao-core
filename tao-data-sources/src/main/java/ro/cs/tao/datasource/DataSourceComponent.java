package ro.cs.tao.datasource;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.notifications.ProgressNotifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataSourceComponent")
public class DataSourceComponent extends TaoComponent {

    @XmlElement
    private String sensorName;
    @XmlElement
    private String dataSourceName;
    @XmlTransient
    private String userName;
    @XmlTransient
    private String password;

    public DataSourceComponent(String sensorName, String dataSourceName) {
        if (sensorName == null) {
            throw new IllegalArgumentException("Parameter [sensorName] must not be null");
        }
        this.sensorName = sensorName;
        this.dataSourceName = dataSourceName;
    }

    private DataSourceComponent() { }

    @Override
    public String defaultName() { return "NewDatasource"; }

    public void setUserCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public DataQuery createQuery() {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.dataSourceName != null ?
                dsManager.get(this.sensorName, this.dataSourceName) : dsManager.get(this.sensorName);
        return dataSource.createQuery(this.sensorName);
    }

    public List<EOProduct> doQuery(List<QueryParameter> parameters) throws QueryException {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.dataSourceName != null ?
                dsManager.get(this.sensorName, this.dataSourceName) : dsManager.get(this.sensorName);
        dataSource.setCredentials(this.userName, this.password);
        final DataQuery query = dataSource.createQuery(this.sensorName);
        if (parameters != null) {
            parameters.forEach(query::addParameter);
        }
        return query.execute();
    }

    public List<EOProduct> doFetch(List<EOProduct> products) {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.dataSourceName != null ?
                dsManager.get(this.sensorName, this.dataSourceName) : dsManager.get(this.sensorName);
        ProgressNotifier notifier = new ProgressNotifier(this.id);
        notifier.started(String.format("Download-%s", this.id));
        int counter = 1;
        for (EOProduct product : products) {
            try {
                ProductFetchStrategy fetchStrategy = dataSource.getProductFetchStrategy(product.getProductType());
                if (fetchStrategy instanceof DownloadStrategy) {
                    ((DownloadStrategy) fetchStrategy).setProgressListener(notifier);
                }
                Path productPath = fetchStrategy.fetch(product);
                if (productPath != null) {
                    product.setLocation(productPath.toUri().toString());
                }
            } catch (IOException ex) {
                Logger.getLogger(DataSourceComponent.class.getSimpleName()).warning(
                        String.format("Fetching product '%s' failed: %s", product.getName(), ex.getMessage()));
            } catch (URISyntaxException e) {
                Logger.getLogger(DataSourceComponent.class.getSimpleName()).warning(
                        String.format("Updating product location for '%s' failed: %s", product.getName(), e.getMessage()));
            } finally {
                notifier.notifyProgress(counter++ / products.size());
            }
        }
        notifier.ended();
        return products;
    }

    @Override
    public DataSourceComponent clone() throws CloneNotSupportedException {
        DataSourceComponent newComponent = (DataSourceComponent) super.clone();
        newComponent.sensorName = this.sensorName;
        newComponent.dataSourceName = this.dataSourceName;
        return newComponent;
    }
}
