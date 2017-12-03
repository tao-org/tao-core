package ro.cs.tao.services.interfaces;

import ro.cs.tao.component.ProcessingComponent;
import ro.cs.tao.serialization.MediaType;
import ro.cs.tao.serialization.SerializationException;

/**
 * Service for managing ProcessingComponent entities.
 *
 * @author Cosmin Cara
 */
public interface ComponentService extends CRUDService<ProcessingComponent> {

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
