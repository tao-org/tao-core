package ro.cs.tao.datasource.persistence;

import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.persistence.EntityProvider;

import java.util.List;

public interface DataSourceComponentGroupProvider extends EntityProvider<DataSourceComponentGroup, String> {

    List<DataSourceComponentGroup> listByUser(String userName);
    DataSourceComponentGroup getDataSourceComponentGroupByLabel(String label);

}
