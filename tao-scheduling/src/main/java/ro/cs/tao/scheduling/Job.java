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
package ro.cs.tao.scheduling;

import org.quartz.JobDetail;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * @author Cosmin Cara
 */
public interface Job extends org.quartz.Job {
    String configKey();
    String groupName();
    default JobDescriptor createDescriptor(String name, int rateInMinutes) {
        return new JobDescriptor()
                .setName(name)
                .setGroup(groupName())
                .setFireTime(LocalDateTime.now().plusSeconds(10))
                .setRate(rateInMinutes);
    }
    default JobDetail buildDetail(String jobName, int interval, Map<String, Object> parameters) {
        return createDescriptor(jobName, interval)
                .buildJobDetail(getClass(), parameters);
    }
}
