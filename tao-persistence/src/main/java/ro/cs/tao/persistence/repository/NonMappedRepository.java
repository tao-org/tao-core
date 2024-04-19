package ro.cs.tao.persistence.repository;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import ro.cs.tao.Sort;
import ro.cs.tao.SortDirection;
import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.persistence.PersistenceManager;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public abstract class NonMappedRepository<T, K> {
    protected final PersistenceManager persistenceManager;
    protected final JdbcTemplate jdbcTemplate;
    protected final Logger logger = Logger.getLogger(getClass().getName());

    NonMappedRepository(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
        this.jdbcTemplate = new JdbcTemplate(persistenceManager.getDataSource());
    }

    protected abstract RowMapper<T> rowMapper();
    protected abstract String selectSQL();
    protected abstract String insertSQL();
    protected abstract String updateSQL();
    protected abstract String deleteSQL();

    public T single(K key, Supplier<String> whereStatement) throws PersistenceException {
        if (whereStatement == null || key == null) {
            throw new PersistenceException("Argument cannot be null");
        }
        final String sql = selectSQL() + " WHERE " + whereStatement.get();
        try {
            return jdbcTemplate.queryForObject(sql, rowMapper(), key);
        } catch (DataAccessException dax) {
            throw new PersistenceException(dax);
        }
    }

    public T single(Supplier<String> whereStatement, Function<PreparedStatement, PreparedStatement> parameterMapper) throws PersistenceException {
        if (whereStatement == null || parameterMapper == null) {
            throw new PersistenceException("Argument cannot be null");
        }
        List<T> results = list(whereStatement, parameterMapper, null);
        if (results != null && results.size() != 1) {
            throw new PersistenceException("Query returned more than one result");
        }
        if (results == null) {
            return null;
        } else {
            return results.get(0);
        }
    }

    public boolean exists(Supplier<String> whereStatement, Function<PreparedStatement, PreparedStatement> parameterMapper) {
        if (whereStatement == null) {
            return false;
        }
        try {
            List<T> results = list(whereStatement, parameterMapper, null);
            return results != null && !results.isEmpty();
        } catch (PersistenceException e) {
            logger.warning(e.getMessage());
            return false;
        }
    }

    public List<T> list(Supplier<String> whereStatement,
                        Function<PreparedStatement, PreparedStatement> parameterMapper,
                        Sort sort) throws PersistenceException {
        try {
            return jdbcTemplate.query(
                    connection -> {
                        String sql = selectSQL() + (whereStatement != null ? " WHERE " + whereStatement.get() : "");
                        if (sort != null) {
                            Map<String, SortDirection> fieldsForSort = sort.getFieldsForSort();
                            String[] sorts = new String[fieldsForSort.size()];
                            int idx = 0;
                            for (Map.Entry<String, SortDirection> entry : fieldsForSort.entrySet()) {
                                sorts[idx++] = entry.getKey() + " " + entry.getValue().name();
                            }
                            sql += " " + String.join(",", sorts);
                        }
                        final PreparedStatement statement = connection.prepareStatement(sql);
                        return parameterMapper != null ? parameterMapper.apply(statement) : statement;
                    }, rowMapper());
        } catch (DataAccessException dax) {
            throw new PersistenceException(dax);
        }
    }

    public T insert(String keyName, Class<K> keyClass,
                    Function<PreparedStatement, PreparedStatement> parameterMapper) throws PersistenceException {
        if (parameterMapper == null) {
            throw new PersistenceException("Argument cannot be null");
        }
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> parameterMapper.apply(connection.prepareStatement(insertSQL())), keyHolder);
            Map<String, Object> keys = keyHolder.getKeys();
            K id = null;
            id = keyClass.cast(keys.get(keyName));
            return id != null ? single(id, () -> keyName + " = ?") : null;
        } catch (Exception e) {
            throw new PersistenceException(e);
        }
    }

    public int update(K key, Supplier<String> whereStatement,
                      Function<PreparedStatement, PreparedStatement> parameterMapper) throws PersistenceException {
        if (whereStatement == null || key == null || parameterMapper == null) {
            throw new PersistenceException("Argument cannot be null");
        }
        try {
        return jdbcTemplate.update(
                connection -> {
                    final String sql = updateSQL() + " WHERE " + whereStatement.get();
                    return parameterMapper.apply(connection.prepareStatement(sql));
                });
        } catch (DataAccessException dax) {
            throw new PersistenceException(dax);
        }
    }

    public int delete(K key, Supplier<String> whereStatement,
                      Function<PreparedStatement, PreparedStatement> parameterMapper) throws PersistenceException {
        if (whereStatement == null || key == null || parameterMapper == null) {
            throw new PersistenceException("Argument cannot be null");
        }
        try {
            return jdbcTemplate.update(
                    connection -> {
                        final String sql = deleteSQL() + " WHERE " + whereStatement.get();
                        return parameterMapper.apply(connection.prepareStatement(sql));
            });
        } catch (DataAccessException dax) {
            throw new PersistenceException(dax);
        }
    }
}