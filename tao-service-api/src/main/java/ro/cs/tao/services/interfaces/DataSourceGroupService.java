package ro.cs.tao.services.interfaces;

import ro.cs.tao.Tag;
import ro.cs.tao.datasource.DataSourceComponentGroup;

import java.util.List;

public interface DataSourceGroupService extends CRUDService<DataSourceComponentGroup, String> {

    List<DataSourceComponentGroup> getUserDataSourceComponentGroups(String userName);

    List<DataSourceComponentGroup> getDataSourceComponentGroups();

    List<Tag> getDatasourceGroupTags();
}
