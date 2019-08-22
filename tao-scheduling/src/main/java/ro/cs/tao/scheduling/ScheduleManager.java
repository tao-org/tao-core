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

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.jdbcjobstore.InvalidConfigurationException;
import org.quartz.impl.jdbcjobstore.JobStoreTX;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.simpl.SimpleThreadPool;
import org.quartz.utils.DBConnectionManager;

import ro.cs.tao.configuration.ConfigurationManager;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.services.bridge.spring.SpringContextBridge;

public class ScheduleManager {
	public final static String JOB_STATUS_KEY = "jobStatus";
	
	private final static String INSTANCE_NAME_KEY = "tao.quartz.scheduler.instanceName";
	private final static String DRIVER_DELEGATE_CLASS_KEY = "tao.quartz.jobStore.driverDelegateClass";
	private final static String TABLE_PREFIX_KEY = "tao.quartz.jobStore.tablePrefix";
	private final static String DATASOURCE_KEY = "tao.quartz.jobStore.dataSource";

	private static ScheduleManager instance;
	private static final Logger logger;
	private Scheduler quarzScheduler;

	static {
		logger = Logger.getLogger(ScheduleManager.class.getName());
		instance = new ScheduleManager();
	}

	private ScheduleManager() {
		try {
			final PersistenceManager persistenceManager = SpringContextBridge.services().getService(PersistenceManager.class);
			
			final String dataSource = ConfigurationManager.getInstance().getValue(DATASOURCE_KEY);
			final String instanceName = ConfigurationManager.getInstance().getValue(INSTANCE_NAME_KEY);
			final String instanceID = instanceName + "-1";
			final String tablePrefix = ConfigurationManager.getInstance().getValue(TABLE_PREFIX_KEY);
			final String driverDelegateClass = ConfigurationManager.getInstance().getValue(DRIVER_DELEGATE_CLASS_KEY);

			DBConnectionManager.getInstance().addConnectionProvider(dataSource,
					new SharedConnectionProvider(persistenceManager.getDataSource()));

			// Configure jdbc store
			JobStoreTX jdbcJobStore = new JobStoreTX();
			jdbcJobStore.setInstanceName(instanceName);
			jdbcJobStore.setDataSource(dataSource);
			jdbcJobStore.setTablePrefix(tablePrefix);
			jdbcJobStore.setDriverDelegateClass(driverDelegateClass);

			// Create a scheduler.
			DirectSchedulerFactory.getInstance().createScheduler(instanceName, instanceID,
					new SimpleThreadPool(5, Thread.NORM_PRIORITY), jdbcJobStore);
			this.quarzScheduler = DirectSchedulerFactory.getInstance().getScheduler(instanceName);
		} catch (SchedulerException | InvalidConfigurationException  e) {
			logger.warning(String.format("Scheduler could not be instantiated: %s", e.getMessage()));
		}
	}

	public static void start() throws ScheduleException {
		if (instance == null || instance.quarzScheduler == null) {
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
		if (instance == null || instance.quarzScheduler == null) {
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

	public static JobKey schedule(Job job, String id, LocalDateTime startTime, int interval, Map<String, Object> parameters)
			throws ScheduleException {
		if (instance == null || instance.quarzScheduler == null) {
			throw new ScheduleException("Scheduling not available");
		}
		try {
			if (!instance.quarzScheduler.isStarted()) {
				throw new ScheduleException("Scheduler is not started");
			}
			final JobDescriptor descriptor = job.createDescriptor(id, startTime, interval);
			final JobDetail jobDetail = descriptor.buildJobDetail(job.getClass(), parameters);
			final Trigger trigger = descriptor.buildTrigger();
			if (instance.quarzScheduler.checkExists(jobDetail.getKey())) {
				// check if the previous job was running and set the id to the new job
				final JobDataMap newMap = jobDetail.getJobDataMap(); 
				final JobDataMap oldMap = instance.quarzScheduler.getJobDetail(jobDetail.getKey()).getJobDataMap();
				oldMap.entrySet().stream().filter(el -> !newMap.containsKey(el.getKey())).forEach(el -> newMap.put(el.getKey(), el.getValue()));
				try {
					instance.quarzScheduler.deleteJob(jobDetail.getKey());
				} catch (Exception e1) {
					logger.warning(String.format("Unable to delete job '%s' (next run: %s)", jobDetail.getKey(),
							descriptor.getFireTime()));
				}
				instance.quarzScheduler.scheduleJob(jobDetail, trigger);
				logger.info(String.format("Rescheduled job '%s' (next run: %s)", jobDetail.getKey(),
						descriptor.getFireTime()));
			} else {
				instance.quarzScheduler.scheduleJob(jobDetail, trigger);
				logger.info(String.format("Scheduled job '%s' (next run: %s)", jobDetail.getKey(),
						descriptor.getFireTime()));
			}
			return jobDetail.getKey();
		} catch (SchedulerException e) {
			logger.severe(String.format("Failed to schedule job '%s'. Reason: %s", id, e.getMessage()));
			throw new ScheduleException(e);
		}
	}
	
	public static boolean remove(final JobKey key) throws ScheduleException {
		if (instance == null || instance.quarzScheduler == null) {
			throw new ScheduleException("Scheduling not available");
		}
		try {
			return instance.quarzScheduler.deleteJob(key);
		} catch(SchedulerException e) {
			logger.severe(String.format("Failed to delete job '%s'. Reason: %s", key.getName(), e.getMessage()));
			throw new ScheduleException(e);
		}
	}
	
	public static void pause(final JobKey key) throws ScheduleException {
		if (instance == null || instance.quarzScheduler == null) {
			throw new ScheduleException("Scheduling not available");
		}
		try {
			instance.quarzScheduler.pauseJob(key);
		} catch(SchedulerException e) {
			logger.severe(String.format("Failed to pause job '%s'. Reason: %s", key.getName(), e.getMessage()));
			throw new ScheduleException(e);
		}
	}
	
	
	public static void resume(final JobKey key) throws ScheduleException {
		if (instance == null || instance.quarzScheduler == null) {
			throw new ScheduleException("Scheduling not available");
		}
		try {
			instance.quarzScheduler.resumeJob(key);
		} catch(SchedulerException e) {
			logger.severe(String.format("Failed to resume job '%s'. Reason: %s", key.getName(), e.getMessage()));
			throw new ScheduleException(e);
		}
	}
	
	
	public static List<JobDetail> getUserSchedules(final String userName) throws ScheduleException {
		if (instance == null || instance.quarzScheduler == null) {
			throw new ScheduleException("Scheduling not available");
		}
		try {
			final List<JobDetail> jobs = new LinkedList<>();
			final Set<JobKey> keys = instance.quarzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(userName));
			for (JobKey key : keys) {
				JobDetail jd = instance.quarzScheduler.getJobDetail(key); 
				
				// add the current state to the parameters
				final List<? extends Trigger> triggers = instance.quarzScheduler.getTriggersOfJob(key);
				if (triggers == null || triggers.isEmpty()) {
					// no trigger associated!
					jd.getJobDataMap().put(JOB_STATUS_KEY, TriggerState.NONE.name());
				} else {
					final Trigger t = triggers.get(0);
					final TriggerState triggerState = instance.quarzScheduler.getTriggerState(t.getKey());
					if (triggerState == null) {
						// no trigger associated!
						jd.getJobDataMap().put(JOB_STATUS_KEY, TriggerState.NONE.name());
					} else {
						jd.getJobDataMap().put(JOB_STATUS_KEY, triggerState.name());
					}
				}
				
				jobs.add(jd);
			}
			return jobs;
		} catch(SchedulerException e) {
			logger.severe(String.format("Failed to get the schedules for user '%s'. Reason: %s", userName, e.getMessage()));
			throw new ScheduleException(e);
		}
	}
	
}
