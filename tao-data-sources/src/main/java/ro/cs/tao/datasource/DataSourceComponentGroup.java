package ro.cs.tao.datasource;

import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.beans.Query;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Group for several data source components.
 * Such components are created when building data sets consisting of several queries and/or product types.
 *
 * @author Cosmin Cara
 */
public class DataSourceComponentGroup extends TaoComponent {
    private String userName;
    private List<DataSourceComponent> dataSourceComponents;
    private Set<Query> dataSourceQueries;

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public List<DataSourceComponent> getDataSourceComponents() {
        return dataSourceComponents;
    }

    public void setDataSourceComponents(List<DataSourceComponent> dataSourceComponents) {
        this.dataSourceComponents = dataSourceComponents;
        /*if (this.dataSourceComponents != null) {
            for (DataSourceComponent component : this.dataSourceComponents) {
                transferDescriptors(component);
            }
        }*/
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

    public Set<Query> getDataSourceQueries() {  return dataSourceQueries; }
    public void setDataSourceQueries(Set<Query> dataSourceQueries) { this.dataSourceQueries = dataSourceQueries; }

    public void addQuery(Query query, String sourceId) {
        if (this.dataSourceQueries == null) {
            this.dataSourceQueries = new LinkedHashSet<>();
        }
        if (query == null) {
            throw new NullPointerException("[query]");
        }
        if (this.dataSourceComponents == null) {
            throw new IllegalArgumentException(String.format("No data source found in this group for sensor %s", query.getSensor()));
        }
        List<DataSourceComponent> components = this.dataSourceComponents.stream()
                .filter(d -> d.getSources().get(0).getId().equals(sourceId)).collect(Collectors.toList());
        if (components.size() == 1) {
            if (this.dataSourceQueries.stream().noneMatch(q -> q.getId().equals(query.getId()))) {
                this.dataSourceQueries.add(query);
            } else {
                throw new IllegalArgumentException(String.format("There is already a query for the sensor '%s'",
                                                                 query.getSensor()));
            }
        } else {
            throw new IllegalArgumentException(String.format("Either there is already a query for the sensor '%s' or no data source found in this group for it",
                                                             query.getSensor()));
        }
    }

    public void removeQuery(long queryId) {
        if (this.dataSourceQueries != null) {
            this.dataSourceQueries.removeIf(q -> q.getId().equals(queryId));
        }
    }

    private void transferDescriptors(DataSourceComponent fromComponent) {
        List<SourceDescriptor> childSources = fromComponent.getSources();
        if (childSources != null) {
            for (SourceDescriptor source : childSources) {
                SourceDescriptor clone = source.clone();
                clone.setName(source.getId());
                addSource(clone);
            }
        }
        List<TargetDescriptor> childTargets = fromComponent.getTargets();
        if (childTargets != null) {
            for (TargetDescriptor target : childTargets) {
                TargetDescriptor clone = target.clone();
                clone.setName(target.getId());
                try {
                    clone.addConstraint(fromComponent.getSensorName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                addTarget(clone);
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
