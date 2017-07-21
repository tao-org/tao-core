package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.data.DataSource;

/**
 * CRUD repository for DataSource entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "dataSourceRepository")
@Transactional
public interface DataSourceRepository extends PagingAndSortingRepository<DataSource, Integer> {

    /**
     * Find DataSource entity by its identifier
     * @param id - the given data source identifier
     * @return the corresponding DataSource entity
     */
    DataSource findById(Integer id);

    /**
     * Find DataSource entity by its name
     * @param name - the given data source name
     * @return the corresponding DataSource entity
     */
    DataSource findByName(String name);
}
