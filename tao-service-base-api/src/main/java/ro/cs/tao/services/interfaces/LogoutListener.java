package ro.cs.tao.services.interfaces;

import ro.cs.tao.user.SessionDuration;

public interface LogoutListener {

    void doAction(String userToken, SessionDuration sessionDuration, int processingTime);

}
