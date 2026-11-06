package ro.cs.tao.gadm;

import org.locationtech.jts.geom.Geometry;
import ro.cs.tao.component.Identifiable;

public class AdminL2Unit implements Identifiable<String> {
    private String id;
    private String name;
    private String adminL1Id;
    private Geometry extent;

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

    public String getAdminL1Id() {
        return adminL1Id;
    }

    public void setAdminL1Id(String adminL1Id) {
        this.adminL1Id = adminL1Id;
    }

    public Geometry getExtent() {
        return extent;
    }

    public void setExtent(Geometry extent) {
        this.extent = extent;
    }
}
