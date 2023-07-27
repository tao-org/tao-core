package ro.cs.tao.workspaces;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiPolygon;
import ro.cs.tao.component.StringIdentifiable;
import ro.cs.tao.serialization.GeometryAdapter;

import java.time.LocalDateTime;

public class Site extends StringIdentifiable {
    private String name;
    private String description;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Geometry footprint;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getFootprint() {
        try {
            return new GeometryAdapter().unmarshal(footprint);
        } catch (Exception e) {
            return null;
        }
    }
    public void setFootprint(String geometryAsText) {
        try {
            this.footprint = new GeometryAdapter().marshal(geometryAsText);
            if (this.footprint instanceof MultiPolygon && this.footprint.getNumGeometries() == 1) {
                this.footprint = this.footprint.getGeometryN(0);
            }
        } catch (Exception ignored) { }
    }
}
