package ro.cs.tao.services.interfaces;
import ro.cs.tao.services.model.event.ExternalEventNotification;

@FunctionalInterface
public interface ExternalEventNotificationHandler {

    void onNotification(ExternalEventNotification notification);
}
