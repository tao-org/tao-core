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
import ro.cs.tao.component.TaoComponent;
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
     * Finds processing components that are similar to the provided text and belong to the specified user.
     *
     * @param text  The text to compare for similarity.
     * @param userId The unique identifier of the user whose components will be searched.
     * @return A list of processing components that match the similarity criteria.
     */
    List<ProcessingComponentInfo> findBySimilarity(String text, String userId);

    /**
     * Finds and retrieves a list of labels based on their similarity to the provided text
     * and belonging to the specified user.
     *
     * @param text  The text to compare for similarity.
     * @param userId The unique identifier of the user whose labels will be searched.
     * @return A list of labels that match the similarity criteria.
     */
    List<String> findLabelsBySimilarity(String text, String userId);

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

    /**
     * Transforms the specified processing component into a source descriptor.
     *
     * @param parameterId      The unique identifier of the parameter to be transformed into a source descriptor.
     * @param parentComponent  The parent processing component to which the transformation applies.
     */
    void transformIntoSource(String parameterId, ProcessingComponent parentComponent);

    /**
     * Transforms the specified parameter into a target descriptor within the context of the provided parent processing component.
     *
     * @param parameterId      The unique identifier of the parameter to be transformed into a target descriptor.
     * @param parentComponent  The parent processing component to which the transformation is applied.
     */
    void transformIntoTarget(String parameterId, ProcessingComponent parentComponent);

    /**
     * Transforms the specified source or target descriptor into a parameter within the context
     * of the provided parent processing component.
     *
     * @param sourceOrTargetDescriptorId The unique identifier of the descriptor (either source or target)
     *                                   to be transformed into a parameter.
     * @param parentComponent            The parent processing component to which the transformation applies.
     */
    void transformIntoParameter(String sourceOrTargetDescriptorId, ProcessingComponent parentComponent);

    /**
     * Retrieves a TaoComponent instance based on its unique identifier.
     *
     * @param id the unique identifier of the component to be retrieved.
     * @return the TaoComponent instance corresponding to the provided identifier, or null if no matching component is found.
     */
    TaoComponent getComponent(String id);
}
