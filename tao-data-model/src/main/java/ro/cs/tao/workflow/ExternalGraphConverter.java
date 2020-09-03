package ro.cs.tao.workflow;

/**
 * A graph (i.e. execution graph) is a sequence of tasks to be executed, expressed as a JSON object.
 * External such graphs are converted to TAO workflows and then executed by TAO, in the respective Docker containers.
 */
public interface ExternalGraphConverter {
    /**
     * Converts the given JSON representation of an execution graph to a TAO workflow descriptor.
     * Prior to this, the Docker container that hosts the components of this graph should have been registered with TAO.
     * @param jsonGraph The execution graph
     * @param container The Docker container name
     */
    WorkflowDescriptor convert(String jsonGraph, String container) throws Exception;
}
