package ro.cs.tao.user;

import java.time.LocalDateTime;

public class SessionDuration {
    private String userId;
    private LocalDateTime loggedOn;
    private int duration;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getLoggedOn() {
        return loggedOn;
    }

    public void setLoggedOn(LocalDateTime loggedOn) {
        this.loggedOn = loggedOn;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
