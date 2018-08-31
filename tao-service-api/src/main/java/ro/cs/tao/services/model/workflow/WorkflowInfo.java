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

public class WorkflowInfo {

    protected Long id;
    private String userName;
    private Visibility visibility;
    protected Status status;
    private String path;
    private boolean active;

    public WorkflowInfo(WorkflowDescriptor workflow) {
        this.id = workflow.getId();
        this.userName = workflow.getUserName();
        this.visibility = workflow.getVisibility();
        this.status = workflow.getStatus();
        this.path = workflow.getPath();
        this.active = workflow.isActive();
    }

    public Long getId() { return id; }

    public String getUserName() { return userName; }

    public Visibility getVisibility() { return visibility; }

    public Status getStatus() { return status; }

    public String getPath() { return path; }

    public boolean isActive() { return active; }
}
