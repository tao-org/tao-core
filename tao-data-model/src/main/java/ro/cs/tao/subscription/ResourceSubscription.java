package ro.cs.tao.subscription;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class ResourceSubscription extends LongIdentifiable {
    private String userId;
    private SubscriptionType type;
    private Map<String, FlavorSubscription> flavors;
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

    public Map<String, FlavorSubscription> getFlavors() {
        return flavors;
    }

    public void setFlavors(Map<String, FlavorSubscription> flavors) {
        this.flavors = flavors;
    }

    public void addFlavor(FlavorSubscription flavorSubscription) {
        if (this.flavors == null) {
            this.flavors = new LinkedHashMap<>();
        }
        this.flavors.put(flavorSubscription.getFlavorId(), flavorSubscription);
    }

    public void removeFlavor(String flavorId) {
        if (this.flavors != null) {
            this.flavors.remove(flavorId);
        }
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
