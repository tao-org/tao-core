package ro.cs.tao.services.interfaces;

import ro.cs.tao.persistence.config.DatabaseConfiguration;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface LocalDBService extends TAOService {

    List<Map<String, Object>> executeQuery(String sql) throws SQLException;

    List<Map<String, Object>> executeQuery(String sql, Map<String, Object> params) throws SQLException;

}
