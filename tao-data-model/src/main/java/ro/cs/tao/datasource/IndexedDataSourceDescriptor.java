package ro.cs.tao.datasource;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;

/**
 * Represents a descriptor for an indexed data source containing metadata
 * such as the name of the data source, associated sensor, time range,
 * and additional configuration parameters.
 *
 * This class extends {@code LongIdentifiable} to uniquely identify the
 * descriptor using a long-based identifier.
 */
public class IndexedDataSourceDescriptor extends LongIdentifiable {
    private String dataSourceName;
    private String sensorName;
    private int fromYear;
    private int toYear;
    private LocalDateTime lastRun;
    private LinkedHashMap<String, String> parameters;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public int getFromYear() {
        return fromYear;
    }

    public void setFromYear(int fromYear) {
        this.fromYear = fromYear;
    }

    public int getToYear() {
        return toYear;
    }

    public void setToYear(int toYear) {
        this.toYear = toYear;
    }

    public LocalDateTime getLastRun() {
        return lastRun;
    }

    public void setLastRun(LocalDateTime lastRun) {
        this.lastRun = lastRun;
    }

    public LinkedHashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(LinkedHashMap<String, String> parameters) {
        this.parameters = parameters;
    }

}
