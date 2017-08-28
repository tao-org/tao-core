package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.data.DataSourceType;

/**
 * CRUD repository for DataSourceType entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "dataSourceTypeRepository")
@Transactional
public interface DataSourceTypeRepository extends PagingAndSortingRepository<DataSourceType, Integer> {

    /**
     * Find DataSourceType entity by its identifier
     * @param id - the given data source type identifier
     * @return the corresponding DataSourceType entity
     */
    DataSourceType findById(Integer id);

    /**
     * Find DataSourceType entity by its type
     * @param type - the given data source type
     * @return the corresponding DataSourceType entity
     */
    DataSourceType findByType(String type);
}
