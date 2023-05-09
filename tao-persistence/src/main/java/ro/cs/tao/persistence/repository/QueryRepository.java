package ro.cs.tao.persistence.repository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ro.cs.tao.datasource.beans.Query;

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
public interface QueryRepository extends PagingAndSortingRepository<Query, Long> {

    Query findByUserIdAndSensorAndDataSourceAndWorkflowNodeId(String userId, String sensor, String dataSource, long nodeId);

    /*@org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id, dscg.label, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId AND q.sensor_name = :sensor AND q.data_source = :dataSource AND q.graph_node_id = :nodeId",
            nativeQuery = true
    )*/
    @org.springframework.data.jpa.repository.Query(
            value = "SELECT null as groupId, null as groupLabel,q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId AND q.sensor_name = :sensor AND q.data_source = :dataSource AND q.graph_node_id = :nodeId",
            nativeQuery = true
    )
    Query getQuery(@Param("userId") String userId, @Param("sensor") String sensor,
                   @Param("dataSource") String dataSource, @Param("nodeId") long nodeId);

    Query findByUserIdAndLabel(String userId, String label);

    List<Query> findByUserIdAndWorkflowNodeId(String userId, long nodeId);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id as groupId, dscg.label as groupLabel, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId AND q.graph_node_id = :nodeId", nativeQuery = true
    )
    List<Query> getUserQueries(@Param("userId") String userId, @Param("nodeId") long nodeId);

    List<Query> findByUserIdAndSensorAndDataSource(String userId, String sensor, String dataSource);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id as groupId, dscg.label as groupLabel, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId AND q.sensor_name = :sensor AND q.data_source = :dataSource", nativeQuery = true
    )
    List<Query> getUserQueries(@Param("userId") String userId, @Param("sensor") String sensor, @Param("dataSource") String dataSource);

    List<Query> findByUserIdAndSensor(String userId, String sensor);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id as groupId, dscg.label as groupLabel, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId AND q.sensor_name = :sensor", nativeQuery = true
    )
    List<Query> getQueriesByUserAndSensor(@Param("userId") String userId, @Param("sensor") String sensor);

    List<Query> findByUserIdAndDataSource(String userId, String dataSource);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id as groupId, dscg.label as groupLabel, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId AND q.data_source = :dataSource", nativeQuery = true
    )
    List<Query> getQueriesByUserAndDataSource(@Param("userId") String userId, @Param("dataSource") String dataSource);

    List<Query> findByUserId(String userId);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id as groupId, dscg.label as groupLabel, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.user_id = :userId", nativeQuery = true
    )
    List<Query> getUserQueries(@Param("userId") String userId);

    @org.springframework.data.jpa.repository.Query(
            value = "SELECT dscg.id as groupId, dscg.label as groupLabel, q.* FROM workflow.query q " +
                    "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                    "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id " +
                    "WHERE q.id = :queryId", nativeQuery = true
    )
    Query getQuery(@Param("queryId") long queryId);
}
