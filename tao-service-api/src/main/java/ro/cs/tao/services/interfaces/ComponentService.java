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

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;

import java.util.List;

/**
 * Service for managing ProcessingComponent entities.
 *
 * @author Cosmin Cara
 */
public interface ComponentService extends CRUDService<ProcessingComponent> {

    /**
     * Retrieves the list of class names that represent component input/output constraints.
     */
    List<String> getAvailableConstraints();

    /**
     * Retrieves the list of processing components belonging to a user.
     * @param userName  The user name
     */
    List<ProcessingComponent> getUserProcessingComponents(String userName);

    /**
     * Retrieves the list of script components belonging to a user.
     * @param userName  The user name
     */
    List<ProcessingComponent> getUserScriptComponents(String userName);

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
}
