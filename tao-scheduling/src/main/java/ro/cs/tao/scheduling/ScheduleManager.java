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
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Map;
import java.util.logging.Logger;

public class ScheduleManager {
    private static final ScheduleManager instance;
    private static final Logger logger;
    private Scheduler quarzScheduler;

    static {
        logger = Logger.getLogger(ScheduleManager.class.getName());
        instance = new ScheduleManager();
    }

    private ScheduleManager() {
        try {
            this.quarzScheduler = StdSchedulerFactory.getDefaultScheduler();
        } catch (SchedulerException e) {
            logger.warning(String.format("Scheduler could not be instantiated: %s", e.getMessage()));
        }
    }

    public static void start() throws ScheduleException {
        if (instance.quarzScheduler == null) {
            throw new ScheduleException("Scheduling not available");
        }
        try {
            if (!instance.quarzScheduler.isStarted()) {
                instance.quarzScheduler.start();
            }
        } catch (SchedulerException e) {
            logger.severe(String.format("Scheduler could not be started: %s", e.getMessage()));
            throw new ScheduleException(e);
        }
    }

    public static void stop() throws ScheduleException {
        if (instance.quarzScheduler == null) {
            throw new ScheduleException("Scheduling not available");
        }
        try {
        if (instance.quarzScheduler.isStarted()) {
            instance.quarzScheduler.standby();
        }
        } catch (SchedulerException e) {
            logger.severe(String.format("Scheduler could not be stopped: %s", e.getMessage()));
            throw new ScheduleException(e);
        }
    }

    public static void schedule(Job job, String name, int interval, Map<String, Object> parameters) throws ScheduleException {
        if (instance.quarzScheduler == null) {
            throw new ScheduleException("Scheduling not available");
        }
        try {
            if (!instance.quarzScheduler.isStarted()) {
                throw new ScheduleException("Scheduler is not started");
            }
            final JobDescriptor descriptor = job.createDescriptor(name, interval);
            final JobDetail jobDetail = job.buildDetail(name, interval, parameters);
            final Trigger trigger = descriptor.buildTrigger();
            if (instance.quarzScheduler.checkExists(jobDetail.getKey())) {
                try {
                    instance.quarzScheduler.deleteJob(jobDetail.getKey());
                } catch (Exception e1) {
                    logger.warning(String.format("Unable to delete job '%s' (next run: %s)",
                                                 jobDetail.getKey(), descriptor.getFireTime()));
                }
                instance.quarzScheduler.rescheduleJob(trigger.getKey(), trigger);
                logger.info(String.format("Rescheduled job '%s' (next run: %s)",
                                          jobDetail.getKey(), descriptor.getFireTime()));
            } else {
                instance.quarzScheduler.scheduleJob(jobDetail, trigger);
                logger.info(String.format("Scheduled job '%s' (next run: %s)",
                                          jobDetail.getKey(), descriptor.getFireTime()));
            }
        } catch (SchedulerException e) {
            logger.severe(String.format("Failed to schedule job '%s'. Reason: %s",
                                        name, e.getMessage()));
            throw new ScheduleException(e);
        }
    }

}
