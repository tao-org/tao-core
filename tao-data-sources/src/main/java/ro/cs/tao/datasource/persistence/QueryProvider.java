package ro.cs.tao.datasource.persistence;

import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.EntityProvider;

import java.util.List;

public interface QueryProvider extends EntityProvider<Query, Long> {

    Query get(String userId, String label);
    Query get(String userId, String sensor, String dataSource, long workflowNodeId);
    List<Query> listBySensor(String userId, String sensor);
    List<Query> listByDataSource(String userId, String dataSource);
    List<Query> list(String userId);
    List<Query> list(String userId, long nodeId);
    List<Query> list(String userId, String sensor, String dataSource);
}
