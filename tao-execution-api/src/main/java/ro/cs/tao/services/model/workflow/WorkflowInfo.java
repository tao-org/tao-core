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

package ro.cs.tao.services.model.workflow;

import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public class WorkflowInfo {

    protected Long id;
    private final String name;
    private final String description;
    private final String userId;
    private final Visibility visibility;
    protected final Status status;
    private final String path;
    private final boolean active;
    private final String image;
    private final LocalDateTime created;
    private List<String> tags;

    public WorkflowInfo(WorkflowDescriptor workflow, String image) {
        this.id = workflow.getId();
        this.userId = workflow.getUserId();
        this.visibility = workflow.getVisibility();
        this.status = workflow.getStatus();
        this.path = workflow.getPath();
        this.active = workflow.isActive();
        this.name = workflow.getName();
        this.description = workflow.getDescription();
        this.image = image;
        this.created = workflow.getCreated();
        this.tags = workflow.getTags();
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public String getDescription() { return description; }

    public String getUserId() { return userId; }

    public Visibility getVisibility() { return visibility; }

    public Status getStatus() { return status; }

    public String getPath() { return path; }

    public boolean isActive() { return active; }

    public String getImage() {
        return image;
    }

    public LocalDateTime getCreated() { return created; }

    public List<String> getTags() { return tags; }

    public WorkflowDescriptor toWorkflowDescriptor() {
        final WorkflowDescriptor descriptor = new WorkflowDescriptor();
        descriptor.setId(this.id);
        descriptor.setUserId(this.userId);
        descriptor.setVisibility(this.visibility);
        descriptor.setStatus(this.status);
        descriptor.setPath(this.path);
        descriptor.setActive(this.active);
        descriptor.setName(this.name);
        descriptor.setCreated(this.created);
        descriptor.setTags(this.tags);
        return descriptor;
    }
}
