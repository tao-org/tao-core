/**
 * 
 */
package ro.cs.tao.services.interfaces;

import ro.cs.tao.services.model.scheduling.SchedulingInfo;
import ro.cs.tao.services.model.scheduling.SchedulingMode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Interface for job scheduling operations
 * 
 * @author Lucian Barbulescu
 */
public interface SchedulingService extends TAOService {

	/**
	 * List the schedules for the current user.
	 * 
	 * @return the list of schedule names for the current user
	 */
	List<SchedulingInfo> listUserSchedules(); 
	
	/**
	 * Add a new workflow execution schedule for the current user.
	 * 
	 * @param name the schedule friendly name
	 * @param startDate the date the execution should start. If null, the execution starts immediately
	 * @param repeatInterval the execution repeat interval, in seconds
	 * @param workflowId the workflow that will be executed
	 * @param inputs the workflow inputs map
	 * @param mode the scheduling execution mode
	 * @return the name allocated to the schedule job
	 */
	String addExecutionSchedule(final String name, LocalDateTime startDate, int repeatInterval, final long workflowId, Map<String, Map<String, String>> inputs, final SchedulingMode mode);

	
	/**
	 * Update an execution schedule for the current user.
	 * 
	 * @param id the schedule id
	 * @param name the schedule friendly name
	 * @param startDate the date the execution should start. If null, the execution starts immediately
	 * @param repeatInterval the execution repeat interval, in seconds
	 * @param workflowId the workflow that will be executed
	 * @param inputs the workflow inputs map
	 * @param mode the scheduling execution mode
	 * @return the name allocated to the schedule job
	 */
	String updateExecutionSchedule(final String id, final String name, LocalDateTime startDate, int repeatInterval, final long workflowId, Map<String, Map<String, String>> inputs, final SchedulingMode mode);

	
	/**
	 * Delete a schedule for the current user given its identifier.
	 * @param scheduleID the schedule identifier
	 * @return true if the schedule was removed, false otherwise
	 */
	boolean removeExecutionSchedule(final String scheduleID);
	
	/**
	 * Pause a schedule for the current user given its identifier.
	 * @param scheduleID the schedule identifier
	 */
	void pauseExecutionSchedule(final String scheduleID);
	
	/**
	 * Pause a schedule for the current user given its identifier.
	 * @param scheduleID the schedule identifier
	 */
	void resumeExecutionSchedule(final String scheduleID);
}
