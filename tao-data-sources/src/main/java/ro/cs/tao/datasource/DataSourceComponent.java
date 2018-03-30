/*
 * Copyright (C) 2017 CS ROMANIA
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

import ro.cs.tao.component.DataDescriptor;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.beans.Parameter;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.datasource.remote.DownloadStrategy;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.DataFormat;
import ro.cs.tao.messaging.ProgressNotifier;
import ro.cs.tao.serialization.GenericAdapter;
import ro.cs.tao.utils.Crypto;
import ro.cs.tao.utils.FileUtils;

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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
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
    private int maxRetries;
    @XmlTransient
    private List<Parameter> overriddenParameters;

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
        this.targetCardinality = -1;
        this.logger = Logger.getLogger(DataSourceComponent.class.getSimpleName());
        TargetDescriptor targetDescriptor = new TargetDescriptor();
        targetDescriptor.setParentId(this.id);
        targetDescriptor.setName("results");
        DataDescriptor dataDescriptor = new DataDescriptor();
        dataDescriptor.setFormatType(DataFormat.RASTER);
        targetDescriptor.setDataDescriptor(dataDescriptor);
        addTarget(targetDescriptor);
    }

    public DataSourceComponent() { this.logger = Logger.getLogger(DataSourceComponent.class.getSimpleName()); }

    public String getSensorName() { return sensorName; }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getDataSourceName() { return dataSourceName; }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String defaultName() { return this.sensorName + "-" + this.dataSourceName; }

    @XmlElementWrapper(name = "specificParameters")
    @XmlElement(name = "dsParameter")
    public List<Parameter> getOverriddenParameters() { return overriddenParameters; }
    public void setOverriddenParameters(List<Parameter> parameters) { this.overriddenParameters = parameters; }

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
    public List<SourceDescriptor> getSources() {
        //throw new RuntimeException("Not allowed on " + getClass().getName());
        return null;
    }

    @Override
    public void setSources(List<SourceDescriptor> sources) {
        //throw new RuntimeException("Not allowed on " + getClass().getName());
    }

    @Override
    public int getSourceCardinality() {
        //throw new RuntimeException("Not allowed on " + getClass().getName());
        return -1;
    }

    @Override
    public void setSourceCardinality(int sourceCardinality) {
        //throw new RuntimeException("Not allowed on " + getClass().getName());
    }

    @Override
    public void addSource(SourceDescriptor source) {
        throw new RuntimeException("Not allowed on " + getClass().getName());
    }

    @Override
    public void removeSource(SourceDescriptor source) {
        throw new RuntimeException("Not allowed on " + getClass().getName());
    }

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

    public FetchMode getFetchMode() { return fetchMode; }
    public void setFetchMode(FetchMode mode) { this.fetchMode = mode; }
    public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    public int getMaxRetries() { return maxRetries; }

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

    public long doCount(List<QueryParameter> parameters) throws QueryException {
        DataSourceManager dsManager = DataSourceManager.getInstance();
        DataSource dataSource = this.dataSourceName != null ?
                dsManager.get(this.sensorName, this.dataSourceName) : dsManager.get(this.sensorName);
        dataSource.setCredentials(this.userName, this.password);
        final DataQuery query = dataSource.createQuery(this.sensorName);
        if (parameters != null) {
            parameters.forEach(query::addParameter);
        }
        return query.getCount();
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
        for (EOProduct product : products) {
            // add the attribute for max retries such that if the maxRetries is exceeded
            // to be set, on failure, to aborted state
            product.addAttribute("maxRetries", String.valueOf(this.maxRetries));
            try {
                if (!cancelled) {
                    if (this.productStatusListener != null) {
                        if (!this.productStatusListener.downloadStarted(product)) {
                            continue;
                        }
                    }
                    currentProduct = product;
                    ProgressNotifier notifier = new ProgressNotifier(securityContext().getPrincipal(),
                            this,
                            DataSourceTopics.PRODUCT_PROGRESS);
                    ProductFetchStrategy templateFetcher = dataSource.getProductFetchStrategy(product.getProductType());
                    if (templateFetcher instanceof DownloadStrategy) {
                        DownloadStrategy downloadStrategy = ((DownloadStrategy) templateFetcher).clone();
                        downloadStrategy.setProgressListener(notifier);
                        downloadStrategy.setDestination(destinationPath);
                        downloadStrategy.setFetchMode(this.fetchMode);
                        if (localRootPath != null) {
                            try {
                                Path archivePath = Paths.get(localRootPath);
                                FileUtils.ensureExists(archivePath);
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
                    Path productPath = currentFetcher.fetch(product);
                    if (productPath != null) {
                        product.setLocation(productPath.toUri().toString());
                    }
                    notifier.ended();
                    if (this.productStatusListener != null) {
                        // if the path is null, it means that either the product failed downloading or the
                        // tiles filter did not passed
                        if (productPath != null) {
                            this.productStatusListener.downloadCompleted(product);
                        } else {
                            this.productStatusListener.downloadFailed(product, "Could not download or no tiles");
                        }
                    }
                }
            } catch (InterruptedException iex) {
                logger.info(String.format("Fetching product '%s' cancelled",
                                          product.getName()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadFailed(product, "Cancelled");
                }
            } catch (IOException ex) {
                logger.warning(String.format("Fetching product '%s' failed: %s",
                                             product.getName(), ex.getMessage()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadFailed(product, ex.getMessage());
                }
            } catch (URISyntaxException e) {
                logger.warning(String.format("Updating product location for '%s' failed: %s",
                                             product.getName(), e.getMessage()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadFailed(product, e.getMessage());
                }
            } catch (NoSuchElementException e) {
                logger.warning(String.format("Fetching product '%s' ignored: %s",
                        product.getName(), e.getMessage()));
                if (this.productStatusListener != null) {
                    this.productStatusListener.downloadIgnored(product, e.getMessage());
                }
            } finally {
                //notifier.notifyProgress(counter++ / products.size());
                if (currentFetcher != null) {
                    currentFetcher.resume();
                }
                currentFetcher = null;
            }
        }
        return products;
    }

    public void resume() {
        this.cancelled = false;
        if (this.currentFetcher != null) {
            this.currentFetcher.resume();
        }
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
                final Method method = strategy.getClass().getMethod("setFilteredTiles", Set.class);
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
