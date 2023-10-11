package ro.cs.tao.user;

import java.time.LocalDateTime;

public class SessionDuration {
    private String userId;
    private LocalDateTime loggedOn;
    private double duration;

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

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }
}
