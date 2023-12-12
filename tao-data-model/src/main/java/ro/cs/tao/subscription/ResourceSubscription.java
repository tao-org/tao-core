package ro.cs.tao.subscription;

import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.topology.NodeFlavor;

import java.time.LocalDateTime;

public class ResourceSubscription extends LongIdentifiable {
    private String userId;
    private SubscriptionType type;
    private NodeFlavor flavor;
    private int flavorQuantity;
    private Integer flavorHddSizeGB;
    private Integer flavorSsdSizeGB;
    private Integer objectStorageGB;
    private boolean paid;
    private LocalDateTime created;
    private LocalDateTime ended;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SubscriptionType getType() {
        return type;
    }

    public void setType(SubscriptionType type) {
        this.type = type;
    }

    public NodeFlavor getFlavor() {
        return flavor;
    }

    public void setFlavor(NodeFlavor flavor) {
        this.flavor = flavor;
    }

    public int getFlavorQuantity() {
        return flavorQuantity;
    }

    public void setFlavorQuantity(int flavorQuantity) {
        this.flavorQuantity = flavorQuantity;
    }

    public Integer getFlavorHddSizeGB() {
        return flavorHddSizeGB;
    }

    public void setFlavorHddSizeGB(Integer flavorHddSizeGB) {
        this.flavorHddSizeGB = flavorHddSizeGB;
    }

    public Integer getFlavorSsdSizeGB() {
        return flavorSsdSizeGB;
    }

    public void setFlavorSsdSizeGB(Integer flavorSsdSizeGB) {
        this.flavorSsdSizeGB = flavorSsdSizeGB;
    }

    public Integer getObjectStorageGB() {
        return objectStorageGB;
    }

    public void setObjectStorageGB(Integer objectStorageGB) {
        this.objectStorageGB = objectStorageGB;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public LocalDateTime getEnded() {
        return ended;
    }

    public void setEnded(LocalDateTime ended) {
        this.ended = ended;
    }
}
