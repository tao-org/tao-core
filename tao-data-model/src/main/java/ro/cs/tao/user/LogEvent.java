package ro.cs.tao.user;

import ro.cs.tao.component.LongIdentifiable;

import java.time.LocalDateTime;

public class LogEvent extends LongIdentifiable {
    private LocalDateTime timestamp;
    private String userName;
    private String event;

    public LogEvent() {
        super();
    }

    public LogEvent(LocalDateTime timestamp, String userName, String event) {
        super();
        this.timestamp = timestamp;
        this.userName = userName;
        this.event = event;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }
}
