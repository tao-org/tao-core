package ro.cs.tao.services.interfaces;

import ro.cs.tao.Tag;
import ro.cs.tao.datasource.DataSourceComponentGroup;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.eodata.EOProduct;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.utils.Tuple;

import java.security.Principal;
import java.util.List;

public interface DataSourceGroupService extends CRUDService<DataSourceComponentGroup, String> {

    DataSourceComponentGroup saveDataSourceGroup(String groupId, String groupLabel,
                                                 List<Tuple<Query, List<EOProduct>>> groupQueries,
                                                 Principal user) throws PersistenceException;

    List<DataSourceComponentGroup> getUserDataSourceComponentGroups(String userName);

    List<DataSourceComponentGroup> getDataSourceComponentGroups();

    List<Tag> getDatasourceGroupTags();
}
