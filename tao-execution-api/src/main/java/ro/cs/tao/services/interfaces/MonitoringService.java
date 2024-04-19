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
 * @author Cosmin Cara
 */
public interface MonitoringService<T> extends TAOService {

    RuntimeInfo getMasterSnapshot();

    Map<String, RuntimeInfo> getNodesSnapshot();

    RuntimeInfo getNodeSnapshot(String hostName);

    List<T> getLiveNotifications(String userId);

    Map<String, List<T>> getUnreadNotifications(String userId);

    List<T> getNotifications(String userId, int page);

    List<Long> acknowledgeNotification(List<Long> notifications, String userId);

    void deleteAll(String userId);

    Map<String, Boolean> getNodesOnlineStatus();

}
