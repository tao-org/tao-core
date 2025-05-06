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

/**
 * Interface for listening to changes in the status of an execution task.
 *
 * Implementers of this interface can define custom logic to handle changes
 * in the state of {@code ExecutionTask} instances. The {@code statusChanged}
 * method provides a default implementation that can be overridden to process
 * the status updates.
 *
 * @author Cosmin Cara
 */
public interface StatusChangeListener {
    default void statusChanged(ExecutionTask changedTask) { }
}
