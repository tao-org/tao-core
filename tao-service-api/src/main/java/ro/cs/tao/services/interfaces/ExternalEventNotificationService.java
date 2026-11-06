package ro.cs.tao.services.interfaces;

/**
 * Service for managing Server Sent Events notifications.
 *
 * @author Oana H.
 */
public interface ExternalEventNotificationService extends TAOService {

    void registerHandler(ExternalEventNotificationHandler handler);

    void start();

    void stop();
}
