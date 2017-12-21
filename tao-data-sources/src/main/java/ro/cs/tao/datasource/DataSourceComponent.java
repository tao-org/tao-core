package ro.cs.tao.datasource;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.messaging.ProgressNotifier;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
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
    @XmlTransient
    private boolean cancelled;
    @XmlTransient
    private ProductFetchStrategy currentFetcher;
    @XmlTransient
    private ProductStatusListener productStatusListener;
    @XmlTransient
    private EOProduct currentProduct;
    @XmlTransient
    private FetchMode fetchMode;

    @XmlTransient
    private final Logger logger;

    public DataSourceComponent(String sensorName, String dataSourceName) {
        if (sensorName == null) {
            throw new IllegalArgumentException("Parameter [sensorName] must not be null");
        }
        this.sensorName = sensorName;
        this.dataSourceName = dataSourceName;
        this.logger = Logger.getLogger(DataSourceComponent.class.getSimpleName());
    }

    private DataSourceComponent() { this.logger = Logger.getLogger(DataSourceComponent.class.getSimpleName()); }

    @Override
    public String defaultName() { return "NewDatasource"; }

    public void setUserCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    public void setFetchMode(FetchMode mode) { this.fetchMode = mode; }

    public void setProductStatusListener(ProductStatusListener listener) {
        this.productStatusListener = listener;
    }

    public EOProduct getCurrentProduct() { return currentProduct; }

    public DataQuery createQuery() {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.dataSourceName != null ?
                dsManager.get(this.sensorName, this.dataSourceName) : dsManager.get(this.sensorName);
        dataSource.setCredentials(this.userName, this.password);
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

    public List<EOProduct> doFetch(List<EOProduct> products, Set<String> tiles, String destinationPath) {
        return doFetch(products, tiles, destinationPath, null);
    }

    public List<EOProduct> doFetch(List<EOProduct> products, Set<String> tiles, String destinationPath, String localRootPath) {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.dataSourceName != null ?
                dsManager.get(this.sensorName, this.dataSourceName) : dsManager.get(this.sensorName);
        if (this.userName != null) {
            dataSource.setCredentials(this.userName, this.password);
        }
        ProgressNotifier notifier = new ProgressNotifier(this, DataSourceTopics.PRODUCT_PROGRESS);
        //int counter = 1;
        for (EOProduct product : products) {
            try {
                if (!cancelled) {
                    currentProduct = product;
                    notifier.started(product.getName());
                    if (this.productStatusListener != null) {
                        this.productStatusListener.downloadStarted(product);
                    }
                    this.currentFetcher = dataSource.getProductFetchStrategy(product.getProductType());
                    if (this.currentFetcher instanceof DownloadStrategy) {
                        final DownloadStrategy downloadStrategy = (DownloadStrategy) this.currentFetcher;
                        downloadStrategy.setProgressListener(notifier);
                        downloadStrategy.setDestination(destinationPath);
                        downloadStrategy.setFetchMode(this.fetchMode);
                        if (localRootPath != null) {
                            try {
                                Path archivePath = Paths.get(localRootPath);
                                downloadStrategy.setLocalArchiveRoot(archivePath.toAbsolutePath().toString());
                            } catch (InvalidPathException e) {
                                throw new IOException(e);
                            }
                        }
                    }
                    if (tiles != null && !tryApplyFilter(this.currentFetcher, tiles)) {
                        logger.warning(String.format("Fetch strategy for data source [%s] doesn't support tiles filter",
                                                     dataSourceName));
                    }
                    Path productPath = this.currentFetcher.fetch(product);
                    if (productPath != null) {
                        product.setLocation(productPath.toUri().toString());
                    }
                    notifier.ended();
                    if (this.productStatusListener != null) {
                        this.productStatusListener.downloadCompleted(product);
                    }
                }
            } catch (InterruptedException iex) {
                logger.info(String.format("Fetching product '%s' cancelled",
                                          product.getName()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadFailed(product);
                }
            } catch (IOException ex) {
                logger.warning(String.format("Fetching product '%s' failed: %s",
                                             product.getName(), ex.getMessage()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadFailed(product);
                }
            } catch (URISyntaxException e) {
                logger.warning(String.format("Updating product location for '%s' failed: %s",
                                             product.getName(), e.getMessage()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadFailed(product);
                }
            } finally {
                //notifier.notifyProgress(counter++ / products.size());
                this.currentFetcher = null;
            }
        }
        return products;
    }

    public void cancel() {
        this.cancelled = true;
        if (this.currentFetcher != null) {
            this.currentFetcher.cancel();
        }
    }

    private boolean tryApplyFilter(ProductFetchStrategy strategy, Set<String> tiles) {
        if (strategy != null) {
            try {
                final Method method = strategy.getClass().getMethod("setFilteredTiles", tiles.getClass());
                method.invoke(strategy, tiles);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public DataSourceComponent clone() throws CloneNotSupportedException {
        DataSourceComponent newComponent = (DataSourceComponent) super.clone();
        newComponent.sensorName = this.sensorName;
        newComponent.dataSourceName = this.dataSourceName;
        newComponent.fetchMode = this.fetchMode;
        return newComponent;
    }
}
