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
package ro.cs.tao.services.model.execution;

import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.execution.model.ExecutionStatus;

import java.time.LocalDateTime;

/**
 * Light wrapper over ExecutionJob entity for services operations purpose
 * @author Oana H.
 */
public class ExecutionJobInfo {

    private long id;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long workflowId;
    private String queryId;
    private String userName;
    private ExecutionStatus executionStatus;

    public ExecutionJobInfo(){}

    public ExecutionJobInfo(final ExecutionJob executionJob){
        this.id = executionJob.getId();
        this.startTime = executionJob.getStartTime();
        this.endTime = executionJob.getEndTime();
        this.workflowId = executionJob.getWorkflowId();
        this.queryId = executionJob.getQueryId();
        this.userName = executionJob.getUserName();
        this.executionStatus = executionJob.getExecutionStatus();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(long workflowId) {
        this.workflowId = workflowId;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }
}
