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

package ro.cs.tao.execution.local;

import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.Variable;
import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.datasource.DataQuery;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.OutputDataHandlerManager;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.Query;
import ro.cs.tao.serialization.StringListAdapter;
import ro.cs.tao.utils.executors.NamedThreadPoolExecutor;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class QueryExecutor extends Executor<DataSourceExecutionTask> {

    private ExecutorService backgroundWorker = new NamedThreadPoolExecutor("query-thread", 1);//Executors.newSingleThreadExecutor();
    private DataSourceComponent dataSourceComponent;
    @Override
    public boolean supports(TaoComponent component) { return component instanceof DataSourceComponent; }

    @Override
    public void execute(DataSourceExecutionTask task) throws ExecutionException {
        try {
            task.setResourceId(UUID.randomUUID().toString());
            logger.fine(String.format("Succesfully submitted task with id %s", task.getResourceId()));
            task.setExecutionNodeHostName(InetAddress.getLocalHost().getHostName());
            task.setStartTime(LocalDateTime.now());
            changeTaskStatus(task, ExecutionStatus.RUNNING);
            dataSourceComponent = task.getComponent();
            List<Variable> values = task.getInputParameterValues();
            if (values == null || values.size() == 0) {
                throw new ExecutionException("No input data for the task");
            }
            Query query = Query.fromString(values.get(0).getValue());
            if (query == null) {
                throw new ExecutionException("Invalid input data for the task");
            }
            DataQuery dataQuery = Query.toDataQuery(query);
            final Future<List<EOProduct>> future = backgroundWorker.submit(dataQuery::execute);
            List<EOProduct> results = future.get();
            ExecutionStatus status;
            if (results != null && results.size() > 0) {
                String sensorName = dataSourceComponent.getSensorName().toLowerCase().replace(" ", "-");
                final List<EOProduct> products = dataSourceComponent.doFetch(results,
                        null,
                        ConfigurationManager.getInstance().getValue("product.location"),
                        ConfigurationManager.getInstance().getValue(String.format("local.%s.path", sensorName)),
                                                                             null);
                if (products != null) {
                    backgroundWorker.submit(() -> persistResults(results));
                    task.getComponent().setTargetCardinality(results.size());
                    task.setOutputParameterValue(dataSourceComponent.getTargets().get(0).getName(),
                                                 serializeResults(results));
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
        /*for (EOProduct product : results) {
            try {
                persistenceManager.saveEOProduct(product);
            } catch (Exception e) {
                logger.severe(String.format("Product %s could not be written to database: %s",
                                            product.getName(), e.getMessage()));
            }
        }*/
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

}
