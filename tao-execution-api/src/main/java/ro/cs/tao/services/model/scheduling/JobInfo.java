package ro.cs.tao.services.model.scheduling;

import ro.cs.tao.execution.model.ExecutionStatus;
import ro.cs.tao.utils.DateUtils;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Information related to a job.
 * 
 * @author Lucian Barbulescu
 *
 */
public class JobInfo {
	private static final DateFormat df = DateUtils.getFormatterAtLocal("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	
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
			final Date d = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
			this.startTime = df.format(d);
		}
	}
	public String getEndTime() {
		return endTime;
	}
	public void setEndTime(LocalDateTime endTime) {
		if (endTime == null) {
			this.endTime = "";
		} else {
			final Date d = Date.from(endTime.atZone(ZoneId.systemDefault()).toInstant());
			this.endTime = df.format(d);
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
