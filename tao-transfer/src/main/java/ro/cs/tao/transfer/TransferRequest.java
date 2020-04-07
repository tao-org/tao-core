package ro.cs.tao.transfer;

import ro.cs.tao.topology.NodeDescription;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO class encapsulating a file copy request.
 *
 * @author Cosmin Cara
 * @since 1.1.0
 */
public class TransferRequest {
    private final long taskId;
    private final NodeDescription targetHost;
    private String target;
    private Set<String> sourceFiles;

    /**
     * Creates a new file transfer request for the given task identifier and on the given host.
     * @param taskId    The execution task identifier
     * @param toHost    The host to which to transfer files
     */
    public TransferRequest(long taskId, NodeDescription toHost) {
        this.taskId = taskId;
        this.targetHost = toHost;
    }

    /**
     * Returns the execution task identifier for which the request was submitted
     */
    public long getTaskId() { return taskId; }

    /**
     * Returns the host to which the files should be copied.
     */
    public NodeDescription getTargetHost() { return targetHost; }

    /**
     * Returns the destination (root) folder into which the files will be copied.
     */
    public String getTarget() { return target; }

    /**
     * Sets the destination (root) folder into which the files will be copied.
     */
    public void setTarget(String target) { this.target = target; }

    /**
     * Returns the files to be copied (with their original paths).
     */
    public Set<String> getSourceFiles() {
        if (sourceFiles == null) {
            sourceFiles = new LinkedHashSet<>();
        }
        return sourceFiles;
    }
    /**
     * Sets the files to be copied (with their original paths).
     */
    public void setSourceFiles(Set<String> sourceFiles) {
        this.sourceFiles = new LinkedHashSet<>(sourceFiles);
    }
    /**
     * Adds a file to be copied (its original paths).
     */
    public void addSourceFile(String file) {
        getSourceFiles().add(file);
    }
}
