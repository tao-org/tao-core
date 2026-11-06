package ro.cs.tao.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.cs.tao.topology.AdminKey;
import ro.cs.tao.topology.AdminKeyId;

import java.util.List;

public interface AdminKeyRepository extends JpaRepository<AdminKey, AdminKeyId> {
    List<AdminKey> findByUserId(String userId);

    List<AdminKey> findByNodeId(String nodeId);
}
