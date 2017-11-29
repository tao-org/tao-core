package ro.cs.tao.services.interfaces;

import ro.cs.tao.messaging.Message;
import ro.cs.tao.services.model.monitoring.Snapshot;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface MonitoringService {

    Snapshot getMasterSnapshot();

    Snapshot getNodeSnapshot(String hostName);

    List<Message> getLiveNotifications();

    List<Message> getNotifications(int userId, int page);

    List<Message> acknowledgeNotification(List<Message> notifications);

}
