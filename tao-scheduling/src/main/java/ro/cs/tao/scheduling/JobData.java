/**
 * 
 */
package ro.cs.tao.scheduling;

import java.io.Serializable;

import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;

/**
 * @author Lucian Barbulescu
 *
 */
public class JobData implements Serializable {

	/** Generated serial ID.	 */
	private static final long serialVersionUID = -5748226728972063672L;
	
	/**
	 * The details for the scheduler job.
	 */
	private final JobDetail jobDetail;
	
	/**
	 * The trigger currently attached to the job.
	 */
	private final Trigger trigger;

	/**
	 * The trigger state.
	 */
	private final TriggerState triggerState;
	
	/**
	 * Constructor.
	 * 
	 * @param jobDetail the scheduler job details.
	 * @param trigger the trigger for the scheduler.
	 */
	JobData(final JobDetail jobDetail, final Trigger trigger, final TriggerState triggerState) {
		this.jobDetail = jobDetail;
		this.trigger = trigger;
		this.triggerState = triggerState;
	}

	/** Get the job details.
	 *  
	 * @return the job details.
	 */
	public JobDetail getJobDetail() {
		return jobDetail;
	}

	/**Get the job trigger.
	 * 
	 * @return the trigger
	 */
	public Trigger getTrigger() {
		return trigger;
	}

	/** Get the trigegr state.
	 * @return the trigger state
	 */
	public TriggerState getTriggerState() {
		return triggerState;
	}
	
}
