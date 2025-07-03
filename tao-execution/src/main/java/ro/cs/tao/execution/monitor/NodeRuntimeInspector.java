package ro.cs.tao.execution.monitor;

import ro.cs.tao.topology.NodeRole;
import ro.cs.tao.utils.executors.AuthenticationType;

import java.io.IOException;

/**
 * NodeRuntimeInspector defines an interface for inspecting and retrieving runtime
 * information for a specific node, including processor usage, memory, and disk
 * utilization. It is intended to support different types of node roles and can
 * be implemented for various platforms or environments.
 */
public interface NodeRuntimeInspector {

    /**
     * Determines whether the specified node role is intended for the current context or operation.
     *
     * @param role the node role to check, representing a specific type of node
     *             (e.g., MASTER, WORKER, K8S).
     * @return true if the specified node role is intended for the current context;
     *         false otherwise.
     */
    boolean isIntendedFor(NodeRole role);

    /**
     * Initializes the node runtime inspector with the provided connection details
     * and authentication type.
     *
     * @param host the hostname or IP address of the node to connect to
     * @param user the user name for authentication
     * @param password the password for authentication
     * @param authType the authentication type to be used, either PASSWORD or CERTIFICATE
     * @throws Exception if an error occurs during initialization
     */
    void initialize(String host, String user, String password, AuthenticationType authType) throws Exception;

    /**
     * Retrieves the current processor usage of the node as a percentage value.
     * Processor usage is calculated based on the system's CPU utilization.
     *
     * @return the current processor usage as a decimal value, where 1.0 indicates
     *         100% usage, and 0.0 indicates 0% usage.
     * @throws IOException if an error occurs while obtaining processor usage data,
     *                     such as issues accessing system resources.
     */
    double getProcessorUsage() throws IOException;

    /**
     * Retrieves the total memory of the node in megabytes.
     *
     * @return the total memory of the node,*/
    long getTotalMemoryMB() throws IOException;

    /**
     * Retrieves the amount of available memory on the node in megabytes.
     * The available memory refers to the memory that is not currently in use
     * by the system or applications.
     *
     * @return the available memory on the node, measured in megabytes
     * @throws IOException if an error occurs while retrieving the available memory
     */
    long getAvailableMemoryMB() throws IOException;

    /**
     * Retrieves the total disk space of the node in gigabytes.
     *
     * @return the total disk space available on the node, measured in gigabytes
     * @throws IOException if an error occurs while retrieving the disk space
     */
    long getTotalDiskGB() throws IOException;

    /**
     * Retrieves the amount of used disk space on the node in gigabytes.
     *
     * @return the used disk space on the node, measured in gigabytes
     * @throws IOException if an error occurs while retrieving the disk usage information
     */
    long getUsedDiskGB() throws IOException;

    /**
     * Retrieves runtime information for the current node, such as CPU usage,
     * memory data, and disk space details.
     *
     * @return an instance of {@code RuntimeInfo} containing details about
     *         processor usage, memory metrics, disk usage, and a timestamp
     *         representing when the information was gathered
     * @throws Exception if an error occurs while fetching runtime information
     */
    RuntimeInfo getInfo() throws Exception;

    /**
     * Retrieves a snapshot of the current runtime information for the node.
     * The snapshot contains details such as CPU usage, memory data, and disk space details,
     * along with a timestamp indicating when the snapshot was taken.
     *
     * @return an instance of {@code RuntimeInfo} containing information about
     *         processor usage, memory metrics, disk usage, and the timestamp
     *         of the data collection.
     * @throws Exception if an error occurs while fetching the runtime information.
     */
    RuntimeInfo getSnapshot() throws Exception;

    /**
     * Retrieves runtime information for the current node if it is a cluster, such as CPU usage,
     * memory data, and disk space details.
     *
     * @return an instance of {@code RuntimeInfoCluster} containing details about
     *         processor usage, memory metrics, disk usage, and a timestamp
     *         representing when the information was gathered for each node and an aggregated information of all nodes
     * @throws Exception if an error occurs while fetching runtime information
     */
    default RuntimeInfoCluster getInfoCluster() throws Exception {
        return null;
    }

    /**
     * Retrieves runtime information for the current node if it is a cluster, such as CPU usage,
     * memory data, and disk space details.
     *
     * @return an instance of {@code RuntimeInfoCluster} containing details about
     *         processor usage, memory metrics, disk usage, and a timestamp
     *         representing when the information was gathered for each node and an aggregated information of all nodes
     * @throws Exception if an error occurs while fetching runtime information
     */
    default RuntimeInfoCluster getSnapshotCluster() throws Exception {
        return null;
    }

}
