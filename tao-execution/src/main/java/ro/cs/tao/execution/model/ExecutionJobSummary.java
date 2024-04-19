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

package ro.cs.tao.execution.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Trimmed down version of {@link ExecutionJob} that can be used for brief information by upper layers.
 *
 * @author Cosmin Cara
 */
public class ExecutionJobSummary {
    private long id;
    private String workflowName;
    private String jobName;
    private LocalDateTime jobStart;
    private LocalDateTime jobEnd;
    private String userId;
    private ExecutionStatus jobStatus;
    private List<ExecutionTaskSummary> taskSummaries;

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getJobName() { return jobName; }

    public void setJobName(String jobName) { this.jobName = jobName; }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public LocalDateTime getJobStart() {
        return jobStart;
    }

    public void setJobStart(LocalDateTime jobStart) {
        this.jobStart = jobStart;
    }

    public LocalDateTime getJobEnd() {
        return jobEnd;
    }

    public void setJobEnd(LocalDateTime jobEnd) {
        this.jobEnd = jobEnd;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public ExecutionStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(ExecutionStatus status) {
        this.jobStatus = status;
    }

    public List<ExecutionTaskSummary> getTaskSummaries() {
        return taskSummaries;
    }

    public void setTaskSummaries(List<ExecutionTaskSummary> taskSummaries) {
        this.taskSummaries = taskSummaries;
    }
}
