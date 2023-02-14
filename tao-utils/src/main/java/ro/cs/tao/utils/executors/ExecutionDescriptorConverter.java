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

    boolean isIntendedFor(ExecutionUnitFormat unitFormat);

}
