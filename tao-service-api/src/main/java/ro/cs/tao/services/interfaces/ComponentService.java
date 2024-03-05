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

import ro.cs.tao.Sort;
import ro.cs.tao.Tag;
import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;
import ro.cs.tao.services.model.component.ProcessingComponentInfo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Service for managing ProcessingComponent entities.
 *
 * @author Cosmin Cara
 */
public interface ComponentService extends CRUDService<ProcessingComponent, String> {

    /**
     * Retrieves the list of class names that represent component input/output constraints.
     */
    List<String> getAvailableConstraints();

    /**
     * Retrieves all the processing components
     */
    List<ProcessingComponentInfo> getProcessingComponents();

    /**
     * Retrieves a page of processing components
     * @param pageNumber    The page number
     * @param pageSize      The page size
     * @param sort          The sort direction
     * @return
     */
    List<ProcessingComponentInfo> getProcessingComponents(int pageNumber, int pageSize, Sort sort);

    /**
     * Retrieves the list of processing components belonging to a user.
     * @param userId  The user identifier
     */
    List<ProcessingComponentInfo> getUserProcessingComponents(String userId);

    /**
     * Retrieves the information for all the components except the ones with the given ids.
     * @param ids   The component identifiers to exclude
     */
    List<ProcessingComponentInfo> getOtherComponents(Set<String> ids);

    /**
     * Retrieves the list of script components belonging to a user.
     * @param userId  The user identifier
     */
    List<ProcessingComponentInfo> getUserScriptComponents(String userId);

    /**
     * Retrieves all the tags associated to components
     */
    List<Tag> getComponentTags();

    /**
     * Imports the definition of a processing component from the given data.
     *
     * @param mediaType     The type of the data. Can be one of JSON or XML.
     * @param data          The component definition.
     */
    ProcessingComponent importFrom(MediaType mediaType, String data) throws SerializationException;

    /**
     * Exports the given processing component entity to the specified media type.
     *
     * @param mediaType     The format of the output. Can be one of JSON or XML.
     * @param component     The entity to be exported.
     */
    String exportTo(MediaType mediaType, ProcessingComponent component) throws SerializationException;

    /**
     * Imports the definition of a component from a stream source.
     * @param source    The input stream source
     * @throws IOException
     */
    ProcessingComponent importComponent(InputStream source) throws IOException, SerializationException;

    /**
     * Exports the definition of a component as a JSON
     * @param component The component to be exported
     */
    String exportComponent(ProcessingComponent component) throws SerializationException;
}
