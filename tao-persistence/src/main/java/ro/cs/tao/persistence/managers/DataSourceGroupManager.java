package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.cs.tao.component.SourceDescriptor;
import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.persistence.repository.DataSourceGroupRepository;
import ro.cs.tao.persistence.repository.SourceDescriptorRepository;
import ro.cs.tao.persistence.repository.TargetDescriptorRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("dataSourceGroupManager")
public class DataSourceGroupManager extends TaoComponentManager<DataSourceComponentGroup, DataSourceGroupRepository> {

    @Autowired
    private SourceDescriptorRepository sourceDescriptorRepository;

    @Autowired
    private TargetDescriptorRepository targetDescriptorRepository;

    public List<DataSourceComponentGroup> getUserDataSourceComponents(String userName) {
        return userName != null ? this.repository.getUserDataSourceComponentGroups(userName) :
                this.repository.getDataSourceComponentGroups();
    }

    public DataSourceComponentGroup getDataSourceComponentByLabel(String label) {
        List<DataSourceComponentGroup> components = this.repository.getDataSourceComponentGroupByLabel(label);
        return (components == null || components.size() == 0) ? null : components.get(0);
    }

    @Override
    public DataSourceComponentGroup get(String id) {
        DataSourceComponentGroup group = this.repository.getById(id);
        if (group != null) {
            // workaround for two eager collections
            Set<Query> queries = group.getDataSourceQueries();
            List<DataSourceComponent> components = group.getDataSourceComponents();
            if (components != null) {
                group.setDataSourceComponents(group.getDataSourceComponents().stream().distinct().collect(Collectors.toList()));
            }
        }
        return group;
        //return super.get(id);
    }

    @Override
    public DataSourceComponentGroup save(DataSourceComponentGroup entity) throws PersistenceException {
        List<SourceDescriptor> sources = entity.getSources();
        if (sources != null) {
            for (SourceDescriptor sourceDescriptor : sources) {
                sourceDescriptor = sourceDescriptorRepository.save(sourceDescriptor);
            }
        }
        List<TargetDescriptor> targets = entity.getTargets();
        if (targets != null) {
            for (TargetDescriptor targetDescriptor : targets) {
                targetDescriptor = targetDescriptorRepository.save(targetDescriptor);
            }
        }
        return super.save(entity);
    }

    @Override
    public DataSourceComponentGroup update(DataSourceComponentGroup entity) throws PersistenceException {
        List<SourceDescriptor> sources = entity.getSources();
        if (sources != null) {
            for (SourceDescriptor sourceDescriptor : sources) {
                sourceDescriptor = sourceDescriptorRepository.save(sourceDescriptor);
            }
        }
        List<TargetDescriptor> targets = entity.getTargets();
        if (targets != null) {
            for (TargetDescriptor targetDescriptor : targets) {
                targetDescriptor = targetDescriptorRepository.save(targetDescriptor);
            }
        }
        return super.update(entity);
    }

    @Override
    public DataSourceComponentGroup delete(String id) throws PersistenceException {
        return super.delete(id);
    }

    @Override
    protected boolean checkId(String entityId, boolean existingEntity) {
        return entityId != null && !entityId.isEmpty();
    }

    @Override
    protected boolean checkEntity(DataSourceComponentGroup entity) {
        return entity != null && entity.getLabel() != null;
    }
}
