package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.docker.ContainerInstance;

import java.util.List;

/**
 * CRUD repository for Container instance entities
 *
 * @author Cosmin Cara
 *
 */
@Repository
@Qualifier(value = "containerInstanceRepository")
@Transactional
public interface ContainerInstanceRepository extends PagingAndSortingRepository<ContainerInstance, String> {

    List<ContainerInstance> findByContainerId(String containerId);

    List<ContainerInstance> findByUserId(String userId);

    ContainerInstance findByContainerIdAndUserId(String containerId, String userId);

    @Query(value = "SELECT host_port FROM component.container_instance", nativeQuery = true)
    List<Integer> getAllocatedPorts();
}
