package ro.cs.tao.datasource.param;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Descriptor for indexed data source parameter.
 *
 * @author dstefanescu
 */
public class IndexedDataSourceParameter {
    private Map<String,String> authentication;
    private String paramsPrefix;
    private Map<String, Map<String,Object>> sensor;

    public Map<String, String> getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Map<String, String> authentication) {
        this.authentication = authentication;
    }

    public String getParamsPrefix() {return paramsPrefix; }

    public Map<String, Map<String, Object>> getSensor() {
        return sensor;
    }

    public Map<String, Object> getSensor(String name) {
        return sensor != null ? sensor.get(name) : null;
    }

    public void setSensor(Map<String, Map<String, Object>> sensor) {
        this.sensor = sensor;
    }
}
