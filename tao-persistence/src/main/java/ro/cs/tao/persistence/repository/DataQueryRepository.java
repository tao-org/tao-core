package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.data.DataQuery;

/**
 * CRUD repository for DataQuery entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "dataQueryRepository")
@Transactional
public interface DataQueryRepository extends PagingAndSortingRepository<DataQuery, Integer> {

    /**
     * Find DataQuery entity by its numeric auto-generated identifier
     * @param id - the DB related data query identifier
     * @return the corresponding DataQuery entity
     */
    DataQuery findById(Integer id);

    /**
     * Find DataQuery entity by its name
     * @param name - the given data query name
     * @return the corresponding DataQuery entity
     */
    DataQuery findByName(String name);
}
