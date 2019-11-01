package ro.cs.tao.persistence.repository;

import org.springframework.jdbc.core.RowMapper;
import ro.cs.tao.datasource.beans.Query;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.data.jsonutil.JacksonUtil;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.utils.Crypto;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryViewRepository extends NonMappedRepository<Query, Long> {

    public QueryViewRepository(PersistenceManager persistenceManager) {
        super(persistenceManager);
    }

    public Query getQuery(long id) throws PersistenceException {
        return single(id, () -> "q.id = ?");
    }

    public List<Query> getUserQueries(String userId) throws PersistenceException {
        return list(() -> "q.user_id = ?", preparedStatement -> {
            try {
                preparedStatement.setString(1, userId);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return preparedStatement;
        }, null);
    }

    @Override
    protected RowMapper<Query> rowMapper() {
        return ((resultSet, i) -> {
            Query query = new Query();
            query.setGroupId(resultSet.getString(1));
            query.setGroupLabel(resultSet.getString(2));
            query.setId(resultSet.getLong(3));
            query.setLabel(resultSet.getString(4));
            query.setUserId(resultSet.getString(5));
            query.setWorkflowNodeId(resultSet.getLong(6));
            query.setComponentId(resultSet.getString(7));
            query.setSensor(resultSet.getString(8));
            query.setDataSource(resultSet.getString(9));
            final String user = resultSet.getString(10);
            if (user != null && !user.isEmpty()) {
                query.setUser(user);
                query.setPassword(Crypto.decrypt(resultSet.getString(11), user));
            }
            query.setPageSize(resultSet.getInt(12));
            query.setPageNumber(resultSet.getInt(13));
            query.setLimit(resultSet.getInt(14));
            String values = resultSet.getString(15);
            if (values != null) {
                Map<String, String> map = JacksonUtil.fromString(values, HashMap.class);
                query.setValues(map);
            }
            return query;
        });
    }

    @Override
    protected String selectSQL() {
        return "SELECT dscg.id, dscg.label, q.* FROM workflow.query q " +
                "LEFT JOIN component.data_source_component_group_components dscgc ON dscgc.data_source_component_id = q.component_id " +
                "LEFT JOIN component.data_source_component_group dscg ON dscg.id = dscgc.data_source_component_group_id ";
    }

    @Override
    protected String insertSQL() {
        return null;
    }

    @Override
    protected String updateSQL() {
        return null;
    }

    @Override
    protected String deleteSQL() {
        return null;
    }
}
