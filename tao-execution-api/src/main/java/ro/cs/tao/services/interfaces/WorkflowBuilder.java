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

package ro.cs.tao.services.interfaces;

import ro.cs.tao.persistence.PersistenceException;
import ro.cs.tao.workflow.WorkflowDescriptor;

/**
 * This interface defines the structure and functionality for building workflow descriptors.
 * A workflow descriptor represents a configuration or definition of a workflow.
 * Implementations of this interface provide specific mechanisms for creating and managing
 * workflows, including the ability to generate system workflows.
 */
public interface WorkflowBuilder {

    /**
     * Retrieves the name associated with the builder.
     *
     * @return the name as a string
     */
    String getName();

    /**
     * Creates a new instance of a workflow descriptor. The workflow descriptor
     * serves as a blueprint or configuration for a workflow, containing all the
     * necessary details for defining the structure and behavior of the workflow.
     *
     * @return a new instance of {@link WorkflowDescriptor}
     * @throws PersistenceException if an error occurs during the creation process
     */
    WorkflowDescriptor createWorkflowDescriptor() throws PersistenceException;

    /**
     * Creates a new instance of the system-level workflow descriptor. This method
     * serves as a shortcut for creating a workflow descriptor with default system-level
     * configurations or properties, delegating to the {@code createWorkflowDescriptor} method.
     *
     * @return a new instance of {@link WorkflowDescriptor}
     * @throws PersistenceException if an error occurs during the creation process
     */
    default WorkflowDescriptor createSystemWorkflowDescriptor() throws PersistenceException {
        return createWorkflowDescriptor();
    }
}
