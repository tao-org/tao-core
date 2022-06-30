package ro.cs.tao.services.model.component;

import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.datasource.DataSourceComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSetInfo {

    private String id;
    private String label;
    private String description;
    private List<String> products;
    private List<String> tags;

    public ProductSetInfo() {
    }

    public ProductSetInfo(DataSourceComponent component) {
        this.id = component.getId();
        this.label = component.getLabel();
        this.description = component.getDescription();
        this.tags = component.getTags();
        final SourceDescriptor descriptor = component.getSources().stream().filter(s -> DataSourceComponent.QUERY_PARAMETER.equals(s.getName())).findFirst().orElse(null);
        this.products = new ArrayList<>();
        if (descriptor != null) {
            String location = descriptor.getDataDescriptor().getLocation();
            if (location != null) {
                this.products.addAll(Arrays.stream(location.split(",")).collect(Collectors.toList()));
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTags() { return tags; }

    public List<String> getProducts() {
        return products;
    }
}
