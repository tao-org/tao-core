package ro.cs.tao.services.model.event;

public class ExternalEventNotification {

    // event identifier (it will correspond to the generated product ID)
    private String id;
    // the collection that corresponds to the generated product
    private String collection;
    // one of: Flood, Draught, Normal condition
    private String feature;
    // product footprint (GeoJSON)
    private String geometry;

    public ExternalEventNotification(String id, String collection, String feature, String geometry) {
        this.id = id;
        this.collection = collection;
        this.feature = feature;
        this.geometry = geometry;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", collection='" + collection + '\'' +
                ", feature='" + feature + '\'' +
                ", geometry=" + geometry +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
