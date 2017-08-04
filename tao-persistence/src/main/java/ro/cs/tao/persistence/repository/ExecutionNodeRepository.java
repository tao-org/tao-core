package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.persistence.data.ExecutionNode;

/**
 * CRUD repository for ExecutionNode entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "executionNodeRepository")
@Transactional
public interface ExecutionNodeRepository extends PagingAndSortingRepository<ExecutionNode, Long> {

    /**
     * Find ExecutionNode entity by its identifier
     * @param id - the given execution node identifier
     * @return the corresponding ExecutionNode entity
     */
    ExecutionNode findById(Integer id);

    /**
     * Find ExecutionNode entity by its IP address
     * @param ipAddress - the given IP address
     * @return the corresponding ExecutionNode entity
     */
    ExecutionNode findByIpAddress(String ipAddress);
}
