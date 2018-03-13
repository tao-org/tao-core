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
import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.execution.model.ExecutionTask;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryExecutor extends Executor {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private ExecutorService backgroundWorker = Executors.newSingleThreadExecutor();
    private DataSourceComponent dataSourceComponent;

    @Override
    public boolean supports(TaoComponent component) { return component instanceof DataSourceComponent; }

    @Override
    public void execute(ExecutionTask task) throws ExecutionException {
        dataSourceComponent = (DataSourceComponent) task.getProcessingComponent();
        List<Variable> values = task.getInputParameterValues();
        Variable user = values.stream().filter(v -> USERNAME.equalsIgnoreCase(v.getKey())).findFirst().orElse(null);
        if (user != null) {
            Variable pwd = values.stream().filter(v -> PASSWORD.equalsIgnoreCase(v.getKey())).findFirst().orElse(null);
            if (pwd != null) {
                dataSourceComponent.setUserCredentials(user.getValue(), pwd.getValue());
                values.remove(user);
                values.remove(pwd);
            }
        }
        try {
            final Map<String, ParameterDescriptor> parameterDescriptorMap =
                    DataSourceManager.getInstance()
                            .getSupportedParameters(dataSourceComponent.getSensorName(),
                                    dataSourceComponent.getDataSourceName());
            DataQuery query = dataSourceComponent.createQuery();
            for (Variable entry : values) {
                final ParameterDescriptor descriptor = parameterDescriptorMap.get(entry.getKey());
                if (descriptor == null) {
                    throw new QueryException(String.format("Parameter [%s] not supported by data source '%s' for sensor '%s'",
                            entry.getKey(),
                            dataSourceComponent.getDataSourceName(),
                            dataSourceComponent.getSensorName()));
                }
                final Class type = descriptor.getType();
                String value = entry.getValue();
                final QueryParameter queryParameter;
                if (value != null && isArray(value)) {
                    String[] elements = value.substring(0, value.length() - 1).split(",");
                    queryParameter = query.createParameter(entry.getKey(),
                            type,
                            Date.class.isAssignableFrom(type) ?
                                    new SimpleDateFormat("yyyy-MM-dd").parse(elements[0])
                                    : Array.get(value, 0),
                            Date.class.isAssignableFrom(type) ?
                                    new SimpleDateFormat("yyyy-MM-dd").parse(elements[1])
                                    : Array.get(value, 1));
                } else {
                    queryParameter = query.createParameter(entry.getKey(),
                            type,
                            Date.class.isAssignableFrom(type) ?
                                    new SimpleDateFormat("yyyy-MM-dd").parse(value)
                                    : entry.getValue());
                }
                query.addParameter(queryParameter);
            }
            final Future<List<EOProduct>> future = backgroundWorker.submit(query::execute);
            List<EOProduct> results = future.get();
            if (results != null && results.size() > 0) {
                String sensorName = dataSourceComponent.getSensorName().toLowerCase().replace(" ", "-");
                dataSourceComponent.doFetch(results,
                            null,
                            ConfigurationManager.getInstance().getValue("product.location"),
                            ConfigurationManager.getInstance().getValue(String.format("local.%s.path", sensorName)));
            }
            markTaskFinished(task, ExecutionStatus.DONE);
        } catch (Exception ex) {
            logger.severe(ex.getMessage());
            markTaskFinished(task, ExecutionStatus.FAILED);
        }
    }

    @Override
    public void stop(ExecutionTask task) throws ExecutionException {
        if (dataSourceComponent == null || !dataSourceComponent.equals(task.getProcessingComponent())) {
            throw new ExecutionException("stop() called on different component");
        }
        dataSourceComponent.cancel();
        markTaskFinished(task, ExecutionStatus.CANCELLED);
    }

    @Override
    public void suspend(ExecutionTask task) throws ExecutionException {
        throw new ExecutionException("suspend() not supported on data sources");
    }

    @Override
    public void resume(ExecutionTask task) throws ExecutionException {
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
}
