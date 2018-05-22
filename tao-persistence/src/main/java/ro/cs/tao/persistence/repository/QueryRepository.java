package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.execution.model.Query;

import java.util.List;

/**
 * CRUD repository for Query entities
 *
 * @author oana
 *
 */
@Repository
@Qualifier(value = "queryRepository")
@Transactional
public interface QueryRepository extends PagingAndSortingRepository<Query, String> {

    Query findById(long id);

    Query findByUserIdAndSensorAndDataSourceAndWorkflowNodeId(String userId, String sensor, String dataSource, long nodeId);

    List<Query> findByUserIdAndWorkflowNodeId(String userId, long nodeId);

    List<Query> findByUserIdAndSensorAndDataSource(String userId, String sensor, String dataSource);

    List<Query> findByUserId(String userId);

    List<Query> findByUserIdAndSensor(String userId, String sensor);

    List<Query> findByUserIdAndDataSource(String userId, String dataSource);
}
