package ro.cs.tao.services.interfaces;

import ro.cs.tao.services.model.monitoring.Snapshot;

import java.util.List;

/**
 * @author Cosmin Cara
 */
public interface MonitoringService<T> {

    Snapshot getMasterSnapshot();

    Snapshot getNodeSnapshot(String hostName);

    List<T> getLiveNotifications();

    List<T> getNotifications(String user, int page);

    List<T> acknowledgeNotification(List<T> notifications);

}
