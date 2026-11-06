package ro.cs.tao.user;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;

/**
 * Represents an event logged within an application, consisting of a timestamp, user information, session details,
 * and a description of the event.
 *
 * This class extends the {@code LongIdentifiable} abstract class, which provides a unique long ID identifier.
 */
public class LogEvent extends LongIdentifiable {
    private LocalDateTime timestamp;
    private String userId;
    private String sessionId;
    private String event;

    public LogEvent() {
        super();
    }

    public LogEvent(LocalDateTime timestamp, String userId, String sessionId, String event) {
        super();
        this.timestamp = timestamp;
        this.userId = userId;
        this.sessionId = sessionId;
        this.event = event;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
