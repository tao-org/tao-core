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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import ro.cs.tao.component.LongIdentifiable;
import ro.cs.tao.execution.callback.EndpointDescriptor;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Models the execution of an workflow.
 *
 * @author Cosmin Udroiu
 */
public class ExecutionJob extends LongIdentifiable implements StatusChangeListener {
    private String name;
    private String appId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long workflowId;
    private String queryId;
    private String userId;
    private String jobOutputPath;
    private ExecutionStatus executionStatus;
    private boolean external;
    private EndpointDescriptor callbackDescriptor;
    private Map<String, List<String>> taskDependencies;
    private JobType jobType;
    private String batchId;
    @JsonManagedReference
    private List<ExecutionTask> tasks;

    public ExecutionJob() {}

    @Override
    public Long defaultId() { return null; }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }

    public String getQueryId() { return queryId; }
    public void setQueryId(String queryId) { this.queryId = queryId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Long getWorkflowId() { return workflowId; }
    public void setWorkflowId(Long workflowId) { this.workflowId = workflowId; }

    public String getJobOutputPath() {
        return jobOutputPath;
    }

    public void setJobOutputPath(String jobOutputPath) {
        this.jobOutputPath = jobOutputPath;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setExecutionStatus(ExecutionStatus executionStatus) {
        this.executionStatus = executionStatus;
    }
    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public boolean isExternal() { return external; }
    public void setExternal(boolean external) { this.external = external; }

    public EndpointDescriptor getCallbackDescriptor() { return callbackDescriptor; }
    public void setCallbackDescriptor(EndpointDescriptor descriptor) { this.callbackDescriptor = descriptor; }

    public Map<String, List<String>> getTaskDependencies() {
        return taskDependencies;
    }

    public void setTaskDependencies(Map<String, List<String>> taskDependencies) {
        this.taskDependencies = taskDependencies;
    }

    public void addTaskDependency(long taskId, long parentTaskId) {
        if (this.taskDependencies == null) {
            this.taskDependencies = new HashMap<>();
        }
        final String key = String.valueOf(taskId);
        if (!this.taskDependencies.containsKey(key)) {
            this.taskDependencies.put(key, new ArrayList<>());
        }
        this.taskDependencies.get(key).add(String.valueOf(parentTaskId));
    }

    public void setTasks(List<ExecutionTask> tasks) {
        this.tasks = tasks;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    @JsonManagedReference
    public List<ExecutionTask> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        return tasks.stream().distinct().collect(Collectors.toList());
    }

    public List<ExecutionTask> orderedTasks() {
        return getTasks().stream()
                    .distinct()
                    .sorted(Comparator.comparing(ExecutionTask::getId))
                    .collect(Collectors.toList());
    }

    public List<ExecutionTask> rootTasks() {
        return orderedTasks().stream().filter(t -> t.getLevel() == 1).collect(Collectors.toList());
    }

    public void addTask(ExecutionTask task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        if (this.tasks.stream().noneMatch(t -> t.getId() != null && t.getId().equals(task.getId()))) {
            this.tasks.add(task);
        }
    }

    public List<ExecutionTask> find(ExecutionStatus status) {
        List<ExecutionTask> running = null;
        if (this.tasks != null && !this.tasks.isEmpty()) {
            running = this.tasks.stream()
                                .filter(t -> t.getExecutionStatus() == status)
                                .collect(Collectors.toList());
        }
        return running;
    }
}
