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
    private String host;
    private ExecutionStatus taskStatus;

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

}
