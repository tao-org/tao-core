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

import ro.cs.tao.datasource.beans.Parameter;
import ro.cs.tao.execution.model.ExecutionJob;
import ro.cs.tao.services.model.FileObject;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Generic interface for a WPS web service.
 * @param <W>   The capability type
 * @param <T>   The output type descriptor
 */
public interface WebProcessingService<W, T> extends TAOService {

    /**
     * Returns a list of capabilities (operations) supported by this service
     */
    List<W> getCapabilities();

    /**
     * Describes a supported operation
     * @param identifier    The operation identifier
     */
    ProcessInfo<W, T> describeProcess(long identifier);

    /**
     * Executes the operation identified by <code>identifier</code> using the given parameter values
     * @param identifier    The operation identifier
     * @param parameters    The parameter [name,value] pairs
     * @return              The execution job identifier
     */
    long execute(long identifier, Map<String, Map<String, String>> parameters);

    default long execute(String user, long identifier, Map<String, Map<String, String>> parameters) {
        return execute(identifier, parameters);
    }

    /**
     * Returns information about the execution job having the given identifier
     * @param executionJobId    The execution job identifier
     */
    ExecutionJob getStatus(long executionJobId);

    /**
     * Returns the list of results of the given execution job
     * @param executionJobId    The execution job identifier
     */
    List<FileObject> getJobResult(long executionJobId) throws IOException;

    /**
     * Interface describing an operation
     * @param <W>   The operation descriptor type
     * @param <T>   The output descriptor type
     */
    interface ProcessInfo<W, T> {
        /**
         * The operation (workflow) description
         */
        W getCapabilityInfo();

        /**
         * The supported parameters (grouped by workflow node)
         */
        Map<String, List<Parameter>> getParameters();

        /**
         * The description of the outputs
         */
        List<T> getOutputs();
    }
}
