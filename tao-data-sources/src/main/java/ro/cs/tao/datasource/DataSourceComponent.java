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
package ro.cs.tao.datasource;

import org.apache.http.HttpStatus;
import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.beans.Parameter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.datasource.util.Utilities;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.messaging.DownloadProgressNotifier;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.serialization.GenericAdapter;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.ExceptionUtils;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.executors.monitoring.DownloadProgressListener;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;

/**
 * TAO component specialized that wraps a data source and actually performs product fetching.
 *
 * @author Cosmin Cara
 */
@XmlRootElement(name = "dataSourceComponent")
public class DataSourceComponent extends TaoComponent {
    public static final String QUERY_PARAMETER = "query";
    public static final String SYNC_PARAMETER = "dateSync";
    public static final String RESULTS_PARAMETER = "results";

    @XmlElement
    private String sensorName;
    @XmlElement
    private String dataSourceName;
    @XmlElement
    private boolean system;
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
    private int maxRetries;
    @XmlTransient
    private List<Parameter> overriddenParameters;
    @XmlTransient
    private DownloadProgressListener progressListener;
    @XmlTransient
    private Principal principal;

    @XmlTransient
    private final Logger logger;

    public DataSourceComponent(String sensorName, String dataSourceName) {
        if (sensorName == null) {
            throw new IllegalArgumentException("Parameter [sensorName] must not be null");
        }
        if (dataSourceName == null) {
            throw new IllegalArgumentException("Parameter [dataSourcename] must not be null");
        }
        this.sensorName = sensorName;
        this.dataSourceName = dataSourceName;
        this.id = sensorName + "-" + dataSourceName;
        this.logger = Logger.getLogger(DataSourceComponent.class.getName());
        SourceDescriptor sourceDescriptor = new SourceDescriptor();
        sourceDescriptor.setParentId(this.id);
        sourceDescriptor.setName(QUERY_PARAMETER);
        DataDescriptor srcData = new DataDescriptor();
        srcData.setFormatType(DataFormat.OTHER);
        sourceDescriptor.setDataDescriptor(srcData);
        sourceDescriptor.setCardinality(0);
        addSource(sourceDescriptor);

        sourceDescriptor = new SourceDescriptor();
        sourceDescriptor.setParentId(this.id);
        sourceDescriptor.setName(SYNC_PARAMETER);
        srcData = new DataDescriptor();
        srcData.setFormatType(DataFormat.RASTER);
        sourceDescriptor.setDataDescriptor(srcData);
        sourceDescriptor.setCardinality(0);
        addSource(sourceDescriptor);

        TargetDescriptor targetDescriptor = new TargetDescriptor();
        targetDescriptor.setParentId(this.id);
        targetDescriptor.setName(RESULTS_PARAMETER);
        DataDescriptor destData = new DataDescriptor();
        destData.setFormatType(DataFormat.RASTER);
        targetDescriptor.setDataDescriptor(destData);
        targetDescriptor.setCardinality(0);
        addTarget(targetDescriptor);
        this.system = false;
    }

    public DataSourceComponent() { this.logger = Logger.getLogger(DataSourceComponent.class.getName()); }

    public Principal getPrincipal() {
        return principal;
    }

    public void setPrincipal(Principal principal) {
        this.principal = principal;
    }

    /**
     * Returns the sensor name of this component
     */
    public String getSensorName() { return sensorName; }
    /**
     * Sets the sensor name of this component
     * @param sensorName    The sensor name
     */
    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    /**
     * Returns the data source name of this component
     */
    public String getDataSourceName() { return dataSourceName; }

    /**
     * Sets the data source name of this component
     * @param dataSourceName    The data source name
     */
    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public boolean getSystem() { return system; }
    public void setSystem(boolean value) { this.system = value; }

    /**
     * Returns the user name used by this component to connect to the data source
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the user name used by this component to connect to the data source
     * @param userName  The user name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Returns the password used by this component to connect to the data source
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password used by this component to connect to the data source
     * @param password  The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String defaultId() { return this.sensorName + "-" + this.dataSourceName; }

    /**
     * Returns the parameters that are overridden by this component
     */
    @XmlElementWrapper(name = "specificParameters")
    @XmlElement(name = "dsParameter")
    public List<Parameter> getOverriddenParameters() { return overriddenParameters; }
    /**
     * Sets the parameters that are overridden by this component
     * @param parameters    The list of overridden parameters
     */
    public void setOverriddenParameters(List<Parameter> parameters) { this.overriddenParameters = parameters; }

    /**
     * Returns the value of the given parameter
     * @param name      The parameter name
     * @throws Exception    If the parameter value cannot be marshalled back to the parameter type
     */
    public Object getParameterValue(String name) throws Exception {
        if (this.overriddenParameters != null) {
            Optional<Parameter> parameter = this.overriddenParameters.stream().filter(p -> p.getName().equals(name)).findFirst();
            return parameter.isPresent() ?
                    new GenericAdapter(parameter.get().getType()).marshal(parameter.get().getValue()) :
                    null;
        } else {
            return null;
        }
    }

    @Override
    public void addSource(SourceDescriptor source) {
        if (this.sources != null && this.sources.size() == 2) {
            throw new RuntimeException("A data source component should have exactly two source descriptors");
        }
        super.addSource(source);
    }

    @Override
    public void removeSource(SourceDescriptor source) {
        throw new RuntimeException("Not allowed on " + getClass().getName());
    }

    /**
     * Sets the cardinality of the outputs. Since the outputs can have their own cardinality,
     * this is a convenience method to set the cardinality of the output if the component has only one output.
     * @param targetCardinality The output cardinality.
     */
    public void setTargetCardinality(int targetCardinality) {
        if (this.targets != null && this.targets.size() == 1) {
            this.targets.get(0).setCardinality(targetCardinality);
        }
    }

    @Override
    public void addTarget(TargetDescriptor target) {
        if (this.targets != null && this.targets.size() == 1) {
            throw new RuntimeException("A data source component should have only one target descriptor");
        }
        super.addTarget(target);
    }

    @Override
    public void removeTarget(TargetDescriptor target) {
        throw new RuntimeException("Not allowed on " + getClass().getName());
    }

    /**
     * Sets the credentials of the account to be used by this component
     * @param userName  The user name
     * @param password  The password (preferably encrypted)
     */
    public void setUserCredentials(String userName, String password) {
        this.userName = userName;
        this.password = password;
        if (password != null && userName != null) {
            String decryptedPass = null;
            try {
                decryptedPass = Crypto.decrypt(password, userName);
            } catch (Exception ex) {
                logger.warning(ex.getMessage());
            }
            if (decryptedPass != null) {
                this.password = decryptedPass;
            }
        }
    }

    /**
     * Returns the fetch mode of this component.
     * @see FetchMode for possible values
     */
    public FetchMode getFetchMode() { return fetchMode; }

    /**
     * Sets the fetch mode of this component.
     * @param mode  The mode.
     * @see FetchMode for possible values
     */
    public void setFetchMode(FetchMode mode) { this.fetchMode = mode; }

    /**
     * Sets the maximum number of retries this component should perform for an item that was not successfully fetched.
     * @param maxRetries    The number of retries
     */
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }

    /**
     * Returns the maximum number of retries this component should perform for an item that was not successfully fetched.
     */
    public int getMaxRetries() { return maxRetries; }

    /**
     * Sets a listener that will react to product fetch status changes (i.e. fetch started, error, fetch completed)
     * @param listener  The status listener
     */
    public void setProductStatusListener(ProductStatusListener listener) {
        this.productStatusListener = listener;
    }

    /**
     * Sets a progress listener that will receive progress notifications.
     * @param listener  The progress listener
     */
    public void setProgressListener(DownloadProgressListener listener) { this.progressListener = listener; }

    /**
     * Returns the product that this component currently is trying to fetch.
     */
    public EOProduct getCurrentProduct() { return currentProduct; }

    /**
     * Creates a query that can be submitted to (executed against) the data source of this component.
     */
    public DataQuery createQuery() {
        DataSource<?, ?> dataSource = createDataSource();
        return dataSource.createQuery(this.sensorName);
    }

    /**
     * Performs a query with the given query parameters.
     * @param parameters    The query parameters
     */
    public List<EOProduct> doQuery(List<QueryParameter<?>> parameters) throws QueryException {
        final DataQuery query = createQuery();
        if (parameters != null) {
            parameters.forEach(query::addParameter);
        }
        return query.execute();
    }

    /**
     * Performs a count query with the given parameters.
     * @param parameters    The query parameters
     * @return  The number of results
     */
    public long doCount(List<QueryParameter<?>> parameters) throws QueryException {
        final DataQuery query = createQuery();
        if (parameters != null) {
            parameters.forEach(query::addParameter);
        }
        return query.getCount();
    }

    /**
     * Retrieves the given list of products, optionally filtering them by tiles, to the given path.
     * @param products  The list of products to be retrieved
     * @param tiles     The tile filter. Can be null.
     * @param destinationPath   The location where the products will be placed.
     */
    public List<EOProduct> doFetch(List<EOProduct> products, Set<String> tiles, String destinationPath) {
        return doFetch(products, tiles, destinationPath, null, null);
    }

    /**
     * Retrieves the given list of products, optionally filtering them by tiles, to the given path.
     * If the localRootPath is set and the {@link FetchMode} of this component is set to FetchMode.SYMLINK,
     * this component will try to create symbolic links to the given products that are physically found in the local
     * archive.
     * @param products  The list of products to be fetched
     * @param tiles     The tile filter
     * @param destinationPath   The location where the products will be placed
     * @param localRootPath     The path to the local products archive
     * @param additionalProperties  Additional properties that can be passed to the component
     */
    public List<EOProduct> doFetch(List<EOProduct> products, Set<String> tiles, String destinationPath, String localRootPath, Properties additionalProperties) {
        return DownloadManager.queueDownload(this, this::doFetchImpl, products, tiles, destinationPath, localRootPath, additionalProperties);
    }

    /**
     * Resumes the fetch operation on the current product.
     */
    public void resume() {
        this.cancelled = false;
        if (this.currentFetcher != null) {
            this.currentFetcher.resume();
        }
    }

    /**
     * Cancels the fetch operation on the current product.
     */
    public void cancel() {
        DownloadManager.cancelDownload(this, this::cancelImpl);
    }

    @Override
    public DataSourceComponent clone() throws CloneNotSupportedException {
        DataSourceComponent newComponent = (DataSourceComponent) super.clone();
        newComponent.sensorName = this.sensorName;
        newComponent.dataSourceName = this.dataSourceName;
        newComponent.fetchMode = this.fetchMode;
        newComponent.principal = this.principal;
        return newComponent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DataSourceComponent that = (DataSourceComponent) o;
        return Objects.equals(sensorName, that.sensorName) &&
                Objects.equals(dataSourceName, that.dataSourceName) &&
                Objects.equals(userName, that.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sensorName, dataSourceName, userName);
    }

    private Void cancelImpl() {
        this.cancelled = true;
        if (this.currentFetcher != null) {
            this.currentFetcher.cancel();
        }
        return null;
    }

    private List<EOProduct> doFetchImpl(List<EOProduct> products, Set<String> tiles, String destinationPath, String localRootPath, Properties additionalProperties) {
        DataSource<?, ?> dataSource;
        String errorMessage;
        for (EOProduct product : products) {
            dataSource = createDataSource(product);
            // add the attribute for max retries such that if the maxRetries is exceeded
            // to be set, on failure, to aborted state
            product.addAttribute("maxRetries", String.valueOf(this.maxRetries));
            errorMessage = null;
            try {
                if (!cancelled) {
                    if (this.productStatusListener != null) {
                        if (!this.productStatusListener.downloadStarted(product)) {
                            continue;
                        }
                    }
                    currentProduct = product;
                    ProductFetchStrategy templateFetcher = dataSource.getProductFetchStrategy(product.getProductType());
                    if (templateFetcher == null) {
                        final String collection = product.getAttributeValue("collection");
                        if (collection != null) {
                            templateFetcher = dataSource.getProductFetchStrategy(collection);
                        } else {
                            templateFetcher = dataSource.getProductFetchStrategy(product.getSatelliteName().replace("-", ""));
                        }
                    }
                    if (templateFetcher == null) {
                        throw new NoSuchElementException("Product type '" + product.getProductType() + "' doesn't have an associated download strategy!");
                    }
                    if (templateFetcher instanceof DownloadStrategy) {
                        DownloadStrategy downloadStrategy = ((DownloadStrategy) templateFetcher).clone();
                        downloadStrategy.setDestination(destinationPath);
                        downloadStrategy.setFetchMode(this.fetchMode);
                        if (additionalProperties != null) {
                            downloadStrategy.addProperties(additionalProperties);
                        }
                        if (localRootPath != null) {
                            try {
                                Path archivePath = Paths.get(localRootPath);
                                downloadStrategy.setLocalArchiveRoot(archivePath.toAbsolutePath().toString());
                            } catch (InvalidPathException e) {
                                throw new IOException(e);
                            }
                        }
                        currentFetcher = downloadStrategy;
                    } else {
                        currentFetcher = templateFetcher.clone();
                    }
                    if (tiles != null && !tryApplyFilter(currentFetcher, tiles)) {
                        logger.warning(String.format("Fetch strategy for data source [%s] doesn't support tiles filter",
                                                     dataSourceName));
                    }
                    if (this.progressListener == null) {
                        Principal theOwner = this.principal == null ? SessionStore.currentContext().getPrincipal() : this.principal;
                        currentFetcher.setProgressListener(new DownloadProgressNotifier(theOwner,
                                                                                        this,
                                                                                        DataSourceTopic.PRODUCT_PROGRESS));
                    } else {
                        currentFetcher.setProgressListener(this.progressListener);
                    }
                    Path productPath = currentFetcher.fetch(product);
                    if (productPath != null) {
                        product.setLocation(productPath.toUri().toString());
                    }
                    if (this.productStatusListener != null) {
                        // if the path is null, it means that either the product failed downloading or the
                        // tiles filter were not passed
                        if (productPath != null) {
                            this.productStatusListener.downloadCompleted(product);
                        } else {
                            this.productStatusListener.downloadFailed(product, "Could not download or no tiles");
                        }
                    }
                }
            } catch (InterruptedException iex) {
                logger.warning(String.format("Fetching product '%s' cancelled",
                                          product.getName()));
                errorMessage = "Cancelled";
            } catch (IOException ex) {
                logger.warning(String.format("Fetching product '%s' failed: %s",
                                             product.getName(), ExceptionUtils.getStackTrace(logger, ex)));
                errorMessage = ex.getMessage();
            } catch (URISyntaxException e) {
                logger.warning(String.format("Updating product location for '%s' failed: %s",
                                             product.getName(), e.getMessage()));
                errorMessage = e.getMessage();
            } catch (NoSuchElementException e) {
                logger.warning(String.format("Fetching product '%s' ignored: %s",
                                             product.getName(), e.getMessage()));
                errorMessage = e.getMessage();
            } catch (Exception e) {
                errorMessage = e.getMessage();
            } finally {
                if (errorMessage != null && this.productStatusListener != null) {
                    if(errorMessage.contains(""+ HttpStatus.SC_ACCEPTED)){
                        this.productStatusListener.downloadQueued(product,"Product not ready for download now. Download was queued.");
                    }else {
                        this.productStatusListener.downloadFailed(product, errorMessage);
                    }
                }
                if (currentFetcher != null) {
                    currentFetcher.resume();
                }
                currentFetcher = null;
                this.currentProduct = null;
                //notifier.ended();
            }
        }
        return products;
    }

    private DataSource<?, ?> createDataSource() {
        return createDataSource(null);
    }

    private DataSource<?, ?> createDataSource(EOProduct product) {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource<?, ?> dataSource = null;
        if (product != null) {
            final String productLocationUrlDomain = Utilities.getDomainURL(product.getLocation());
            dataSource = dsManager.getMatchingDataSource(productLocationUrlDomain);
        }
        if (dataSource == null) {
            dataSource = this.dataSourceName != null ?
                    dsManager.createInstance(this.sensorName, this.dataSourceName) :
                    dsManager.createInstance(this.sensorName);
        }
        if (!StringUtilities.isNullOrEmpty(this.userName)) {
            dataSource.setCredentials(this.userName, this.password);
        }
        return dataSource;
    }

    private boolean tryApplyFilter(ProductFetchStrategy strategy, Set<String> tiles) {
        if (strategy != null) {
            try {
                final Method method = strategy.getClass().getMethod("setFilteredTiles", Set.class);
                method.invoke(strategy, tiles);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }
}
