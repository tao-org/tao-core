package ro.cs.tao.services.model.component;

import ro.cs.tao.datasource.DataSourceComponent;

import java.util.List;

/**
 * Read-only bean for a DataSourceComponent entity
 */
public class DataSourceInfo {

    private String id;
    private String label;
    private String version;
    private String description;
    private String authors;
    private String copyright;
    private String nodeAffinity;
    private String sensorName;
    private String dataSourceName;
    private List<String> tags;

    public DataSourceInfo(DataSourceComponent component) {
        this.id = component.getId();
        this.label = component.getLabel();
        this.version = component.getVersion();
        this.description = component.getDescription();
        this.authors = component.getAuthors();
        this.copyright = component.getCopyright();
        this.nodeAffinity = component.getNodeAffinity() != null ? component.getNodeAffinity().getValue() : null;
        this.sensorName = component.getSensorName();
        this.dataSourceName = component.getDataSourceName();
        this.tags = component.getTags();
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public String getAuthors() {
        return authors;
    }

    public String getCopyright() {
        return copyright;
    }

    public String getNodeAffinity() {
        return nodeAffinity;
    }

    public String getSensorName() {
        return sensorName;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public List<String> getTags() { return tags; }
}
