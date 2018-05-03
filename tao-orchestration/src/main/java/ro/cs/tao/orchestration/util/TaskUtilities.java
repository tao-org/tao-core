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

package ro.cs.tao.orchestration.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ro.cs.tao.component.ComponentLink;
import ro.cs.tao.component.GroupComponent;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.execution.model.DataSourceExecutionTask;
import ro.cs.tao.execution.model.ExecutionGroup;
import ro.cs.tao.execution.model.ExecutionTask;
import ro.cs.tao.execution.model.ProcessingExecutionTask;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for shortcutting various operations on ExecutionTasks.
 *
 * @author Cosmin Cara
 */
@Component
public class TaskUtilities {

    @Autowired
    private static PersistenceManager persistenceManager;

    public static TaoComponent getComponentFor(ExecutionTask task) {
        TaoComponent component = null;
        if (task != null) {
            try {
                if (task instanceof ProcessingExecutionTask) {
                    component = getComponentFor((ProcessingExecutionTask) task);
                } else if (task instanceof ExecutionGroup) {
                    component = getComponentFor((ExecutionGroup) task);
                } else if (task instanceof DataSourceExecutionTask) {
                    component = getComponentFor((DataSourceExecutionTask) task);
                }
            } catch (PersistenceException pex) {
                Logger.getLogger(TaskUtilities.class.getName()).severe(pex.getMessage());
            }
        }
        return component;
    }

    public static ProcessingComponent getComponentFor(ProcessingExecutionTask task) throws PersistenceException {
        WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        return persistenceManager.getProcessingComponentById(node.getComponentId());
    }

    public static GroupComponent getComponentFor(ExecutionGroup task) throws PersistenceException {
        WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        return persistenceManager.getGroupComponentById(node.getComponentId());
    }

    public static DataSourceComponent getComponentFor(DataSourceExecutionTask task) {
        WorkflowNodeDescriptor node = persistenceManager.getWorkflowNodeById(task.getWorkflowNodeId());
        return persistenceManager.getDataSourceInstance(node.getComponentId());
    }

    public static int getSourceCardinality(ExecutionTask task) {
        TaoComponent component = getComponentFor(task);
        return component.getSourceCardinality();
    }

    public static int getTargetCardinality(ExecutionTask task) {
        TaoComponent component = getComponentFor(task);
        return component.getTargetCardinality();
    }

    /**
     * Returns the mapping between two tasks. The map keys are the targetTask's inputs, while the map values
     * are the sourceTask's outputs.
     */
    public static Map<String, String> getConnectedInputs(ExecutionTask sourceTask, ExecutionTask targetTask) {
        Map<String, String> connections = new LinkedHashMap<>();
        WorkflowNodeDescriptor targetNode = persistenceManager.getWorkflowNodeById(targetTask.getWorkflowNodeId());
        WorkflowNodeDescriptor sourceNode = persistenceManager.getWorkflowNodeById(sourceTask.getWorkflowNodeId());
        List<ComponentLink> links = targetNode.getIncomingLinks();
        if (links != null) {
            links.stream()
                    .filter(l -> l.getSourceNodeId().equals(sourceNode.getId()))
                    .forEach(l -> connections.put(l.getOutput().getName(), l.getInput().getName()));
        }
        return connections;
    }
}
