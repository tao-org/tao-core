/**
 * 
 */
package ro.cs.tao.services.model.scheduling;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/** Information related to a schedule.
 * 
 * @author Lucian Barbulescu
 *
 */
public class SchedulingInfo {
	private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private String id;
	private String friendlyName;
	private String state;
	private long workflowId;
	private String workflowName;
    private int repeatInterval;
    private String startTime;
	private SchedulingMode mode;
	private List<JobInfo> jobs;
	private Map<String, Map<String, String>> parameters;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getFriendlyName() {
		return friendlyName;
	}
	public void setFriendlyName(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public long getWorkflowId() {
		return workflowId;
	}
	public void setWorkflowId(long workflowId) {
		this.workflowId = workflowId;
	}
	public String getWorkflowName() {
		return workflowName;
	}
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}
	public int getRepeatInterval() {
		return repeatInterval;
	}
	public void setRepeatInterval(int repeatInterval) {
		this.repeatInterval = repeatInterval;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(LocalDateTime startTime) {
		if (startTime == null) {
			this.startTime="";
		} else {
			final Date d = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
			this.startTime = df.format(d);
		}
	}
	public SchedulingMode getMode() {
		return mode;
	}
	public void setMode(SchedulingMode mode) {
		this.mode = mode;
	}
	public List<JobInfo> getJobs() {
		if (jobs == null) {
			return new ArrayList<JobInfo>();
		}
		return jobs;
	}
	public void setJobs(List<JobInfo> jobs) {
		this.jobs = jobs;
	}
	public Map<String, Map<String, String>> getParameters() {
		if (parameters == null) {
			return new HashMap<String, Map<String, String>>();
		}
		return parameters;
	}
	public void setParameters(Map<String, Map<String, String>> parameters) {
		this.parameters = parameters;
	}
	
}
