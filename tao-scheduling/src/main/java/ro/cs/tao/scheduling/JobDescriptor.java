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

import org.quartz.*;
import ro.cs.tao.utils.StringUtils;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * @author Cosmin Cara
 */
public class JobDescriptor {
    private String name;
    private String group;
    private LocalDateTime fireTime;
    private int repeatInterval;

    public String getName() { return this.name; }
    public JobDescriptor setName(final String name) {
        this.name = name;
        return this;
    }

    public String getGroup() { return this.group != null ? this.group : "Generic"; }
    public JobDescriptor setGroup(String group) {
        this.group = group;
        return this;
    }

    public LocalDateTime getFireTime() {
        if (fireTime == null) {
            fireTime = LocalDateTime.now().plusMinutes(1);
        }
        return fireTime;
    }
    public JobDescriptor setFireTime(final LocalDateTime fireTime) {
        this.fireTime = fireTime;
        return this;
    }

    public int getRepeatInterval() { return repeatInterval; }
    public JobDescriptor setRate(final int minutes) {
        this.repeatInterval = minutes;
        return this;
    }

    private String buildName() {
        return StringUtils.isNullOrEmpty(name) ? UUID.randomUUID().toString() : name;
    }

    public Trigger buildTrigger() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put("fireTime", getFireTime());
        dataMap.put("repeatInterval", getRepeatInterval());
        return TriggerBuilder.newTrigger()
                .withIdentity(buildName(), getGroup())
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                                                   .withMisfireHandlingInstructionNextWithExistingCount()
                                                   .withIntervalInMinutes(this.repeatInterval)
                                                   .repeatForever())
                .startAt(Date.from(fireTime.atZone(ZoneId.systemDefault()).toInstant()))
                .usingJobData(dataMap)
                .build();
    }

    public JobDetail buildJobDetail(Class<? extends Job> jobType,
                                    Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new IllegalArgumentException("Missing job parameters");
        }
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("descriptor", this);
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            jobDataMap.put(entry.getKey(), entry.getValue());
        }
        return JobBuilder.newJob(jobType)
                         .withIdentity(getName(), getGroup())
                         .usingJobData(jobDataMap)
                         .build();
    }

    public static JobDescriptor buildDescriptor(Trigger trigger) {
        return new JobDescriptor()
                .setName(trigger.getKey().getName())
                .setGroup(trigger.getKey().getGroup())
                .setFireTime((LocalDateTime) trigger.getJobDataMap().get("fireTime"))
                .setRate(trigger.getJobDataMap().getInt("repeatInterval"));
    }
}
