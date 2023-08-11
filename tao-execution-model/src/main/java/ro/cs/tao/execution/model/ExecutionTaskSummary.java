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

public class ExecutionTaskSummary {

    private long taskId;
    private String workflowName;
    private String componentName;
    private LocalDateTime taskStart;
    private LocalDateTime taskEnd;
    private LocalDateTime lastUpdated;
    private double percentComplete;
    private String host;
    private ExecutionStatus taskStatus;
    private String output;
    private String command;
    private String userId;
    private Integer usedCPU;
    private Integer usedRAM;
    private String jobName;
    private String componentType;

    public long getTaskId() {
        return taskId;
    }
    public void setTaskId(long taskId) {
        this.taskId = taskId;
    }

    public String getWorkflowName() { return workflowName; }
    public void setWorkflowName(String workflowName) { this.workflowName = workflowName; }

    public String getComponentName() {
        return componentName;
    }
    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public LocalDateTime getTaskStart() {
        return taskStart;
    }
    public void setTaskStart(LocalDateTime taskStart) {
        this.taskStart = taskStart;
    }

    public LocalDateTime getTaskEnd() {
        return taskEnd;
    }
    public void setTaskEnd(LocalDateTime taskEnd) {
        this.taskEnd = taskEnd;
    }

    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }

    public ExecutionStatus getTaskStatus() {
        return taskStatus;
    }
    public void setTaskStatus(ExecutionStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public double getPercentComplete() {
        return percentComplete;
    }
    public void setPercentComplete(double percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getOutput() {
        return output;
    }
    public void setOutput(String output) {
        this.output = output;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getUsedCPU() {
        return usedCPU;
    }

    public void setUsedCPU(Integer usedCPU) {
        this.usedCPU = usedCPU;
    }

    public Integer getUsedRAM() {
        return usedRAM;
    }

    public void setUsedRAM(Integer usedRAM) {
        this.usedRAM = usedRAM;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }
}
