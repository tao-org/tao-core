package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.NodeDescription;
import ro.cs.tao.topology.NodeFlavor;

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

    List<NodeDescription> findByFlavor(NodeFlavor flavor);

    List<NodeDescription> findByActive(boolean active);

    @Query(value = "SELECT COUNT(n.id) FROM topology.node n " +
            "WHERE NOT EXISTS (SELECT t.id FROM execution.task t JOIN execution.job j ON j.id = t.job_id " +
            "WHERE t.execution_status_id NOT IN (1, 2) AND t.execution_node_host_name = n.id AND " +
            "CASE WHEN (SELECT COALESCE(value, 'false') FROM config.config WHERE id = 'topology.dedicated.user.nodes') = 'true' " +
            "THEN j.user_id = :userId ELSE (n.owner_id IS NULL AND n.role != 'master') END)", nativeQuery = true)
    int countUsableNodes(@Param("userId") String userId);

}
