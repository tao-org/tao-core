package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.NodeDescription;

/**
 * CRUD repository for NodeDescription entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "nodeRepository")
@Transactional
public interface NodeRepository extends PagingAndSortingRepository<NodeDescription, String> {

}
