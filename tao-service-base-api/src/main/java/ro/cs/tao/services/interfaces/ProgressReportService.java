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

import ro.cs.tao.messaging.TaskProgress;

import java.util.List;

public interface ProgressReportService extends TAOService {

    /**
     * Returns information about current long-running tasks.
     * Long-running tasks should report their progress via the common message bus
     * and topics matching the pattern "(.+)progress".
     *
     * @param category The task category (optional)
     * @param jsonFilter Additional filter expressed in json (optional)
     * @return  The list of tasks in progress
     *
     * @see ro.cs.tao.messaging.Topic for possible categories
     */
    List<TaskProgress> getRunningTasks(String category, String jsonFilter);

}
