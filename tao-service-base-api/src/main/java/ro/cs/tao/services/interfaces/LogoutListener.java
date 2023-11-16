package ro.cs.tao.services.interfaces;

import ro.cs.tao.user.SessionDuration;

public interface LogoutListener {

    void doAction(String userId, String token, SessionDuration sessionDuration, int processingTime);

}
