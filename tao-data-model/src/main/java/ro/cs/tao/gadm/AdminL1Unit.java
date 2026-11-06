package ro.cs.tao.gadm;

import org.locationtech.jts.geom.Geometry;
import ro.cs.tao.component.Identifiable;

import java.util.List;

public class AdminL1Unit implements Identifiable<String> {
    private String id;
    private String name;
    private String countryCode;
    private Geometry extent;
    private List<AdminL2Unit> units;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String defaultId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Geometry getExtent() {
        return extent;
    }

    public void setExtent(Geometry extent) {
        this.extent = extent;
    }

    public List<AdminL2Unit> getUnits() {
        return units;
    }

    public void setUnits(List<AdminL2Unit> units) {
        this.units = units;
    }
}
