package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.topology.NodeFlavor;

@Repository
@Qualifier(value = "nodeFlavorRepository")
@Transactional
public interface NodeFlavorRepository extends PagingAndSortingRepository<NodeFlavor, String> {

    @Query(value = "SELECT * FROM topology.node_flavor WHERE cpu >= :cpu AND memory >= :memory AND disk >= :disk ORDER BY cpu, memory, disk LIMIT 1",
            nativeQuery = true)
    NodeFlavor getMatchingFlavor(@Param("cpu")int cpu, @Param("memory")int memory, @Param("disk")int disk);

    @Query(value = "SELECT * FROM topology.node_flavor WHERE (cpu >= :cpu) AND (ABS(memory - :memory) / CAST(:memory AS float) <= 0.1 OR memory > :memory) " +
            "AND id NOT IN ('master', 'k8s') " +
            "ORDER BY cpu, memory LIMIT 1",
            nativeQuery = true)
    NodeFlavor getMatchingFlavor(@Param("cpu")int cpu, @Param("memory")int memory);
}
