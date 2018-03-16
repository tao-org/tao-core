package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.Query;

/**
 * CRUD repository for Query entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "queryRepository")
@Transactional
public interface QueryRepository extends PagingAndSortingRepository<Query, String> {

    /**
     * Find Query entity by its identifier
     * @param id - the given query identifier
     * @return the corresponding Query entity
     */
    Query findById(String id);

    /**
     * Find Query entity by its label
     * @param sensor - the given sensor name
     * @param dataSource - the given data source name
     * @return the corresponding Query entity
     */
    Query findBySensorAndDataSource(String sensor, String dataSource);
}
