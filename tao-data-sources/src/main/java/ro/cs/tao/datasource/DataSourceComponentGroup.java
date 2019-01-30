package ro.cs.tao.datasource;

import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Group for several data source components.
 * Such components are created when building data sets consisting of several queries and/or product types.
 *
 * @author Cosmin Cara
 */
public class DataSourceComponentGroup extends TaoComponent {
   private List<DataSourceComponent> dataSourceComponents;

    public List<DataSourceComponent> getDataSourceComponents() {
        return dataSourceComponents;
    }

    public void setDataSourceComponents(List<DataSourceComponent> dataSourceComponents) {
        this.dataSourceComponents = dataSourceComponents;
        if (this.dataSourceComponents != null) {
            for (DataSourceComponent component : this.dataSourceComponents) {
                transferDescriptors(component);
            }
        }
    }

    public void addDataSourceComponent(DataSourceComponent child) {
        if (this.dataSourceComponents == null) {
            this.dataSourceComponents = new ArrayList<>();
        }
        if (child != null && child.getId() != null) {
            if (this.dataSourceComponents.stream().noneMatch(d -> d.getId().equals(child.getId()))) {
                this.dataSourceComponents.add(child);
                transferDescriptors(child);
            }
        }
    }

    public void removeDataSourceComponent(DataSourceComponent child) {
        if (this.dataSourceComponents != null && child != null && child.getId() != null) {
            this.dataSourceComponents.removeIf(d -> d.getId().equals(child.getId()));
            removeDescriptors(child);
        }
    }

    private void transferDescriptors(DataSourceComponent fromComponent) {
        List<SourceDescriptor> childSources = fromComponent.getSources();
        if (childSources != null) {
            for (SourceDescriptor source : childSources) {
                addSource(source.clone());
            }
        }
        List<TargetDescriptor> childTargets = fromComponent.getTargets();
        if (childTargets != null) {
            for (TargetDescriptor target : childTargets) {
                addTarget(target.clone());
            }
        }
    }

    private void removeDescriptors(DataSourceComponent fromComponent) {
        List<SourceDescriptor> childSources;
        if (fromComponent != null && this.dataSourceComponents != null && this.sources != null &&
                ((childSources = fromComponent.getSources()) != null)) {
            this.sources.removeIf(s -> childSources.stream().anyMatch(cs -> cs.getId().equals(s.getId())));
        }
        List<TargetDescriptor> childTargets;
        if (fromComponent != null && this.dataSourceComponents != null && this.targets != null &&
                ((childTargets = fromComponent.getTargets()) != null)) {
            this.targets.removeIf(s -> childTargets.stream().anyMatch(cs -> cs.getId().equals(s.getId())));
        }
    }
}
