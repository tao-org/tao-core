package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.DataSourceComponent;

import java.util.List;
import java.util.Set;

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

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.system = false AND COALESCE(dsc.volatile, false) = false AND dsc.id like %?1% ORDER BY dsc.id")
    List<DataSourceComponent> getUserDataSourceComponents(String userId);

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.system = false AND COALESCE(dsc.volatile, false) = false ORDER BY dsc.id")
    List<DataSourceComponent> getUserDataSourceComponents();

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.id NOT IN (?1) AND COALESCE(dsc.volatile, false) = false ORDER BY dsc.id")
    List<DataSourceComponent> getOtherDataSourceComponents(Set<String> ids);

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.system = true ORDER BY dsc.id")
    List<DataSourceComponent> getSystemDataSourceComponents();

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.label = ?1")
    List<DataSourceComponent> getDataSourceComponentByLabel(String label);

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.dataSourceName = ?1 ORDER BY dsc.id")
    List<DataSourceComponent> getBySource(String dataSourceName);

    @Query(value = "SELECT dsc FROM DataSourceComponent dsc WHERE dsc.dataSourceName = ?1 AND dsc.sensorName = ?2 AND COALESCE(dsc.volatile, false) = false ORDER BY dsc.id")
    List<DataSourceComponent> getBySourceAndSensor(String dataSourceName, String sensor);

    @Query(value = "SELECT dsc.* FROM component.source_descriptor sd JOIN component.data_source_component dsc ON dsc.id = sd.parent_id " +
            "WHERE dsc.id LIKE '%' || ?1 || '%' AND sd.name = 'query' AND sd.location IS NOT NULL AND COALESCE(dsc.volatile, false) = false ORDER BY dsc.created", nativeQuery = true)
    List<DataSourceComponent> getProductSets(String userName);

    @Query(value = "SELECT dsc.* FROM component.data_source_component dsc " +
                   "JOIN workflow.query q ON q.component_id = dsc.id WHERE q.id = ?1 AND COALESCE(dsc.volatile, false) = false",
           nativeQuery = true)
    DataSourceComponent getQueryDataSourceComponent(long queryId);
}
