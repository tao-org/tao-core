package ro.cs.tao.utils.executors;

import ro.cs.tao.utils.ExecutionUnitFormat;

/**
 * An execution descriptor converter translates the TAO job graph into another format.
 * The conversion takes care of representing both the graph dependencies and the nodes inputs, outputs and parameters.
 *
 * @author Cosmin Cara
 * @since 1.4.2
 */
public interface ExecutionDescriptorConverter {

    /**
     * Determines whether this converter is intended for the specified execution unit format.
     *
     * @param unitFormat The format of the execution unit to be checked.
     * @return true if this converter supports the specified format; false otherwise.
     */
    boolean isIntendedFor(ExecutionUnitFormat unitFormat);

}
