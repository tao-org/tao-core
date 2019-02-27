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

import ro.cs.tao.component.Aggregator;
import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.ProductStatusListener;
import ro.cs.tao.datasource.beans.Query;
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
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.utils.Tuple;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.net.InetAddress;
import java.time.LocalDateTime;
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
                final List<EOProduct> products = dataSourceComponent.doFetch(results,
                        null,
                        ConfigurationManager.getInstance().getValue("product.location"),
                        ConfigurationManager.getInstance().getValue(String.format("local.%s.path", sensorName)),
                                                                             null);
                if (products != null) {
                    if (!dataSourceComponent.getId().contains("Local Database")) {
                        backgroundWorker.submit(() -> persistResults(products));
                    }
                    task.getComponent().setTargetCardinality(results.size());
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
                                    results = sorter.sort(products, sort[1] == null || sort[1].equalsIgnoreCase("asc"));
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
                                    tuples = association.associate(results);
                                }
                            }
                        }
                    }
                    if (tuples != null) {
                        task.setOutputParameterValue(dataSourceComponent.getTargets().get(0).getName(),
                                                     serializeGroups(tuples));
                    } else {
                        task.setOutputParameterValue(dataSourceComponent.getTargets().get(0).getName(),
                                                     serializeResults(results));
                    }
                }
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
        product.setProductStatus(ProductStatus.DOWNLOADING);
        try {
            persistenceManager.saveEOProduct(product);
            return true;
        } catch (PersistenceException e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
            return false;
        }
    }

    @Override
    public void downloadCompleted(EOProduct product) {
        product.setProductStatus(ProductStatus.DOWNLOADED);
        try {
            persistenceManager.saveEOProduct(product);
        } catch (PersistenceException e) {
            logger.severe(String.format("Updating product %s failed. Reason: %s", product.getName(), e.getMessage()));
        }
    }

    @Override
    public void downloadFailed(EOProduct product, String reason) {
        product.setProductStatus(ProductStatus.FAILED);
        try {
            persistenceManager.saveEOProduct(product);
            logger.warning(String.format("Product %s not downloaded. Reason: %s", product.getName(), reason));
        } catch (PersistenceException e) {
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
}
