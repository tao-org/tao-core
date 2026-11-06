package ro.cs.tao.topology;

import jakarta.xml.bind.annotation.XmlRootElement;
import ro.cs.tao.component.LongIdentifiable;

import java.beans.Transient;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Represents a volatile instance associated with a node in a system topology, identified by its long type ID.
 * This class extends {@code LongIdentifiable} and provides additional information specific to the instance, such as:
 * - The associated node ID.
 * - The flavor ID of the instance, which may correlate to hardware specifications.
 * - The user ID that created or manages this instance.
 * - Timestamps for when the instance was created and destroyed.
 * - Metrics related to performance, such as average CPU and memory usage.
 *
 * A volatile instance has transient behaviors and is typically removed or destroyed after a span of time.
 * The duration of its existence (time between creation and destruction) can be computed in terms of fractional hours.
 *
 * Instances of this class are serialized to XML using the {@code @XmlRootElement} annotation with "instance" as its root element.
 */
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

