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

package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.TargetDescriptor;
import ro.cs.tao.datasource.beans.Parameter;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.services.model.workflow.WorkflowInfo;

import javax.tools.FileObject;
import java.util.List;
import java.util.Map;

public interface WebProcessingService extends TAOService {

    List<WorkflowInfo> getCapabilities();

    ProcessInfo describeProcess(long workflowId);

    long execute(long workflowId, Map<String, Map<String, String>> parameters);

    ExecutionJob getStatus(long jobId);

    List<FileObject> getJobResult(long jobId);

    interface ProcessInfo {

        WorkflowInfo getWorkflowInfo();
        Map<String, List<Parameter>> getParameters();
        List<TargetDescriptor> getOutputs();
    }
}
