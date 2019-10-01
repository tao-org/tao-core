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

import ro.cs.tao.component.*;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.ProductStatusListener;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.eodata.EOData;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.eodata.enums.ProductStatus;
import ro.cs.tao.eodata.sorting.Association;
import ro.cs.tao.eodata.sorting.DataSorter;
import ro.cs.tao.eodata.sorting.ProductAssociationFactory;
import ro.cs.tao.eodata.sorting.ProductSortingFactory;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.OutputDataHandlerManager;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.quota.QuotaException;
import ro.cs.tao.quota.UserQuotaManager;
import ro.cs.tao.security.SessionStore;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.net.InetAddress;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
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
    private ExecutorService backgroundWorker = new NamedThreadPoolExecutor("query-thread", 1);//Executors.newSingleThreadExecutor();
    private DataSourceComponent dataSourceComponent;
    @Override
    public boolean supports(TaoComponent component) { return component instanceof DataSourceComponent; }

    @Override
    public void execute(DataSourceExecutionTask task) throws ExecutionException {
        try {
            task.setResourceId(UUID.randomUUID().toString());
            logger.fine(String.format("Successfully submitted task with id %s", task.getId()));
            task.setExecutionNodeHostName(InetAddress.getLocalHost().getHostName());
            task.setStartTime(LocalDateTime.now());
            changeTaskStatus(task, ExecutionStatus.RUNNING);
            dataSourceComponent = task.getComponent();
            List<Variable> values = task.getInputParameterValues();
            if (values == null || values.size() == 0) {
                throw new ExecutionException(String.format("No input data for the task %s", task.getId()));
            }
            Variable variable  = values.stream().filter(v -> "query".equals(v.getKey()) ||
                                                             "local.query".equals(v.getKey()))
                                                .findFirst().orElse(null);
            if (variable == null) {
                throw new ExecutionException("Expected parameter [query] not found");
            }
            Query primaryQuery = Query.fromString(variable.getValue());
            if (primaryQuery == null) {
                throw new ExecutionException(String.format("Invalid input data for the task %s", task.getId()));
            }
            DataQuery dataQuery = Query.toDataQuery(primaryQuery);
            Future<List<EOProduct>> future = backgroundWorker.submit(dataQuery::execute);
            List<EOProduct> results = future.get();
            ExecutionStatus status;
            if (results != null && results.size() > 0) {
                int cardinality = dataSourceComponent.getSources().get(0).getCardinality();
                int rSize = results.size();
                if (cardinality > 0 && cardinality != rSize) {
                    variable = values.stream().filter(v -> "remote.query".equals(v.getKey()))
                                               .findFirst().orElse(null);
                    if (variable == null) {
                        logger.warning(String.format("Task %s expected %s inputs, but received %s. Execution is cancelled",
                                                     task.getId(), cardinality, results.size()));
                        markTaskFinished(task, ExecutionStatus.CANCELLED);
                        return;
                    } else {
                        results.removeIf(r -> r.getProductStatus() == ProductStatus.DOWNLOADING ||
                                              r.getProductStatus() == ProductStatus.DOWNLOADED);
                        logger.info(String.format("Task %s expects %d inputs, but only %d are locally available. Download for the missing will start",
                                                  task.getId(), rSize, rSize - results.size()));
                    }
                }
                String sensorName = dataSourceComponent.getSensorName().toLowerCase().replace(" ", "-");
                dataSourceComponent.setProductStatusListener(this);
                List<EOProduct> products = new ArrayList<>();
                if (!dataSourceComponent.getId().contains(LOCAL_DATABASE)) {
                    // first try to retrieve the products that have already been downloaded locally
                    List<EOProduct> localProducts = persistenceManager.getEOProducts(results.stream().map(EOData::getId).collect(Collectors.joining()));
                    if (localProducts != null) {
                        localProducts.removeIf(p -> p.getProductStatus() != ProductStatus.DOWNLOADED);
                        localProducts.forEach(this::downloadCompleted);
                        products.addAll(localProducts);
                        Set<String> ids = localProducts.stream().map(EOData::getId).collect(Collectors.toSet());
                        results.removeIf(p -> ids.contains(p.getId()));
                    }
                }
                final List<EOProduct> remoteProducts = dataSourceComponent.doFetch(results,
                        null,
                        SystemVariable.SHARED_WORKSPACE.value(),
                        ConfigurationManager.getInstance().getValue(String.format("local.%s.path", sensorName)),
                                                                             null);
                if (remoteProducts != null) {
                    if (!dataSourceComponent.getId().contains(LOCAL_DATABASE)) {
                        backgroundWorker.submit(() -> persistResults(remoteProducts));
                    }
                    products.addAll(remoteProducts);
                }
                //if (results.size() > 0) {
                    task.getComponent().setTargetCardinality(products.size());
                    Long workflowNodeId = task.getWorkflowNodeId();
                    WorkflowDescriptor workflow = persistenceManager.getFullWorkflowDescriptor(task.getJob().getWorkflowId());
                    List<WorkflowNodeDescriptor> linkedNodes =
                            workflow.getNodes().stream().filter(n -> n.getIncomingLinks() != null &&
                                                                     n.getIncomingLinks().stream().anyMatch(l -> l.getSourceNodeId() == workflowNodeId))
                                                        .collect(Collectors.toList());
                    Aggregator aggregator;
                    List<Tuple<EOProduct, EOProduct>> tuples = null;
                    for (WorkflowNodeDescriptor linkedNode : linkedNodes) {
                        Set<ComponentLink> links = linkedNode.getIncomingLinks();
                        for (ComponentLink link : links) {
                            if (link.getSourceNodeId() == workflowNodeId && (aggregator = link.getAggregator()) != null) {
                                String[] sort = aggregator.getSorter();
                                if (sort != null) {
                                    DataSorter<EOProduct> sorter = ProductSortingFactory.getSorter(sort[0]);
                                    products = sorter.sort(products, sort[1] == null || sort[1].equalsIgnoreCase("asc"));
                                }
                                String[] group = aggregator.getAssociator();
                                if (group != null) {
                                    Association<EOProduct> association;
                                    if (group.length == 1) {
                                        association = ProductAssociationFactory.getAssociation(group[0]);
                                    } else {
                                        if (group[1] == null) {
                                            group[1] = "0";
                                        }
                                        association = ProductAssociationFactory.getAssociation(group[0],
                                                                                               Integer.parseInt(group[1]));
                                    }
                                    tuples = association.associate(products);
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
                //}
                status = ExecutionStatus.DONE;
            } else {
                logger.info("Query returned no results");
                status = ExecutionStatus.CANCELLED;
            }
            markTaskFinished(task, status);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            markTaskFinished(task, ExecutionStatus.FAILED);
        }
    }

    @Override
    public void stop(DataSourceExecutionTask task) throws ExecutionException {
        if (dataSourceComponent == null || !dataSourceComponent.equals(task.getComponent())) {
            throw new ExecutionException("stop() called on different component");
        }
        dataSourceComponent.cancel();
        markTaskFinished(task, ExecutionStatus.CANCELLED);
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
        this.isInitialized = false;
    }

    @Override
    public String defaultId() { return "QueryExecutor"; }

    private boolean isArray(String value) {
        return value != null && value.startsWith("[") & value.endsWith("]");
    }

    private void persistResults(List<EOProduct> results) {
        OutputDataHandlerManager.getInstance().applyHandlers(results);
    }

    private String serializeResults(List<EOProduct> results) {
        String json = null;
        try {
            StringListAdapter adapter = new StringListAdapter();
            json = adapter.unmarshal(results.stream().map(EOProduct::getLocation)
                            .collect(Collectors.toList()));
        } catch (Exception e) {
            logger.severe("Serialization of results failed: " + e.getMessage());
        }
        return json;
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

    @Override
    public boolean downloadStarted(EOProduct product) {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	
        try {
        	// check input quota before download
        	if (product.getRefs() != null && !product.getRefs().contains(principal.getName()) && !UserQuotaManager.getInstance().checkUserInputQuota(principal, product.getApproximateSize())) {
        		// do not allow for the download to start
        		return false;
        	}
        	
        	// check if the product already exists
        	final EOProduct oldProd = persistenceManager.getEOProduct(product.getId());
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
            persistenceManager.saveEOProduct(product);
            
            // update the user's input quota
            UserQuotaManager.getInstance().updateUserInputQuota(principal);
            return true;
        } catch (PersistenceException e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
            return false;
        } catch (QuotaException  e) {
        	logger.severe(String.format("Cannot update the input quota for user %s. Reason: %s", principal.getName(), e.getMessage()));
        	return false;
        }
    }

    @Override
    public void downloadCompleted(EOProduct product) {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
        try {
        	// re-update the references, in case some other user tried to download this product after 
        	// the current user started
        	final EOProduct oldProd = persistenceManager.getEOProduct(product.getId());
        	if (oldProd != null) {
        		product.setRefs(oldProd.getRefs());
        	}
        	
            product.setProductStatus(ProductStatus.DOWNLOADED);
            // update the product's reference
            product.addReference(principal.getName());
            persistenceManager.saveEOProduct(product);
            
            // update the user's input quota
            UserQuotaManager.getInstance().updateUserInputQuota(principal);
        } catch (PersistenceException e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
        } catch (QuotaException  e) {
        	logger.severe(String.format("Cannot update the input quota for user %s. Reason: %s", principal.getName(), e.getMessage()));
        }
    }

    @Override
    public void downloadFailed(EOProduct product, String reason) {
    	final Principal principal = SessionStore.currentContext().getPrincipal();
    	
        try {
            product.setProductStatus(ProductStatus.FAILED);
            product.removeReference(principal.getName());
            persistenceManager.saveEOProduct(product);
            // roll back the user's quota
            UserQuotaManager.getInstance().updateUserInputQuota(principal);
            logger.warning(String.format("Product %s not downloaded. Reason: %s", product.getName(), reason));
        } catch (PersistenceException e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
        } catch (QuotaException  e) {
        	logger.severe(String.format("Cannot update the input quota for user %s. Reason: %s", principal.getName(), e.getMessage()));
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
}
