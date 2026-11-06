package ro.cs.tao.user;

import java.time.LocalDateTime;

/**
 * Represents the duration of a user's session, including user identifier, login time, and session duration.
 *
 * This class provides methods to store and retrieve information related to a user's session,
 * such as the user's unique ID, the time at which the user logged on, and the length of the session in minutes.
 */
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
