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

package ro.cs.tao.component;

/**
 * A runtime optimizer is a helper class that tries to combine several components belonging to the same toolbox
 * into a single execution chain, in order to avoid the intermediate I/O.
 *
 * @author Cosmin Cara
 */
public interface RuntimeOptimizer {
    /**
     * Checks if this optimizer is intended for the given container (toolbox).
     *
     * @param containerId   The container identifier
     */
    boolean isIntendedFor(String containerId);

    /**
     * Aggregates the given processing components into a single one.
     * The new component will have the inputs of the first component and the outputs of the last component
     *
     * @param sources   The components to be aggregated
     */
    ProcessingComponent aggregate(ProcessingComponent... sources);
}
