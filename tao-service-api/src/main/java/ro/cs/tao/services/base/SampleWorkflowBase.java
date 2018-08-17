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

package ro.cs.tao.services.base;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.component.TaoComponent;
import ro.cs.tao.datasource.DataSourceComponent;
import ro.cs.tao.datasource.remote.FetchMode;
import ro.cs.tao.eodata.enums.Visibility;
import ro.cs.tao.persistence.PersistenceManager;
import ro.cs.tao.persistence.exception.PersistenceException;
import ro.cs.tao.services.interfaces.ComponentService;
import ro.cs.tao.services.interfaces.SampleWorkflow;
import ro.cs.tao.services.interfaces.WorkflowService;
import ro.cs.tao.workflow.WorkflowDescriptor;
import ro.cs.tao.workflow.WorkflowNodeDescriptor;
import ro.cs.tao.workflow.WorkflowNodeGroupDescriptor;
import ro.cs.tao.workflow.enums.ComponentType;
import ro.cs.tao.workflow.enums.Status;

import java.time.LocalDateTime;
import java.util.Map;

public abstract class SampleWorkflowBase implements SampleWorkflow {
    protected static PersistenceManager persistenceManager;
    protected static ComponentService componentService;
    protected static WorkflowService workflowService;
    private static final float xOrigin = 300;
    private static final float yOrigin = 150;
    private static final float xStep = 300;
    private static final float yStep = 150;

    public static void setPersistenceManager(PersistenceManager persistenceManager) {
        SampleWorkflowBase.persistenceManager = persistenceManager;
    }

    public static void setComponentService(ComponentService componentService) {
        SampleWorkflowBase.componentService = componentService;
    }

    public static void setWorkflowService(WorkflowService workflowService) {
        SampleWorkflowBase.workflowService = workflowService;
    }

    protected DataSourceComponent newDataSourceComponent(String sensor, String dataSource) throws PersistenceException {
        // let's have a DataSourceComponent
        String componentId = sensor + "-" + dataSource;
        DataSourceComponent dataSourceComponent;
        dataSourceComponent = persistenceManager.getDataSourceInstance(componentId);
        if (dataSourceComponent == null) {
            dataSourceComponent = new DataSourceComponent(sensor, dataSource);
            dataSourceComponent.setFetchMode(FetchMode.OVERWRITE);
            dataSourceComponent.setLabel(dataSourceComponent.getSensorName() + " from " + dataSourceComponent.getDataSourceName());
            dataSourceComponent.setVersion("1.0");
            dataSourceComponent.setDescription(dataSourceComponent.getId());
            dataSourceComponent.setAuthors("TAO Team");
            dataSourceComponent.setCopyright("(C) TAO Team");
            dataSourceComponent.setNodeAffinity("Any");
            persistenceManager.saveDataSourceComponent(dataSourceComponent);
        }
        return dataSourceComponent;
    }

    @Override
    public WorkflowDescriptor createWorkflowDescriptor() throws PersistenceException {
        WorkflowDescriptor descriptor = new WorkflowDescriptor();
        descriptor.setName(getName());
        descriptor.setStatus(Status.DRAFT);
        descriptor.setCreated(LocalDateTime.now());
        descriptor.setActive(true);
        descriptor.setUserName("admin");
        descriptor.setVisibility(Visibility.PRIVATE);
        descriptor = persistenceManager.saveWorkflowDescriptor(descriptor);
        addNodes(descriptor);
        return descriptor;
    }

    protected abstract String getName();

    protected abstract void addNodes(WorkflowDescriptor workflow) throws PersistenceException;

    protected WorkflowNodeDescriptor addNode(WorkflowDescriptor parent, String name, String componentId,
                                             ComponentType componentType, Map<String, String> customValues,
                                             WorkflowNodeDescriptor parentNode, ComponentType parentComponentType,
                                             Direction relativeDirection) throws PersistenceException {
        WorkflowNodeDescriptor node = new WorkflowNodeDescriptor();
        node.setWorkflow(parent);
        node.setName(name);
        float[] coords = placeNode(parentNode, relativeDirection);
        node.setxCoord(coords[0]);
        node.setyCoord(coords[1]);
        node.setComponentId(componentId);
        node.setComponentType(componentType);
        if (customValues != null) {
            for (Map.Entry<String, String> entry : customValues.entrySet()) {
                node.addCustomValue(entry.getKey(), entry.getValue());
            }
        }
        node.setCreated(LocalDateTime.now());
        node = workflowService.addNode(parent.getId(), node);
        if (parentNode != null) {
            TaoComponent component1 = componentService.findComponent(parentNode.getComponentId(), parentComponentType);
            TaoComponent component2 = componentService.findComponent(node.getComponentId(), componentType);
            workflowService.addLink(parentNode.getId(), component1.getTargets().get(0).getId(),
                                    node.getId(), component2.getSources().get(0).getId());
        }
        return node;
    }

    protected WorkflowNodeDescriptor addGroupNode(WorkflowDescriptor parent, String name,
                                                  WorkflowNodeDescriptor parentNode,
                                                  WorkflowNodeDescriptor... nodes) throws PersistenceException {
        WorkflowNodeGroupDescriptor grpNode = new WorkflowNodeGroupDescriptor();
        grpNode.setWorkflow(parent);
        grpNode.setName(name);
        float[] coords = placeNode(parentNode, Direction.RIGHT);
        grpNode.setxCoord(coords[0]);
        grpNode.setyCoord(coords[1]);
        grpNode.setCreated(LocalDateTime.now());
        grpNode.setPreserveOutput(true);
        return workflowService.addGroup(parent.getId(), grpNode, parentNode.getId(), nodes);
    }

    protected void addLink(WorkflowDescriptor workflow, WorkflowNodeDescriptor parent, WorkflowNodeDescriptor child) throws PersistenceException {
        if (parent != null && child != null) {
            ProcessingComponent component1 = componentService.findById(parent.getComponentId());
            ProcessingComponent component2 = componentService.findById(child.getComponentId());
            workflowService.addLink(parent.getId(), component1.getTargets().get(0).getId(),
                                    child.getId(), component2.getSources().get(0).getId());
        }
    }

    private float[] placeNode(WorkflowNodeDescriptor relativeTo, Direction direction) {
        float x,y;
        if (relativeTo == null) {
            x = xOrigin;
            y = yOrigin;
        } else {
            x = relativeTo.getxCoord();
            y = relativeTo.getyCoord();
        }
        if (direction != null) {
            switch (direction) {
                case TOP:
                    y -= yStep;
                    break;
                case BOTTOM:
                    y += yStep;
                    break;
                case LEFT:
                    x -= xStep;
                    break;
                case RIGHT:
                    x += xStep;
                    break;
                case TOP_LEFT:
                    x -= xStep;
                    y -= yStep;
                    break;
                case TOP_RIGHT:
                    x += xStep;
                    y -= yStep;
                    break;
                case BOTTOM_LEFT:
                    x -= xStep;
                    y += yStep;
                    break;
                case BOTTOM_RIGHT:
                    x += xStep;
                    y += yStep;
                    break;
            }
        }
        return new float[] { x, y };
    }

    public enum Direction {
        TOP,
        LEFT,
        BOTTOM,
        RIGHT,
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }
}
