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
import ro.cs.tao.datasource.DataSourceManager;
import ro.cs.tao.datasource.QueryException;
import ro.cs.tao.datasource.param.ParameterDescriptor;
import ro.cs.tao.datasource.param.QueryParameter;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.execution.ExecutionException;
import ro.cs.tao.execution.Executor;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.serialization.GenericAdapter;
import ro.cs.tao.serialization.MapAdapter;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class QueryExecutor extends Executor<DataSourceExecutionTask> {

    private ExecutorService backgroundWorker = Executors.newSingleThreadExecutor();
    private DataSourceComponent dataSourceComponent;
    @Override
    public boolean supports(TaoComponent component) { return component instanceof DataSourceComponent; }

    @Override
    public void execute(DataSourceExecutionTask task) throws ExecutionException {
        try {
            task.setResourceId(UUID.randomUUID().toString());
            changeTaskStatus(task, ExecutionStatus.RUNNING);
            dataSourceComponent = task.getComponent();
            final Map<String, ParameterDescriptor> parameterDescriptorMap =
                    DataSourceManager.getInstance()
                            .getSupportedParameters(dataSourceComponent.getSensorName(),
                                    dataSourceComponent.getDataSourceName());
            DataQuery dataQuery = dataSourceComponent.createQuery();
            //List<Parameter> parameters = dataSourceComponent.getOverriddenParameters();
            List<Variable> values = task.getInputParameterValues();
            for (Variable entry : values) {
                final String paramName = entry.getKey();
                final ParameterDescriptor descriptor = parameterDescriptorMap.get(paramName);
                if (descriptor == null) {
                    throw new QueryException(String.format("Parameter [%s] not supported by data source '%s' for sensor '%s'",
                            paramName,
                            dataSourceComponent.getDataSourceName(),
                            dataSourceComponent.getSensorName()));
                }
                final Class type = descriptor.getType();
                String value = entry.getValue();
                final QueryParameter queryParameter;
                if (isArray(value)) {
                    String[] elements = value.substring(0, value.length() - 1).split(",");
                    if (Date.class.isAssignableFrom(type)) {
                        queryParameter = dataQuery.createParameter(paramName,
                                type,
                                new SimpleDateFormat("yyyy-MM-dd").parse(elements[0]),
                                new SimpleDateFormat("yyyy-MM-dd").parse(elements[1]));
                    } else {
                        Object array = Array.newInstance(type, elements.length);
                        GenericAdapter adapter = new GenericAdapter(type.getName());
                        for (int i = 0; i < elements.length; i++) {
                            Array.set(array, i, adapter.marshal(elements[i]));
                        }
                        queryParameter = dataQuery.createParameter(paramName, type, array);
                    }
                } else {
                    queryParameter = dataQuery.createParameter(paramName,
                            type,
                            Date.class.isAssignableFrom(type) ?
                                    new SimpleDateFormat("yyyy-MM-dd").parse(value)
                                    : value);
                }
                dataQuery.addParameter(queryParameter);
            }
            final Future<List<EOProduct>> future = backgroundWorker.submit(dataQuery::execute);
            List<EOProduct> results = future.get();
            if (results != null && results.size() > 0) {
                String sensorName = dataSourceComponent.getSensorName().toLowerCase().replace(" ", "-");
                final List<EOProduct> products = dataSourceComponent.doFetch(results,
                        null,
                        ConfigurationManager.getInstance().getValue("product.location"),
                        ConfigurationManager.getInstance().getValue(String.format("local.%s.path", sensorName)));
                if (products != null) {
                    backgroundWorker.submit(() -> persistResults(results));
                    task.getComponent().setTargetCardinality(results.size());
                    task.setOutputParameterValue(dataSourceComponent.getTargets().get(0).getName(),
                                                 serializeResults(results));
                }
            }
            markTaskFinished(task, ExecutionStatus.DONE);
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
    public String defaultName() { return "QueryExecutor"; }

    private boolean isArray(String value) {
        return value != null && value.startsWith("[") & value.endsWith("]");
    }

    private void persistResults(List<EOProduct> results) {
        for (EOProduct product : results) {
            try {
                persistenceManager.saveEOProduct(product);
            } catch (Exception e) {
                logger.severe(String.format("Product %s could not be written to database: %s",
                                            product.getName(), e.getMessage()));
            }
        }
    }

    private String serializeResults(List<EOProduct> results) {
        String json = null;
        try {
            MapAdapter mapAdapter = new MapAdapter();
            json = mapAdapter.unmarshal(results.stream()
                            .collect(Collectors.toMap(EOProduct::getName, EOProduct::getLocation)));
        } catch (Exception e) {
            logger.severe("Serialization of results failed: " + e.getMessage());
        }
        return json;
    }

}
