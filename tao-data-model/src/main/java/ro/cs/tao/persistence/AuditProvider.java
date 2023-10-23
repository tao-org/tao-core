package ro.cs.tao.persistence;

import ro.cs.tao.user.LogEvent;
import ro.cs.tao.user.SessionDuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AuditProvider extends EntityProvider<LogEvent, Long> {

    List<LogEvent> getByUser(String user);
    List<LogEvent> getByUserAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate);
    List<SessionDuration> getAllSessions();
    List<SessionDuration> getAllSessions(LocalDateTime from, LocalDateTime to);
    List<SessionDuration> getUserSessions(String userId);
    List<SessionDuration> getUserSessions(String userId, LocalDateTime from, LocalDateTime to);
    SessionDuration getLastUserSession(String userId);
    Map<String, Integer> getAggregatedUsersProcessingTime();
    int getAggregatedUserProcessingTime(String userId);
    int getLastSessionUserProcessingTime(String userId);
}
