/*
 * Copyright (C) 2018 CS ROMANIA
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package ro.cs.tao.services.interfaces;

import ro.cs.tao.execution.monitor.RuntimeInfo;

import java.util.List;
import java.util.Map;

/**
 * The MonitoringService interface defines the contract for a monitoring system
 * that provides runtime information, notification management, and monitoring
 * of node statuses. It extends the TAOService marker interface.
 *
 * @param <T> the type of the notification objects handled by the service.
 */
public interface MonitoringService<T> extends TAOService {

    /**
     * Retrieves the master runtime snapshot of the system, which includes detailed
     * information about system resources such as CPU usage, memory usage, disk usage,
     * and the timestamp of when the data was captured.
     *
     * @return the master runtime snapshot as a RuntimeInfo object
     */
    RuntimeInfo getMasterSnapshot();

    /**
     * Retrieves the runtime snapshot for all monitored nodes. The snapshot contains
     * runtime information such as CPU usage, memory usage, disk usage, and timestamps
     * for each node, identified by their respective keys.
     *
     * @return a map where the keys are node identifiers (e.g., hostnames) and
     *         the values are {@link RuntimeInfo} objects containing runtime details for each node.
     */
    Map<String, RuntimeInfo> getNodesSnapshot();

    /**
     * Retrieves the runtime snapshot of a specific node identified by the given hostname.
     * The snapshot provides details about system resources such as CPU usage, memory usage,
     * disk usage, and the timestamp when the data was captured.
     *
     * @param hostName the hostname of the node for which the runtime snapshot is to be retrieved
     * @return a {@link RuntimeInfo} object containing the runtime details of the specified node
     */
    RuntimeInfo getNodeSnapshot(String hostName);

    /**
     * Retrieves the live notifications for a given user. These notifications are dynamically
     * fetched and represent real-time updates relevant to the provided user.
     *
     * @param userId the unique identifier of the user for whom notifications are to be retrieved
     * @return a list of live notifications of type T associated with the specified user
     */
    List<T> getLiveNotifications(String userId);

    /**
     * Retrieves the unread notifications for a given user. Notifications are grouped
     * by categories or types, represented as keys in the returned map. Each key maps
     * to a list of unread notifications specific to that category or type.
     *
     * @param userId the unique identifier of the user for whom unread notifications are to be retrieved
     * @return a map where the keys are notification categories (e.g., types or groups) and the values
     *         are lists of unread notifications of type T associated with the specified user
     */
    Map<String, List<T>> getUnreadNotifications(String userId);

    /**
     * Retrieves a paginated list of notifications for a specified user.
     *
     * @param userId the unique identifier of the user for whom notifications are to be retrieved
     * @param page the page number for paginated results
     * @return a list of notifications of type T associated with the specified user for the given page
     */
    List<T> getNotifications(String userId, int page);

    /**
     * Acknowledges the specified notifications for a given user. This method processes
     * the provided list of notification IDs and marks them as acknowledged for the user
     * identified by the given user ID.
     *
     * @param notifications a list of notification IDs to be acknowledged
     * @param userId the unique identifier of the user acknowledging the notifications
     * @return a list of successfully acknowledged notification IDs
     */
    List<Long> acknowledgeNotification(List<Long> notifications, String userId);

    /**
     * Deletes all the notifications of the specified user.
     *
     * @param userId the unique identifier of the user
     */
    void deleteAll(String userId);

    /**
     * Retrieves the online status of all monitored nodes. This method returns a mapping of node identifiers
     * (e.g., hostnames) to their respective online statuses, where the value is a boolean indicating whether
     * the node is online (true) or offline (false).
     *
     * @return a map containing node identifiers as keys and their online status as boolean values
     */
    Map<String, Boolean> getNodesOnlineStatus();

}
