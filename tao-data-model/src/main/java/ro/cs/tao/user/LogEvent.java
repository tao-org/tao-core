package ro.cs.tao.user;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;

public class LogEvent extends LongIdentifiable {
    private LocalDateTime timestamp;
    private String userId;
    private String event;

    public LogEvent() {
        super();
    }

    public LogEvent(LocalDateTime timestamp, String userId, String event) {
        super();
        this.timestamp = timestamp;
        this.userId = userId;
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
}
