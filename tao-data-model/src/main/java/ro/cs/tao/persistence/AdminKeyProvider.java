package ro.cs.tao.persistence;

import ro.cs.tao.topology.AdminKey;
import ro.cs.tao.topology.AdminKeyId;

import java.util.List;

public interface AdminKeyProvider extends EntityProvider<AdminKey, AdminKeyId> {
    List<AdminKey> getByUserId(String userId);

    List<AdminKey> getByNodeId(String nodeId);
}
