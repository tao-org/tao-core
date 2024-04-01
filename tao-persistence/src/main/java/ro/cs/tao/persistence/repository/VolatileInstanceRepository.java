package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.VolatileInstance;

import java.util.List;

@Repository
@Qualifier(value = "volatileInstanceRepository")
@Transactional
public interface VolatileInstanceRepository extends PagingAndSortingRepository<VolatileInstance, Long> {

    List<VolatileInstance> findByUserId(String userId);

    List<VolatileInstance> findByFlavorId(String flavorId);

    @Query(value = "SELECT * FROM topology.volatile_instance WHERE node_id =:nodeId", nativeQuery = true)
    List<VolatileInstance> getByNodeId(@Param("nodeId") String nodeId);

}
