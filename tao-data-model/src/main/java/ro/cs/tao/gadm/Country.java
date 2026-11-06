package ro.cs.tao.gadm;

import org.locationtech.jts.geom.Geometry;
import ro.cs.tao.component.Identifiable;

import java.util.List;

public class Country implements Identifiable<String> {
    private String iso3Code;
    private String name;
    private Double latitude;
    private Double longitude;
    private Geometry extent;
    private boolean enabled;
    private List<AdminL1Unit> units;

    @Override
    public String getId() {
        return iso3Code;
    }

    @Override
    public void setId(String id) {
        this.iso3Code = id;
    }

    @Override
    public String defaultId() {
        return null;
    }

    public String getIso3Code() {
        return iso3Code;
    }

    public void setIso3Code(String iso3Code) {
        this.iso3Code = iso3Code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Geometry getExtent() {
        return extent;
    }

    public void setExtent(Geometry extent) {
        this.extent = extent;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<AdminL1Unit> getUnits() {
        return units;
    }

    public void setUnits(List<AdminL1Unit> units) {
        this.units = units;
    }
}
