package ro.cs.tao.persistence.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import ro.cs.tao.persistence.AuditProvider;
import ro.cs.tao.persistence.repository.AuditRepository;
import ro.cs.tao.user.LogEvent;
import ro.cs.tao.user.SessionDuration;
import ro.cs.tao.utils.StringUtilities;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = {"ro.cs.tao.persistence.repository" })
@Component("auditManager")
public class AuditManager extends EntityManager<LogEvent, Long, AuditRepository> implements AuditProvider {

    @Autowired
    private DataSource dataSource;

    @Override
    public List<LogEvent> getByUser(String user) {
        return repository.getByUser(user);
    }

    @Override
    public List<LogEvent> getByUserAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate) {
        return repository.getByUserAndInterval(user, startDate, endDate);
    }

    @Override
    public List<SessionDuration> getAllSessions() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> con.prepareStatement("with diffs as ( " +
                                             "with ranges as ( " +
                                             "select user_id, event, timestamp, tsrange(timestamp, lead(timestamp) over (partition by user_id order by timestamp)) as range " +
                                             "from common.audit " +
                                             "where event in ('Login', 'Logout')) " +
                                             "select user_id, timestamp as login_on, (extract(hours from (upper(range) - lower(range)))*3600 + extract(mins from (upper(range) - lower(range)))*60 + extract(seconds from (upper(range) - lower(range))))::integer as time_spent " +
                                             "from ranges) select user_id, login_on, time_spent from diffs where time_spent is not null"), (rs, rowNum) -> {
            SessionDuration result = new SessionDuration();
            result.setUserId(rs.getString(1));
            result.setLoggedOn(rs.getTimestamp(2).toLocalDateTime());
            result.setDuration(rs.getInt(3));
            return result;
        });
    }

    @Override
    public List<SessionDuration> getAllSessions(LocalDateTime from, LocalDateTime to) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
                final PreparedStatement statement = con.prepareStatement("with diffs as ( " +
                                                                       "with ranges as ( " +
                                                                       "select user_id, event, timestamp, tsrange(timestamp, lead(timestamp) over (partition by user_id order by timestamp)) as range " +
                                                                       "from common.audit " +
                                                                       "where event in ('Login', 'Logout')) " +
                                                                       "select user_id, timestamp as login_on, (extract(hours from (upper(range) - lower(range)))*3600 + extract(mins from (upper(range) - lower(range)))*60 + extract(seconds from (upper(range) - lower(range))))::integer as time_spent " +
                                                                       "from ranges) select user_id, login_on, time_spent from diffs where time_spent is not null " +
                                                                       "and login_on between ? and ?");
                statement.setTimestamp(1, Timestamp.valueOf(from));
                statement.setTimestamp(2, Timestamp.valueOf(to));
                return statement;
              },
            (rs, rowNum) -> {
                SessionDuration result = new SessionDuration();
                result.setUserId(rs.getString(1));
                result.setLoggedOn(rs.getTimestamp(2).toLocalDateTime());
                result.setDuration(rs.getInt(3));
                return result;
        });
    }

    @Override
    public SessionDuration getLastUserSession(String userId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
                                      final PreparedStatement statement = con.prepareStatement("with diffs as ( " +
                                                                                                       "with ranges as ( " +
                                                                                                       "select user_id, event, timestamp, tsrange(timestamp, lead(timestamp) over (partition by user_id order by timestamp)) as range " +
                                                                                                       "from common.audit " +
                                                                                                       "where event in ('Login', 'Logout')) " +
                                                                                                       "select user_id, timestamp as login_on, (extract(hours from (upper(range) - lower(range)))*3600 + extract(mins from (upper(range) - lower(range)))*60 + extract(seconds from (upper(range) - lower(range))))::integer as time_spent " +
                                                                                                       "from ranges) select user_id, login_on, time_spent from diffs where time_spent is not null " +
                                                                                                       "and user_id = ? order by login_on desc limit 1");
                                      statement.setString(1, userId);
                                      return statement;
                                  },
                                  (rs) -> {
                                      if (rs.next()) {
                                          SessionDuration result = new SessionDuration();
                                          result.setUserId(rs.getString(1));
                                          result.setLoggedOn(rs.getTimestamp(2).toLocalDateTime());
                                          result.setDuration(rs.getInt(3));
                                          return result;
                                      } else {
                                          return null;
                                      }
                                  });
    }

    @Override
    public List<SessionDuration> getUserSessions(String userId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
              final PreparedStatement statement = con.prepareStatement("with diffs as ( " +
                                                                               "with ranges as ( " +
                                                                               "select user_id, event, timestamp, tsrange(timestamp, lead(timestamp) over (partition by user_id order by timestamp)) as range " +
                                                                               "from common.audit " +
                                                                               "where event in ('Login', 'Logout')) " +
                                                                               "select user_id, timestamp as login_on, (extract(hours from (upper(range) - lower(range)))*3600 + extract(mins from (upper(range) - lower(range)))*60 + extract(seconds from (upper(range) - lower(range))))::integer as time_spent " +
                                                                               "from ranges) select user_id, login_on, time_spent from diffs where time_spent is not null " +
                                                                               "and user_id = ?");
              statement.setString(1, userId);
              return statement;
          },
          (rs, rowNum) -> {
              SessionDuration result = new SessionDuration();
              result.setUserId(rs.getString(1));
              result.setLoggedOn(rs.getTimestamp(2).toLocalDateTime());
              result.setDuration(rs.getInt(3));
              return result;
          });
    }

    @Override
    public List<SessionDuration> getUserSessions(String userId, LocalDateTime from, LocalDateTime to) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
              final PreparedStatement statement = con.prepareStatement("with diffs as ( " +
                                                                               "with ranges as ( " +
                                                                               "select user_id, event, timestamp, tsrange(timestamp, lead(timestamp) over (partition by user_id order by timestamp)) as range " +
                                                                               "from common.audit " +
                                                                               "where event in ('Login', 'Logout')) " +
                                                                               "select user_id, timestamp as login_on, (extract(hours from (upper(range) - lower(range)))*3600 + extract(mins from (upper(range) - lower(range)))*60 + extract(seconds from (upper(range) - lower(range))))::integer as time_spent " +
                                                                               "and user_id = ? and login_on between ? and ?");
              statement.setString(1, userId);
              statement.setTimestamp(2, Timestamp.valueOf(from));
              statement.setTimestamp(3, Timestamp.valueOf(to));
              return statement;
          },
          (rs, rowNum) -> {
              SessionDuration result = new SessionDuration();
              result.setUserId(rs.getString(1));
              result.setLoggedOn(rs.getTimestamp(2).toLocalDateTime());
              result.setDuration(rs.getInt(3));
              return result;
          });
    }

    @Override
    public Map<String, Integer> getAggregatedUsersProcessingTime() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final Map<String, Integer> results = new HashMap<>();
        jdbcTemplate.query(con -> {
              return con.prepareStatement("with task_duration as ( select id, end_time, extract(epoch from coalesce(end_time, now()) - start_time) as minutes " +
                                               "from execution.task) " +
                                               "select j.user_id, sum(d.minutes) as total from execution.task t " +
                                               "join execution.job j on j.id = t.job_id join task_duration d on d.id = t.id " +
                                               "where ((d.end_time is NULL and t.execution_status_id = 2) or (d.end_time is not null)) " +
                                               "group by j.user_id order by j.user_id, total desc;");
          },
          (rs, rowNum) -> {
              final AbstractMap.SimpleEntry<String, Integer> entry = new AbstractMap.SimpleEntry<>(rs.getString(1), rs.getInt(2));
              results.put(entry.getKey(), entry.getValue());
              return entry;
          });
        return results;
    }

    @Override
    public int getAggregatedUserProcessingTime(String userId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
               final PreparedStatement statement = con.prepareStatement("with task_duration as ( select id, end_time, extract(epoch from (coalesce(end_time, now()) - start_time)) as minutes " +
                                                                                "from execution.task) " +
                                                                                "select j.user_id, sum(d.minutes) as total from execution.task t " +
                                                                                "join execution.job j on j.id = t.job_id join task_duration d on d.id = t.id " +
                                                                                "where j.user_id = ? and ((d.end_time is NULL and t.execution_status_id = 2) or (d.end_time is not null)) " +
                                                                                "group by j.user_id order by j.user_id, total desc;");
               statement.setString(1, userId);
               return statement;
           },
           (rs) -> rs.next() ? rs.getInt(2) : 0);
    }

    @Override
    public int getLastSessionUserProcessingTime(String userId) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        return jdbcTemplate.query(con -> {
                                      final PreparedStatement statement = con.prepareStatement("with last_session as (select user_id, timestamp from common.audit " +
                                                                                               "where user_id = ? and event = 'Logout' order by timestamp desc limit 1), " +
                                                                                               "task_duration as ( select t.id, end_time, extract(epoch from (coalesce(t.end_time, now()) - t.start_time)) as minutes from execution.task t " +
                                                                                               "join execution.job j on j.id = t.job_id join last_session s on s.user_id = j.user_id where t.start_time >= s.timestamp) " +
                                                                                               "select j.user_id, sum(d.minutes) as total from execution.task t " +
                                                                                               "join execution.job j on j.id = t.job_id join task_duration d on d.id = t.id " +
                                                                                               "where j.user_id = ? and ((d.end_time is NULL and t.execution_status_id = 2) or (d.end_time is not null)) " +
                                                                                               "group by j.user_id order by j.user_id, total desc;");
                                      statement.setString(1, userId);
                                      statement.setString(2, userId);
                                      return statement;
                                  },
                                  (rs) -> rs.next() ? rs.getInt(2) : 0);
    }

    @Override
    protected String identifier() { return "id"; }

    @Override
    protected boolean checkId(Long entityId, boolean existingEntity) {
        return true;
    }

    @Override
    protected boolean checkEntity(LogEvent data) {
        return data != null && !StringUtilities.isNullOrEmpty(data.getUserId())
                && !StringUtilities.isNullOrEmpty(data.getEvent());
    }
}
