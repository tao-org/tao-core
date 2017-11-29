package ro.cs.tao.services.model.datasource;

import ro.cs.tao.datasource.param.ParameterDescriptor;

import java.util.Map;

/**
 * @author Cosmin Cara
 */
public class DataSourceInstance {
    private String sensor;
    private String dataSourceName;
    private Map<String, ParameterDescriptor> parameters;

    public DataSourceInstance() {
    }

    public DataSourceInstance(String sensor, String dataSourceName, Map<String, ParameterDescriptor> parameters) {
        this.sensor = sensor;
        this.dataSourceName = dataSourceName;
        this.parameters = parameters;
    }

    public String getSensor() {
        return sensor;
    }

    public void setSensor(String sensor) {
        this.sensor = sensor;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public Map<String, ParameterDescriptor> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, ParameterDescriptor> parameters) {
        this.parameters = parameters;
    }
}
