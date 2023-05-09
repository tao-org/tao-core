package ro.cs.tao.services.model.scheduling;

import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.utils.DateUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Information related to a job.
 * 
 * @author Lucian Barbulescu
 *
 */
public class JobInfo {
	private static final DateTimeFormatter df = DateUtils.getFormatterAtLocal("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
	private long jobId;
	private String startTime;
	private String endTime;
	private ExecutionStatus status;
	private String name;
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(LocalDateTime startTime) {
		if (startTime == null) {
			this.startTime = "";
		} else {
			this.startTime = startTime.format(df);
		}
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalDateTime endTime) {
		if (endTime == null) {
			this.endTime = "";
		} else {
			this.endTime = endTime.format(df);
		}
	}
	public ExecutionStatus getStatus() {
		return status;
	}
	public void setStatus(ExecutionStatus status) {
		this.status = status;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
