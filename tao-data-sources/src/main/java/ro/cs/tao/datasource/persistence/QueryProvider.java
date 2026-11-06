package ro.cs.tao.datasource.persistence;

import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.EntityProvider;

import java.util.List;

/**
 * The QueryProvider interface defines methods for managing Query entities.
 * It extends the EntityProvider interface, providing additional capabilities
 * for fetching, listing, and managing Query objects based on various filtering criteria.
 */
public interface QueryProvider extends EntityProvider<Query, Long> {

    Query get(String userId, String label);
    Query get(String userId, String sensor, String dataSource, long workflowNodeId);
    List<Query> listBySensor(String userId, String sensor);
    List<Query> listByDataSource(String userId, String dataSource);
    List<Query> list(String userId);
    List<Query> list(String userId, long nodeId);
    List<Query> list(String userId, String sensor, String dataSource);
}
