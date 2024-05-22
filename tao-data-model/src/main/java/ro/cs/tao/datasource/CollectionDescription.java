package ro.cs.tao.datasource;

import ro.cs.tao.eodata.enums.SensorType;

public class CollectionDescription {
    private String mission;
    private SensorType sensorType;
    private String description;
    private String temporalCoverage;
    private String spatialCoverage;
    private Boolean tokenNeeded;

    public String getMission() {
        return mission;
    }

    public void setMission(String mission) {
        this.mission = mission;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemporalCoverage() {
        return temporalCoverage;
    }

    public void setTemporalCoverage(String temporalCoverage) {
        this.temporalCoverage = temporalCoverage;
    }

    public String getSpatialCoverage() {
        return spatialCoverage;
    }

    public void setSpatialCoverage(String spatialCoverage) {
        this.spatialCoverage = spatialCoverage;
    }

    public Boolean isTokenNeeded() {
        return tokenNeeded != null ? tokenNeeded : false;
    }

    public void setTokenNeeded(boolean tokenNeeded) {
        this.tokenNeeded = tokenNeeded;
    }
}
