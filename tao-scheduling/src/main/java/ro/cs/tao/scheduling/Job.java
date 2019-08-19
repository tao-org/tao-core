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
package ro.cs.tao.scheduling;

import org.quartz.JobDetail;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface Job extends org.quartz.Job {
    String groupName();
    default JobDescriptor createDescriptor(String name, LocalDateTime startTime, int rateInMinutes) {
        JobDescriptor jd = new JobDescriptor()
                .setName(name)
                .setGroup(groupName())
                .setRate(rateInMinutes);
        if (startTime == null) {
            return jd.setFireTime(LocalDateTime.now().plusSeconds(10));
        }
        return jd.setFireTime(startTime);
    }
    default JobDetail buildDetail(String jobName, LocalDateTime startTime, int interval, Map<String, Object> parameters) {
        return createDescriptor(jobName, startTime, interval)
                .buildJobDetail(getClass(), parameters);
    }
}
