/*
 * Copyright (C) 2017 CS ROMANIA
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
