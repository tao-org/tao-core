package ro.cs.tao.persistence;

import ro.cs.tao.user.LogEvent;
import ro.cs.tao.user.SessionDuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * The AuditProvider interface provides methods for managing and retrieving audit log data
 * and session duration information. It extends the generic {@code EntityProvider} interface
 * for audit log events, identified by a unique Long identifier.
 * This interface includes specific methods to filter and query audit log data based on users,
 * time intervals, and sessions.
 */
public interface AuditProvider extends EntityProvider<LogEvent, Long> {

    /**
     * Retrieves a list of log events associated with the specified user.
     *
     * @param user the identifier of the user whose log events are to be retrieved
     * @return a list of log events associated with the specified user
     */
    List<LogEvent> getByUser(String user);

    /**
     * Retrieves a list of log events associated with the specified user within a given time interval.
     *
     * @param user the identifier of the user whose log events are to be retrieved
     * @param startDate the start timestamp of the interval
     * @param endDate the end timestamp of the interval
     * @return a list of log events associated with the specified user within the provided time interval
     */
    List<LogEvent> getByUserAndInterval(String user, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Retrieves all session duration information for all users.
     *
     * @return a list of SessionDuration objects, each representing the session details
     *         including user ID, login time, and session duration
     */
    List<SessionDuration> getAllSessions();

    /**
     * Retrieves all session duration information for all users within the specified time interval.
     *
     * @param from the start timestamp of the interval
     * @param to the end timestamp of the interval
     * @return a list of SessionDuration objects, each representing session details such as
     *         user ID, login time, and session duration within the specified time interval
     */
    List<SessionDuration> getAllSessions(LocalDateTime from, LocalDateTime to);

    /**
     * Retrieves a list of session duration information associated with the specified user.
     *
     * @param userId the identifier of the user whose session duration information is to be retrieved
     * @return a list of SessionDuration objects, each representing session details such as
     *         login time and session duration for the specified user
     */
    List<SessionDuration> getUserSessions(String userId);

    /**
     * Retrieves a list of session duration information associated with the specified user within the provided time interval.
     *
     * @param userId the identifier of the user whose session duration information is to be retrieved
     * @param from the start timestamp of the interval
     * @param to the end timestamp of the interval
     * @return a list of SessionDuration objects, each representing session details such as
     *         login time and session duration for the specified user within the given time interval
     */
    List<SessionDuration> getUserSessions(String userId, LocalDateTime from, LocalDateTime to);

    /**
     * Retrieves the most recent session duration information for the specified user.
     *
     * @param userId the identifier of the user whose latest session information is to be retrieved
     * @return a SessionDuration object representing the details of the user's latest session,
     *         including user ID, login time, and session duration
     */
    SessionDuration getLastUserSession(String userId);

    /**
     * Aggregates and retrieves the total processing time for each user,
     * represented as a mapping between user identifiers and their respective processing times.
     *
     * @return a map where the keys represent user IDs and the values represent
     *         the total processing time, in seconds, for each user.
     */
    Map<String, Integer> getAggregatedUsersProcessingTime();

    /**
     * Retrieves the aggregated total processing time for a specified user.
     *
     * @param userId the identifier of the user whose total processing time is to be retrieved
     * @return the total processing time, in seconds, for the specified user
     */
    int getAggregatedUserProcessingTime(String userId);

    /**
     * Retrieves the processing time for the user's most recent session.
     *
     * @param userId the identifier of the user whose last session processing time is to be retrieved
     * @return the processing time, in seconds, of the user's most recent session
     */
    int getLastSessionUserProcessingTime(String userId);
}
