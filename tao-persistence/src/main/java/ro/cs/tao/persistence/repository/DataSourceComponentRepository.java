package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.DataSourceComponent;

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
     * Find DataSourceComponent entity by its identifier
     * @param id - the given data source component identifier
     * @return the corresponding DataSourceComponent entity
     */
    //DataSourceComponent findById(String id);

    /**
     * Find DataSourceComponent entity by its label
     * @param label - the given label
     * @return the corresponding DataSourceComponent entity
     */
    DataSourceComponent findByLabel(String label);
}
