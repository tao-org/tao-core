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

package ro.cs.tao.execution.local;

import ro.cs.tao.EnumUtils;
import ro.cs.tao.component.*;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.*;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.datasource.param.JavaType;
import ro.cs.tao.datasource.persistence.DataSourceComponentProvider;
import ro.cs.tao.datasource.persistence.DataSourceConfigurationProvider;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.ProductStatus;
import ro.cs.tao.eodata.sorting.*;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.persistence.ExecutionJobProvider;
import ro.cs.tao.execution.persistence.ExecutionTaskProvider;
import ro.cs.tao.persistence.EOProductProvider;
import ro.cs.tao.persistence.WorkflowProvider;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.spi.OutputDataHandlerManager;
import ro.cs.tao.utils.FileUtilities;
import ro.cs.tao.utils.StringUtilities;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Specialized executor that will perform a query to a data source.
 *
 * @author Cosmin Cara
 */
public class QueryExecutor extends Executor<DataSourceExecutionTask> implements ProductStatusListener {

    private static final String LOCAL_DATABASE = "Local Database";
    private final ExecutorService backgroundWorker = new NamedThreadPoolExecutor("query-thread", 1);//Executors.newSingleThreadExecutor();
    private DataSourceComponent dataSourceComponent;
    private static EOProductProvider productProvider;
    private static WorkflowProvider workflowProvider;
    private static ExecutionTaskProvider taskProvider;
    private static ExecutionJobProvider jobProvider;
    private static DataSourceConfigurationProvider configurationProvider;
    private static DataSourceComponentProvider componentProvider;

    @Override
    public boolean supports(TaoComponent component) { return component instanceof DataSourceComponent; }

    public static void setProductProvider(EOProductProvider provider) {
        productProvider = provider;
    }

    public static void setWorkflowProvider(WorkflowProvider provider) { workflowProvider = provider; }

    public static void setDataSourceConfigurationProvider(DataSourceConfigurationProvider provider) { configurationProvider = provider; }

    public static void setTaskProvider(ExecutionTaskProvider provider) { taskProvider = provider; }

    public static void setJobProvider(ExecutionJobProvider provider) {
        jobProvider = provider;
    }

    public static void setComponentProvider(DataSourceComponentProvider provider) { componentProvider = provider; }

    public QueryExecutor() {
        super();
        if (productProvider == null) {
            throw new ExecutionException("A ProductProvider is required to use this executor");
        }
        if (workflowProvider == null) {
            throw new ExecutionException("A WorkflowProvider is required to use this executor");
        }
        if (taskProvider == null) {
            throw new ExecutionException("An ExecutionTaskProvider is required to use this executor");
        }
        if (jobProvider == null) {
            throw new ExecutionException("An ExecutionJobProvider is required to use this executor");
        }
        if (componentProvider == null) {
            throw new ExecutionException("An DataSourceComponentProvider is required to use this executor");
        }
    }

    @Override
    public void execute(DataSourceExecutionTask task) throws ExecutionException {
        try {
            task.setResourceId(UUID.randomUUID().toString());
            logger.fine(String.format("Received task %s", task.getComponent().getId()));
            task.setExecutionNodeHostName(InetAddress.getLocalHost().getHostName());
            task.setStartTime(LocalDateTime.now());
            //changeTaskStatus(task, ExecutionStatus.RUNNING);
            task.setExecutionStatus(ExecutionStatus.RUNNING);
            taskProvider.update(task);
            final ExecutionJob job = task.getJob();
            job.setExecutionStatus(ExecutionStatus.RUNNING);
            jobProvider.update(job);
            dataSourceComponent = task.getComponent();
            // DSComponents having the id starting with 'product-set-' don't need to execute
            // a query. Instead, they should already contain a list of paths.
            ExecutionStatus status;
            String message = null;
            if (!dataSourceComponent.getId().startsWith("product-set-")) {
                List<Variable> values = task.getInputParameterValues();
                if (values == null || values.isEmpty()) {
                    throw new ExecutionException(String.format("No input data for the task %s", task.getId()));
                }
                Variable variable = values.stream().filter(v -> DataSourceComponent.QUERY_PARAMETER.equals(v.getKey()) ||
                                                  "local.query".equals(v.getKey()))
                                          .findFirst().orElse(null);
                if (variable == null) {
                    throw new ExecutionException("Expected parameter [query] not found");
                }
                Query primaryQuery = Query.fromString(variable.getValue());
                if (primaryQuery == null) {
                    throw new ExecutionException(String.format("Invalid input data for the task %s", task.getId()));
                }
                final DataQuery dataQuery = Query.toDataQuery(primaryQuery);
                final Future<List<EOProduct>> future = backgroundWorker.submit(dataQuery::execute);
                final List<EOProduct> results = future.get();
                if (results != null && !results.isEmpty()) {
                    int cardinality = dataSourceComponent.getSources().get(0).getCardinality();
                    int rSize = results.size();
                    if (cardinality > 0 && cardinality != rSize) {
                        variable = values.stream().filter(v -> "remote.query".equals(v.getKey()))
                                         .findFirst().orElse(null);
                        if (variable == null) {
                            message = String.format("Task %s expected %s inputs, but received %s. Execution is cancelled",
                                                           task.getId(), cardinality, results.size());
                            logger.warning(message);
                            markTaskFinished(task, ExecutionStatus.CANCELLED, message);
                            return;
                        } else {
                            results.removeIf(r -> r.getProductStatus() == ProductStatus.DOWNLOADING ||
                                    r.getProductStatus() == ProductStatus.DOWNLOADED);
                            logger.info(String.format("Task %s expects %d inputs, but only %d are locally available. Download for the missing will start",
                                                      task.getId(), rSize, rSize - results.size()));
                        }
                    }
                    WorkflowDescriptor workflow = workflowProvider.get(job.getWorkflowId());
                    variable = values.stream().filter(v -> DataSourceComponent.SYNC_PARAMETER.equals(v.getKey()))
                                     .findFirst().orElse(null);
                    if (variable != null && variable.getValue() != null) {
                        // The current resultset must be synchronized with the one received in this parameter
                        // Synchronization means filtering by dates of the previous resultset
                        WorkflowNodeDescriptor node = workflow.getNodes().stream().filter(n -> n.getId().equals(task.getWorkflowNodeId())).findFirst().get();
                        String[] filter = null;
                        Set<ComponentLink> links = node.getIncomingLinks();
                        if (links != null) {
                            filter = links.iterator().next().getAggregator().getFilter();
                        }
                        filterByOtherList(results, variable.getValue(), filter);
                    }
                    final String sensorName = dataSourceComponent.getSensorName().toLowerCase().replace(" ", "-");
                    dataSourceComponent.setProductStatusListener(this);
                    List<EOProduct> products = new ArrayList<>();
                    if (!dataSourceComponent.getId().contains(LOCAL_DATABASE)) {
                        // first try to retrieve the products that have already been downloaded locally
                        List<EOProduct> localProducts = productProvider.list(results.stream().map(EOData::getId).collect(Collectors.toList()));
                        if (localProducts != null) {
                            localProducts.removeIf(p -> p.getProductStatus() != ProductStatus.DOWNLOADED);
                            localProducts.forEach(this::downloadCompleted);
                            products.addAll(localProducts);
                            Set<String> ids = localProducts.stream().map(EOData::getId).collect(Collectors.toSet());
                            results.removeIf(p -> ids.contains(p.getId()));
                        }
                    }
                    final DataSourceConfiguration dataSourceConfiguration = configurationProvider.get(dataSourceComponent.getId());
                    String localRepositoryRoot;
                    Properties additionalProperties = null;
                    if (dataSourceConfiguration != null) {
                        dataSourceComponent.setFetchMode(dataSourceConfiguration.getFetchMode());
                        localRepositoryRoot = dataSourceConfiguration.getLocalRepositoryPath();
                        Map<String, String> parameters = dataSourceConfiguration.getParameters();
                        if (parameters != null) {
                            additionalProperties = new Properties();
                            additionalProperties.putAll(parameters);
                        }

                    } else {
                        localRepositoryRoot = ConfigurationManager.getInstance().getValue(String.format("local.%s.path", sensorName));
                    }
                    final List<EOProduct> remoteProducts = dataSourceComponent.doFetch(results,
                                                                                       null,
                                                                                       SystemVariable.USER_WORKSPACE.value(),
                                                                                       localRepositoryRoot,
                                                                                       additionalProperties);
                    if (remoteProducts != null) {
                        if (!dataSourceComponent.getId().contains(LOCAL_DATABASE)) {
                            backgroundWorker.submit(() -> persistResults(remoteProducts));
                        }
                        products.addAll(remoteProducts);
                    }
                    task.getComponent().setTargetCardinality(products.size());
                    Long workflowNodeId = task.getWorkflowNodeId();
                    List<Tuple<EOProduct, EOProduct>> tuples = null;

                    List<WorkflowNodeDescriptor> linkedNodes =
                            workflow.getNodes().stream().filter(n -> n.getIncomingLinks() != null &&
                                            n.getIncomingLinks().stream().anyMatch(l -> l.getSourceNodeId() == workflowNodeId))
                                    .collect(Collectors.toList());
                    Aggregator aggregator;
                    for (WorkflowNodeDescriptor linkedNode : linkedNodes) {
                        Set<ComponentLink> links = linkedNode.getIncomingLinks();
                        for (ComponentLink link : links) {
                            if (link.getSourceNodeId() == workflowNodeId && (aggregator = link.getAggregator()) != null) {
                                String[] sort = aggregator.getSorter();
                                if (sort != null) {
                                    DataSorter<EOProduct> sorter = ProductSortingFactory.getSorter(sort[0]);
                                    products = sorter.sort(products, sort[1] == null || sort[1].equalsIgnoreCase("asc"));
                                }
                                if (linkedNode.getComponentType() == ComponentType.DATASOURCE) {
                                    String[] group = aggregator.getAssociator();
                                    if (group != null) {
                                        Association<EOProduct> association = ProductAssociationFactory.getAssociation(group[0]);
                                        if (group.length == 2) {
                                            JavaType javaType = JavaType.fromFriendlyName(association.description()[1]);
                                            association = ProductAssociationFactory.getAssociation(group[0],
                                                                                                   javaType.isArrayType()
                                                                                                        ? javaType.parseArray(group[1])
                                                                                                        : javaType.parse(group[1]));
                                        }
                                        tuples = association.associate(products);
                                    }
                                }
                            }
                        }
                    }
                    if (tuples != null) {
                        task.setOutputParameterValue(dataSourceComponent.getTargets().get(0).getName(),
                                                     serializeGroups(tuples));
                    } else {
                        task.setOutputParameterValue(dataSourceComponent.getTargets().get(0).getName(),
                                                     serializeResults(products));
                    }
                    status = ExecutionStatus.DONE;
                } else {
                    message = "Query returned no results";
                    logger.warning(message);
                    status = ExecutionStatus.CANCELLED;
                }
            } else {
                final String outPort = dataSourceComponent.getTargets().get(0).getName();
                List<Variable> values = task.getOutputParameterValues();
                final String strList;
                if (values != null && !values.isEmpty() && values.stream().anyMatch(v -> outPort.equals(v.getKey()))) {
                    strList = values.stream().filter(v -> outPort.equals(v.getKey())).findFirst().get().getValue();
                } else {
                    final SourceDescriptor descriptor = dataSourceComponent.getSources().stream()
                                                                           .filter(s -> s.getName().equals(DataSourceComponent.QUERY_PARAMETER))
                                                                           .findFirst().get();
                    strList = descriptor.getDataDescriptor().getLocation();
                    final String[] list = strList.split(",");
                    // First try to replace any symlinks with actual data to avoid S3 read issues (if configured)
                    final StringBuilder builder = new StringBuilder();
                    final StringBuilder srcBuilder = new StringBuilder();
                    final boolean shouldReplace = ConfigurationManager.getInstance().getBooleanValue("replace.local.symlinks");
                    final double total = list.length;
                    double counter = 0;
                    for (String item : list) {
                        try {
                            Path path = FileUtilities.toPath(item);
                            if (shouldReplace && Files.isSymbolicLink(path)) {
                                sendMessage(task, "Product " + path.getFileName() + " is being copied locally.");
                                counter += 1.0;
                                sendProgressMessage(task, counter / total);
                                builder.append(FileUtilities.asUnixPath(FileUtilities.replaceLink(path, null), true));
                            } else {
                                builder.append(FileUtilities.asUnixPath(path, true));
                            }
                            builder.append(",");
                            srcBuilder.append(FileUtilities.asUnixPath(path, true)).append(",");
                        } catch (IOException e) {
                            logger.warning(e.getMessage());
                        }
                    }
                    if (builder.length() > 0) {
                        builder.setLength(builder.length() - 1);
                        srcBuilder.setLength(srcBuilder.length() - 1);
                    } else {
                        throw new QueryException("Either the product set is empty or the files don't exist on the disk");
                    }
                    // Persist any targets of the symlinks, together with the symlink path.
                    // They will be restored at the end of the job.
                    final TargetDescriptor targetDescriptor = dataSourceComponent.getTargets().stream()
                                                                                 .filter(t -> t.getName().equals(DataSourceComponent.RESULTS_PARAMETER))
                                                                                 .findFirst().get();
                    targetDescriptor.getDataDescriptor().setLocation(builder.toString());
                    descriptor.getDataDescriptor().setLocation(srcBuilder.toString());
                    componentProvider.save(dataSourceComponent);
                    task.setOutputParameterValue(outPort, jsonifyResults(Arrays.asList(list)));
                    task.getComponent().setTargetCardinality(descriptor.getCardinality());
                }
                if (StringUtilities.isNullOrEmpty(strList)) {
                    throw new QueryException("Empty product set");
                }
                task.setInstanceTargetOutput(strList);
                status = ExecutionStatus.DONE;
            }
            markTaskFinished(task, status, message);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            markTaskFinished(task, ExecutionStatus.FAILED, ex.getMessage());
        }
    }

    @Override
    public void stop(DataSourceExecutionTask task) throws ExecutionException {
        if (dataSourceComponent == null || !dataSourceComponent.equals(task.getComponent())) {
            logger.warning("stop() called on different component");
        }
        if (dataSourceComponent != null) {
            dataSourceComponent.cancel();
        }
        markTaskFinished(task, ExecutionStatus.CANCELLED, ExecutionStatus.CANCELLED.friendlyName());
    }

    @Override
    public void suspend(DataSourceExecutionTask task) throws ExecutionException {
        throw new ExecutionException("suspend() not supported on data sources");
    }

    @Override
    public void resume(DataSourceExecutionTask task) throws ExecutionException {
        throw new ExecutionException("resume() not supported on data sources");
    }

    @Override
    public void monitorExecutions() throws ExecutionException {
        this.isInitialized.set(false);
    }

    @Override
    public String defaultId() { return "QueryExecutor"; }

    private boolean isArray(String value) {
        return value != null && value.startsWith("[") & value.endsWith("]");
    }

    private void persistResults(List<EOProduct> results) {
        OutputDataHandlerManager.getInstance().applyHandlers(results);
    }

    /**
     * Transforms a list of products in a string containing a list of product names
     * @param results   The list of products
     */
    private String serializeResults(List<EOProduct> results) {
        String json = null;
        try {
            StringListAdapter adapter = new StringListAdapter();
            json = adapter.unmarshal(results.stream().map(p -> FileUtilities.resolve(p.getLocation(), p.getEntryPoint())).collect(Collectors.toList()));
        } catch (Exception e) {
            logger.severe("Serialization of results failed: " + e.getMessage());
        }
        return json;
    }

    private String jsonifyResults(List<String> results) {
        String json = null;
        try {
            StringListAdapter adapter = new StringListAdapter();
            json = adapter.unmarshal(results);
        } catch (Exception e) {
            logger.severe("Serialization of results failed: " + e.getMessage());
        }
        return json;
    }

    private List<String> deserializeResults(String list) {
        List<String> names = null;
        try {
            StringListAdapter adapter = new StringListAdapter();
            names = adapter.marshal(list);
        } catch (Exception e) {
            logger.severe("Deserialization of list failed: " + e.getMessage());
        }
        return names;
    }

    private String serializeGroups(List<Tuple<EOProduct, EOProduct>> results) {
        String json = null;
        try {
            StringListAdapter adapter = new StringListAdapter();
            json = adapter.unmarshal(results.stream()
                                            .map(t -> new Tuple<>(t.getKeyOne().getLocation(),
                                                                  t.getKeyTwo() != null ?
                                                                          t.getKeyTwo().getLocation() : null)
                                                        .toString())
                                             .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.severe("Serialization of results failed: " + e.getMessage());
        }
        return json;
    }

    private void filterByOtherList(List<EOProduct> products, String otherProductsList, String[] filters) {
        final List<String> names = deserializeResults(otherProductsList);
        final List<EOProduct> otherProducts = productProvider.getProductsByNames(names.toArray(new String[0]));
        // If other list consists of non-products, do nothing
        if (otherProducts != null && !otherProducts.isEmpty()) {
            final Set<LocalDate> otherDates = otherProducts.stream().map(p -> p.getAcquisitionDate().toLocalDate()).collect(Collectors.toSet());
            products.removeIf(p -> !otherDates.contains(p.getAcquisitionDate().toLocalDate()));
            final Map<LocalDate, List<EOProduct>> groupsByDate = new HashMap<>();
            LocalDate date;
            // First, group by dates
            for (EOProduct product : products) {
                date = product.getAcquisitionDate().toLocalDate();
                List<EOProduct> list = groupsByDate.computeIfAbsent(date, k -> new ArrayList<>());
                list.add(product);
            }
            // If multiple products on the same date, keep the one according to the defined filter or keep the "middle one"
            DataFilter<EOProduct> filter = null;
            if (filters != null) {
                filter = ProductFilterFactory.getFilter(filters[0]);
                if (filters.length == 2) {
                    JavaType javaType = JavaType.fromFriendlyName(filter.description()[1]);
                    filter = ProductFilterFactory.getFilter(filters[0],
                                                            javaType.isArrayType()
                                                                ? javaType.parseArray(filters[1])
                                                                : javaType.parse(filters[1]));
                }
            }
            if (filter == null) {
                filter = new KeepMiddleProductFilter();
            }
            final Set<EOProduct> toKeep = new HashSet<>();
            for (Map.Entry<LocalDate, List<EOProduct>> entry : groupsByDate.entrySet()) {
                List<EOProduct> list = entry.getValue();
                toKeep.add(filter.filter(list));
            }
            groupsByDate.clear();
            products.removeIf(p -> !toKeep.contains(p));
            toKeep.clear();
        }
    }

    @Override
    public boolean downloadStarted(EOProduct product) {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	
        try {
        	// check input quota before download
        	if (product.getRefs() != null && !product.getRefs().contains(principal.getName()) && !UserQuotaManager.getInstance().checkUserInputQuota(principal, product.getApproximateSize())) {
                // For symlinks, the quota is not affected and hence should continue
                final String fetchMode = product.getAttributeValue("fetch");
                if (fetchMode != null && EnumUtils.getEnumConstantByName(FetchMode.class, fetchMode).value() < 4) {
                    // do not allow for the download to start
                    return false;
                } else {
                    product.removeAttribute("fetch");
                }
        	}
        	
        	// check if the product already exists
        	final EOProduct oldProd = productProvider.get(product.getId());
        	if (oldProd != null) {
        		// if the product is failed or downloading, copy its references but continue with the download
        		if (oldProd.getProductStatus() == ProductStatus.FAILED || oldProd.getProductStatus() == ProductStatus.DOWNLOADING) {
        			product.setRefs(oldProd.getRefs());
        		} else {
        			// add the current user as reference
        			product.addReference(principal.getName());
        			product.setRefs(oldProd.getRefs());
        			// copy the status of the previous product
        			product.setProductStatus(oldProd.getProductStatus());

        			// update the user's input quota
                    UserQuotaManager.getInstance().updateUserInputQuota(principal);
            		// do not allow for the download to start
        			return false;
        		}
        	}        	
        	
            product.setProductStatus(ProductStatus.DOWNLOADING);
            // attach the current user 
            product.addReference(principal.getName());
            productProvider.save(product);

            // update the user's input quota
            UserQuotaManager.getInstance().updateUserInputQuota(principal);
            return true;
        } catch (QuotaException  e) {
        	logger.severe(String.format("Cannot update the input quota for user %s. Reason: %s", principal.getName(), e.getMessage()));
        	return false;
        } catch (Exception e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
            return false;
        }
    }

    @Override
    public void downloadCompleted(EOProduct product) {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
        try {
        	// re-update the references, in case some other user tried to download this product after 
        	// the current user started
        	final EOProduct oldProd = productProvider.get(product.getId());
        	if (oldProd != null) {
        		product.setRefs(oldProd.getRefs());
        	}
        	
            product.setProductStatus(ProductStatus.DOWNLOADED);
            // update the product's reference
            product.addReference(principal.getName());
            productProvider.save(product);

            // update the user's input quota
            UserQuotaManager.getInstance().updateUserInputQuota(principal);
        } catch (QuotaException  e) {
        	logger.severe(String.format("Cannot update the input quota for user %s. Reason: %s", principal.getName(), e.getMessage()));
        } catch (Exception e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
        }
    }

    @Override
    public void downloadFailed(EOProduct product, String reason) {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	
        try {
            product.setProductStatus(ProductStatus.FAILED);
            product.removeReference(principal.getName());
            productProvider.save(product);
            // roll back the user's quota
            UserQuotaManager.getInstance().updateUserInputQuota(principal);
            logger.warning(String.format("Product %s not downloaded. Reason: %s", product.getName(), reason));
        } catch (QuotaException  e) {
        	logger.severe(String.format("Cannot update the input quota for user %s. Reason: %s", principal.getName(), e.getMessage()));
        } catch (Exception e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
        }
    }

    @Override
    public void downloadAborted(EOProduct product, String reason) {
        downloadFailed(product, reason);
    }

    @Override
    public void downloadIgnored(EOProduct product, String reason) {
        //No-op
    }

    @Override
    public void downloadQueued(EOProduct product, String reason) {
        product.setProductStatus(ProductStatus.QUEUED);
        logger.warning(String.format("Product %s not downloaded. Reason: %s", product.getName(), reason));
    }
}
