package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.DataSourceComponentGroup;

import java.util.List;

@Repository
@Qualifier(value = "dataSourceGroupRepository")
@Transactional
public interface DataSourceGroupRepository extends PagingAndSortingRepository<DataSourceComponentGroup, String> {

    @Query(value = "SELECT d FROM DataSourceComponentGroup d LEFT JOIN FETCH d.dataSourceComponents " +
            "LEFT JOIN FETCH d.dataSourceQueries " +
            "WHERE d.id = :id")
    DataSourceComponentGroup getById(@Param("id") String id);

    @Query(value = "SELECT g FROM DataSourceComponentGroup g WHERE g.userId = ?1 ORDER BY g.label")
    List<DataSourceComponentGroup> getUserDataSourceComponentGroups(String userId);

    @Query(value = "SELECT g FROM DataSourceComponentGroup g ORDER BY g.label")
    List<DataSourceComponentGroup> getDataSourceComponentGroups();

    @Query(value = "SELECT g FROM DataSourceComponentGroup g WHERE g.label = ?1 ORDER BY g.label")
    List<DataSourceComponentGroup> getDataSourceComponentGroupByLabel(String label);
}
