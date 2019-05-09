package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeType;

import java.util.List;

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

    List<NodeDescription> findByNodeType(NodeType type);

    List<NodeDescription> findByActive(boolean active);

}
