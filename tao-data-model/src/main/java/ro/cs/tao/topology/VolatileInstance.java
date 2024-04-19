package ro.cs.tao.topology;

import ro.cs.tao.component.LongIdentifiable;

import javax.xml.bind.annotation.XmlRootElement;
import java.beans.Transient;
import java.time.Duration;
import java.time.LocalDateTime;

@XmlRootElement(name = "instance")
public class VolatileInstance extends LongIdentifiable {
    private String nodeId;
    private String flavorId;
    private String userId;
    private LocalDateTime created;
    private LocalDateTime destroyed;
    private double averageCPU;
    private double averageMemory;

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getFlavorId() {
        return flavorId;
    }

    public void setFlavorId(String flavorId) {
        this.flavorId = flavorId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getDestroyed() {
        return destroyed;
    }

    public void setDestroyed(LocalDateTime destroyed) {
        this.destroyed = destroyed;
    }

    public double getAverageCPU() {
        return averageCPU;
    }

    public void setAverageCPU(double averageCPU) {
        this.averageCPU = averageCPU;
    }

    public double getAverageMemory() {
        return averageMemory;
    }

    public void setAverageMemory(double averageMemory) {
        this.averageMemory = averageMemory;
    }

    @Transient
    public double getDuration() {
        return this.destroyed != null
               // fractional number of hours
               ? (double) Duration.between(this.created, this.destroyed).toSeconds() / 3600.0
               : Double.NaN;
    }
}
