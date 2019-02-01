package ro.cs.tao.persistence.managers;

import org.springframework.stereotype.Component;
import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.persistence.repository.DataSourceGroupRepository;

import java.util.List;

@Component("dataSourceGroupManager")
public class DataSourceGroupManager extends TaoComponentManager<DataSourceComponentGroup, DataSourceGroupRepository> {

    public List<DataSourceComponentGroup> getUserDataSourceComponents(String userName) {
        return userName != null ? this.repository.getUserDataSourceComponentGroups(userName) :
                this.repository.getDataSourceComponentGroups();
    }

    public DataSourceComponentGroup getDataSourceComponentByLabel(String label) {
        List<DataSourceComponentGroup> components = this.repository.getDataSourceComponentGroupByLabel(label);
        return (components == null || components.size() == 0) ? null : components.get(0);
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
