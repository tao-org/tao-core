package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.DataSourceComponent;

import java.util.List;

/**
 * CRUD repository for DataSourceComponent entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "dataSourceComponentRepository")
@Transactional
public interface DataSourceComponentRepository extends PagingAndSortingRepository<DataSourceComponent, String> {

    /**
     * Find DataSourceComponent entity by its label
     * @param label - the given label
     * @return the corresponding DataSourceComponent entity
     */
    DataSourceComponent findByLabel(String label);

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.system = false AND dsc.id like %?1% ORDER BY dsc.id")
    List<DataSourceComponent> getUserDataSourceComponents(String userName);

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.system = false ORDER BY dsc.id")
    List<DataSourceComponent> getUserDataSourceComponents();

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.system = true ORDER BY dsc.id")
    List<DataSourceComponent> getSystemDataSourceComponents();

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.label = ?1")
    List<DataSourceComponent> getDataSourceComponentByLabel(String label);

    @Query(value = "SELECT dsc.* FROM component.data_source_component dsc " +
                   "JOIN workflow.query q ON q.component_id = dsc.id WHERE q.id = ?1",
           nativeQuery = true)
    DataSourceComponent getQueryDataSourceComponent(long queryId);
}
