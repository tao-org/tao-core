package ro.cs.tao.services.model.component;

import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceComponentGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Read-only bean for a DataSourceComponentGroup entity
 */
public class DataSourceGroupInfo {

    private String id;
    private String label;
    private String version;
    private String description;
    private String authors;
    private String copyright;
    private String nodeAffinity;
    private List<String> tags;
    private List<DataSourceInfo> dataSourceComponents;

    public DataSourceGroupInfo(DataSourceComponentGroup group) {
        this.id = group.getId();
        this.label = group.getLabel();
        this.version = group.getVersion();
        this.description = group.getDescription();
        this.authors = group.getAuthors();
        this.copyright = group.getCopyright();
        this.nodeAffinity = group.getNodeAffinity();
        this.tags = group.getTags();
        List<DataSourceComponent> dataSourceComponents = group.getDataSourceComponents();
        if (dataSourceComponents != null) {
            this.dataSourceComponents = new ArrayList<>();
            for (DataSourceComponent component : dataSourceComponents) {
                this.dataSourceComponents.add(new DataSourceInfo(component));
            }
        }
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

    public List<String> getTags() { return tags; }

    public List<DataSourceInfo> getDataSourceComponents() { return dataSourceComponents; }
}
