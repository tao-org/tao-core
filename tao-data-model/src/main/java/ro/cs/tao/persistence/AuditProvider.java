package ro.cs.tao.persistence;

import ro.cs.tao.user.LogEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditProvider extends EntityProvider<LogEvent, Long> {

    List<LogEvent> getByUser(String user);
    List<LogEvent> getByUserAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate);

}
