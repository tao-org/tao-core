package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.DataSourceComponentGroup;

import java.util.List;

@Repository
@Qualifier(value = "dataSourceGroupRepository")
@Transactional
public interface DataSourceGroupRepository extends PagingAndSortingRepository<DataSourceComponentGroup, String> {

    @Query(value = "SELECT g FROM DataSourceComponentGroup g WHERE g.system = false AND g.userName = ?1 ORDER BY dsc.id")
    List<DataSourceComponentGroup> getUserDataSourceComponentGroups(String userName);

    @Query(value = "SELECT g FROM DataSourceComponentGroup g WHERE g.system = false ORDER BY dsc.id")
    List<DataSourceComponentGroup> getDataSourceComponentGroups();

    @Query(value = "SELECT g FROM DataSourceComponentGroup g WHERE g.label = ?1")
    List<DataSourceComponentGroup> getDataSourceComponentGroupByLabel(String label);
}
